package com.bill.myplugin

import org.gradle.api.Plugin;
import org.gradle.api.Project;

class MyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        System.out.println("Hello this is a single gradle plugin...");
    }
}