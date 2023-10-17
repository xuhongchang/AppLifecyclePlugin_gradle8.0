package com.hm.iou.lifecycle.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import javassist.ClassPool
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import java.io.FileOutputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.util.jar.JarFile
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

abstract class ModifyClassesTask : DefaultTask() {

    //输入的所有JAR包
    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    //输入的所有目录
    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    //存储输出文件的TaskProperty类型的变量，它仅包含一个File对象，表示一个JAR包文件
    //只有把需要的文件保存到输出，实际打包才会包含这个文件
    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun taskAction() {

        val appLifecycleCallbackList = ArrayList<String>()


        val jarOutput = JarOutputStream(
            BufferedOutputStream(
                FileOutputStream(
                    output.get().asFile
                )
            )
        )

        allDirectories.get().forEach { directory ->
            println("handling " + directory.asFile.getAbsolutePath())
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
                    if (ScanUtil.isTargetProxyClass(file)) {
                        println("找到文件： ${file.name}")
                        appLifecycleCallbackList.add(file.name)
                    }

                    //这行代码使用目录和文件的URI来计算相对路径。directory表示目录，file表示文件。通过计算相对路径，可以获取文件相对于目录的路径。
                    val relativePath = directory.asFile.toURI().relativize(file.toURI()).getPath()
                    println(
                        "Adding from directory ${
                            relativePath.replace(
                                File.separatorChar,
                                '/'
                            )
                        }"
                    )
                    //这行代码创建了一个新的JarEntry对象，表示要添加到输出JAR包的条目，并将其写入到jarOutput中。这里的relativePath表示文件的相对路径。
                    jarOutput.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                    //这行代码获取文件的输入流，并通过use函数确保在复制完成后正确关闭输入流。接着，使用inputStream.copyTo(jarOutput)方法将文件的内容从输入流复制到输出JAR包的条目中。
                    file.inputStream().use { inputStream ->
                        inputStream.copyTo(jarOutput)
                    }
                    //这行代码表示当前条目的处理已经完成，可以关闭输出JAR包的条目。
                    jarOutput.closeEntry()

                }
            }
        }

        var originInject: ByteArray? = null
        val list =ArrayList<String>()
        //对allJars变量中的所有JAR文件进行遍历，并对每个文件执行下面的代码块
        allJars.get().forEach { file ->
            println("handling " + file.asFile.getAbsolutePath())
            //创建了一个JarFile对象，用于读取JAR文件的内容。file.asFile将File类型的file转换为普通的Java File对象
            val jarFile = JarFile(file.asFile)

            //获取了JAR文件中所有的条目（即文件和目录）
            val enumeration = jarFile.entries()
           list.clear()
            while (enumeration.hasMoreElements()) {
                //获取单个条目
                val jarEntry = enumeration.nextElement()
                try {
                    val entryName = jarEntry.name
                    println(" entry name:${entryName}")
                    if (jarEntry.isDirectory || jarEntry.name.isEmpty()) {
                        continue
                    }
                    if (entryName == GlobalConfig.REGISTER_CLASS_FILE_NAME) {
                        //标记这个jar包包含 ApplicationLifecycleManager.class
                        //扫描结束后，我们会生成注册代码到这个文件里
                        println("标记这个jar包包含 ApplicationLifecycleManager.class")
                        //先把这个文件的内容存起来，然后我们注入新的代码，再把整个文件保存到输出，在这里先不保存到输出
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            originInject = inputs.readAllBytes()
                            // println("Find before originInject is ${originInject?.size}")
                        }
                    } else {
                        //查找需要注入的类名称
                        val startsWith = entryName.startsWith(GlobalConfig.PROXY_CLASS_PACKAGE_NAME)
                        if (startsWith) {
                            val substring = entryName.substring(entryName.lastIndexOf("/") + 1)
                            println("找到文件：${substring}")
                            if (substring.isNotEmpty()) {
                                list.add(substring)
                            }
                        }
                        //创建了一个新的JarEntry对象，表示要添加到输出JAR包的条目，并将其写入到jarOutput中
                        jarOutput.putNextEntry(JarEntry(jarEntry.name))
                        //jarFile.getInputStream(jarEntry)方法返回当前条目的输入流，
                        // 然后使用use函数确保在复制完成后正确地关闭输入流。接着，copyTo方法将输入流的内容复制到输出JAR包的条目中，
                        // 这个条目就是在上一步中创建的新的JarEntry对象
                        //这样就把输入文件保存到输出中了
                        jarFile.getInputStream(jarEntry).use {
                            it.copyTo(jarOutput)
                        }
                        //这行代码表示当前条目的处理已经完成，可以关闭输出JAR包的条目
                        jarOutput.closeEntry()
                    }


                } catch (e: Exception) {
                    println("Merge jar error entry:${jarEntry.name}, error is $e ")
                }
            }

            appLifecycleCallbackList.addAll(list)


            //这行代码表示当前JAR文件的所有条目都已经处理完毕，可以关闭JAR文件
            jarFile.close()
        }

        if (originInject == null) { // Check
            error("Can not find ARouter inject point, Do you import ARouter?")
        }
        //执行字节码注入，然后返回注入后的内容
        val resultByteArray= AppLifecycleCodeInjector(appLifecycleCallbackList).execute1(ByteArrayInputStream(originInject))

        //保存这个注入后的内容到输入
        jarOutput.putNextEntry(JarEntry(GlobalConfig.REGISTER_CLASS_FILE_NAME))
        ByteArrayInputStream(resultByteArray).use {
            it.copyTo(jarOutput)
        }
        jarOutput.closeEntry()
        if (appLifecycleCallbackList.isEmpty()) {
            println(" LifeCycleTransform appLifecycleCallbackList empty")
        }else {
            appLifecycleCallbackList.forEach { item ->
                println("需要注入类：$item")
            }
        }
        jarOutput.close()
    }
}