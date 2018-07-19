package com.cyberneticscore.graphvisualizer;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.*;
import com.vivosys.osgi.deps.builder.graph.Edge;
import com.vivosys.osgi.deps.builder.graph.Graph;
import com.vivosys.osgi.deps.builder.graph.Vertex;
import com.vivosys.osgi.deps.builder.graphml.GraphMLGenerator;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class JenkinsFlowVisualizer {

    private final long topJobTimestamp;
    private final Graph graph;

    private final int skippedTimeSec;

    private final ArrayList<String> processedNodes = new ArrayList<>();

    JenkinsFlowVisualizer(String URL,
                          String username,
                          String password,
                          String topJobName,
                          String graphLocation,
                          int skippedTimeSec) throws IOException, URISyntaxException {
        JenkinsServer jenkins = new JenkinsServer(new URI(URL),
                username,
                password);

        graph = new Graph();

        this.skippedTimeSec = skippedTimeSec;

        JobWithDetails topJob = jenkins.getJob(topJobName);

        topJobTimestamp = topJob.getLastBuild().details().getTimestamp();

        Vertex rootVertex = graph.findOrCreate(topJob.getName());
        rootVertex.setContainer(false);
        rootVertex.setLabel(topJob.getName());
        rootVertex.setColor(Color.GREEN);

        String flowType = topJob.getLastBuild().details().getParameters().getOrDefault("BUILD_SCOPE", "UNSPECIFIED");
        rootVertex.appendLabel("Build FLOW - " + flowType);

        iterateOverChildren(topJob, rootVertex);

        writeGraphToFile(graphLocation);
    }

    private void displayUnusedJobs(View view) {
        System.out.println("Validating jobs that are missing");
        for (Job job : view.getJobs()) {
            if (processedNodes.contains(job.getName())) {
                continue;
            }

            Vertex missingVertex = graph.findOrCreate(job.getName(), Color.magenta);
            missingVertex.setLabel(job.getName());
            missingVertex.setContainer(false);

            Edge importEdge = new Edge(missingVertex, missingVertex, "", Color.black);
            graph.addEdge(importEdge);
        }

    }

    private void writeGraphToFile(String fileName) throws IOException {
        GraphMLGenerator gen = new GraphMLGenerator();
        try (Writer out = new FileWriter(fileName)) {
            gen.serialize(graph, out);
            System.out.println("Wrote relationship graph to " + fileName);
        } catch (Exception e) {
            System.out.println("Error generating relationship graph.");
            throw e;
        }
    }

    private void iterateOverChildren(JobWithDetails parentJob, Vertex parentVertex) throws IOException {
        for (Job currentJob : parentJob.getDownstreamProjects()) {
            String jobName = currentJob.getName();
            System.out.println("PROCESSING: " + jobName);

            Vertex currentVertex = getVertexForCurrentJob(parentVertex, jobName);

            if (processedNodes.contains(jobName)) {
                continue;
            }

            processJobs(currentJob, currentVertex);

            processedNodes.add(jobName);
            iterateOverChildren(currentJob.details(), currentVertex);
        }
    }

    private void processJobs(Job currentJob, Vertex currentVertex) throws IOException {
        BuildWithDetails details = currentJob.details().getLastBuild().details();

        if (details.getTimestamp() < topJobTimestamp) {
            currentVertex.setColor(Color.LIGHT_GRAY);
            currentVertex.appendLabel("( OLD JOB )");
            return;
        }

        if (details.isBuilding()) {
            currentVertex.setColor(Color.decode("#ccffff"));
            return;
        }

        if (details.getDuration() < skippedTimeSec * 1000) {
            currentVertex.appendLabel("( Skipped )");
            currentVertex.setColor(Color.PINK);
            return;
        }

        switch (details.getResult()) {
            case REBUILDING:
            case BUILDING: {
                currentVertex.setColor(Color.decode("#ccffff"));
                return;
            }

            case UNSTABLE: {
                String formatted = String.format("P_%s / S_%s / F_%s",
                        details.getTestResult().getPassCount(),
                        details.getTestResult().getSkipCount(),
                        details.getTestResult().getFailCount()
                );

                currentVertex.appendLabel(formatted);
                currentVertex.setColor(Color.ORANGE);
                return;
            }

            case FAILURE: {
                currentVertex.setColor(Color.RED);
                return;
            }

            case ABORTED:
            case CANCELLED: {
                currentVertex.setColor(Color.RED);
                return;
            }

            case SUCCESS: {
                currentVertex.setColor(Color.GREEN);
                return;
            }

            case NOT_BUILT: {
                currentVertex.appendLabel("( NOT BUILT )");
                currentVertex.setColor(Color.WHITE);
                return;
            }

            default: {
                System.out.println("UNKOWN Result: " + details.getResult());
                currentVertex.setColor(Color.DARK_GRAY);
                return;
            }
        }
    }

    private Vertex getVertexForCurrentJob(Vertex parentVertex, String jobName) {
        Vertex currentVertex = graph.findOrCreate(jobName);
        currentVertex.setContainer(false);

        if (currentVertex.getLabel() == null) {
            currentVertex.setLabel(jobName);
        }

        Edge importEdge = new Edge(parentVertex, currentVertex, "", Color.black);
        graph.addEdge(importEdge);
        return currentVertex;
    }

}
