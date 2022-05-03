package com.bill.inner.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Created by Bill on 2022/5/3.
 */

class MyPlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        System.out.println("Hello this is a inner gradle plugin...");
    }
}
