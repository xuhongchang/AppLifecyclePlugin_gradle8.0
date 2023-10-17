package com.hm.iou.lifecycle.plugin

import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile

object ScanUtil {


        lateinit var FILE_CONTAINS_INIT_CLASS: File



    /**
     * 判断该class是否是我们的目标类
     *
     * @param file
     * @return
     */
     fun isTargetProxyClass( file:File) :Boolean{
        println( " file name:${file.name}")
        if (file.name.endsWith(GlobalConfig.PROXY_CLASS_SUFFIX) && file.name.startsWith(GlobalConfig.PROXY_CLASS_PREFIX)) {
            return true
        }
        return false
    }

    /**
     * 扫描jar包里的所有class文件：
     * 1.通过包名识别所有需要注入的类名
     * 2.找到注入管理类所在的jar包，后面我们会在该jar包里进行代码注入
     *
     * @param jarFile
     * @param destFile
     * @return
     */
     fun scanJar( jarFile:File,  destFile:File):ArrayList<String>?  {
        val file =  JarFile(jarFile)
        val enumeration = file.entries()
        var list:ArrayList<String>?=null
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            val entryName = jarEntry.getName()
            println( " entry name:${entryName}")
            if (entryName == GlobalConfig.REGISTER_CLASS_FILE_NAME) {
                //标记这个jar包包含 ApplicationLifecycleManager.class
                //扫描结束后，我们会生成注册代码到这个文件里
                println( "标记这个jar包包含 ApplicationLifecycleManager.class")
                FILE_CONTAINS_INIT_CLASS = destFile
            } else {
                val startsWith = entryName.startsWith(GlobalConfig.PROXY_CLASS_PACKAGE_NAME)
                if (startsWith) {
                    val substring = entryName.substring(entryName.lastIndexOf("/") + 1)
                    println ("找到文件：${substring}")
                    if (list == null) {
                        list =  ArrayList()
                    }
                    if (!substring.isEmpty()) {
                        list.add(substring)
                    }
                }
            }
        }
        return list
    }

     fun shouldProcessPreDexJar( path:String) :Boolean{
        return !path.contains("com.android.support") && !path.contains("/android/m2repository")
    }

}