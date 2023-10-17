package com.hm.iou.lifecycle.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import org.gradle.api.Plugin
import org.gradle.api.Project
//定义一个名为 AppLifecyclePlugin 的类，实现了 Plugin<Project> 接口。这意味着该类是一个 Gradle 插件，并在应用到项目中时会执行 apply 方法。
class AppLifecyclePlugin:Plugin<Project> {
    override fun apply(project: Project) {
        //获取项目的 AndroidComponentsExtension 实例，该实例包含有关 Android 组件（如变体、任务等）的信息和配置。
        val androidComponents =
            project.extensions.getByType(AndroidComponentsExtension::class.java)

        //使用 onVariants 函数监听所有的变体（variants），并对每个变体执行特定的操作。variant 表示当前变体的信息和配置。
        androidComponents.onVariants { variant ->
            //创建一个名为 ${variant.name}TransformAllClassesTask 的任务，并将其注册到项目的任务列表中。ModifyClassesTask 是一个自定义的任务类型，用于修改类文件。
            val taskProviderTransformAllClassesTask =
                project.tasks.register(
                    "${variant.name}TransformAllClassesTask",
                    ModifyClassesTask::class.java
                )
            // https://github.com/android/gradle-recipes
            //获取当前变体的所有构建产物，并使用 use 函数将任务 taskProviderTransformAllClassesTask 应用于这些构建产物。
            variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                .use(taskProviderTransformAllClassesTask)
                //将任务 taskProviderTransformAllClassesTask 设置为转换（transform）任务，作用于类文件（ScopedArtifact.CLASSES）。
                // 这里使用了 ModifyClassesTask::allJars、ModifyClassesTask::allDirectories 和 ModifyClassesTask::output 方法来指定输入和输出。
                .toTransform(
                    ScopedArtifact.CLASSES,
                    ModifyClassesTask::allJars,
                    ModifyClassesTask::allDirectories,
                    ModifyClassesTask::output
                )

        }
    }
}