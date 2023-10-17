package xyz.mlya.javaassist


import org.gradle.api.Plugin
import org.gradle.api.Project

class ModifyPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {
        println "------LifeCycle plugin entrance-------"

    }

}