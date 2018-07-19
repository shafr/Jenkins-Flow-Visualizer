package com.cyberneticscore.graphvisualizer;

import org.aeonbits.owner.ConfigFactory;

import java.io.IOException;
import java.net.URISyntaxException;

public class EntryPoint {
    public static void main(String[] args) throws URISyntaxException, IOException {
        AppConfig appConfig = ConfigFactory.create(AppConfig.class, System.getenv(), System.getProperties());

        AppConfig.doPreRunChecks(appConfig);

        new JenkinsFlowVisualizer(
                appConfig.JENKINS_HOST(),
                appConfig.JENKINS_USER(),
                appConfig.JENKINS_PASS(),
                appConfig.JENKINS_BRANCH_TOP_JOB_NAME(),
                appConfig.JENKINS_GRAPH_LOCATION(),
                appConfig.SKIPPED_TIME_SEC()
        );
    }
}
