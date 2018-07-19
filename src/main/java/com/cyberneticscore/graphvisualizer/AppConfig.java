package com.cyberneticscore.graphvisualizer;

import org.aeonbits.owner.Config;

public interface AppConfig extends Config {

    String JENKINS_HOST();

    String JENKINS_USER();

    String JENKINS_PASS();

    String JENKINS_BRANCH_TOP_JOB_NAME();

    String JENKINS_GRAPH_LOCATION();

    @DefaultValue("75")
    int SKIPPED_TIME_SEC();

    static void doPreRunChecks(AppConfig appConfig) {
        checkAndFail(appConfig.JENKINS_HOST(), "JENKINS_HOST");
        checkAndFail(appConfig.JENKINS_USER(), "JENKINS_USER");
        checkAndFail(appConfig.JENKINS_PASS(), "JENKINS_PASS");
        checkAndFail(appConfig.JENKINS_BRANCH_TOP_JOB_NAME(), "JENKINS_BRANCH_TOP_JOB_NAME");
        checkAndFail(appConfig.JENKINS_GRAPH_LOCATION(), "JENKINS_GRAPH_LOCATION");

        printVars(appConfig);
    }

    static void checkAndFail(String variable, String message) {
        if (variable == null) {
            System.out.println("ERROR: " + message + " property is not set, exiting");
            printUsageMessage();
            System.exit(1);
        }
    }

    static void printUsageMessage() {
        System.out.println("Usage");
        System.out.println("Set String params: ");
        System.out.println("JENKINS_HOST");
        System.out.println("JENKINS_USER");
        System.out.println("JENKINS_PASS");
        System.out.println("JENKINS_BRANCH_TOP_JOB_NAME");
        System.out.println("JENKINS_GRAPH_LOCATION");
    }

    static void printVars(AppConfig cfg){
        System.out.println("Using");
        System.out.println("=====================================");
        System.out.println("JENKINS_HOST: " + cfg.JENKINS_HOST());
        System.out.println("JENKINS_USER: " + cfg.JENKINS_USER());
        System.out.println("JENKINS_PASS=**********");
        System.out.println("JENKINS_BRANCH_TOP_JOB_NAME: " + cfg.JENKINS_BRANCH_TOP_JOB_NAME());
        System.out.println("JENKINS_GRAPH_LOCATION: " + cfg.JENKINS_GRAPH_LOCATION());
        System.out.println("SKIPPED_TIME_SEC: " + cfg.SKIPPED_TIME_SEC());
        System.out.println("=====================================");
    }
}
