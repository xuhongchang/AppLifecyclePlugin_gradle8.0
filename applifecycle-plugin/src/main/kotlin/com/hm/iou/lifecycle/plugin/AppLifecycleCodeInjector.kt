package com.hm.iou.lifecycle.plugin


import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class AppLifecycleCodeInjector(val proxyAppLifecycleClassList: List<String>) {


    fun execute() {
        println("开始执行ASM方法======>>>>>>>>")

        val srcFile = ScanUtil.FILE_CONTAINS_INIT_CLASS
        //创建一个临时jar文件，要修改注入的字节码会先写入该文件里
        val optJar = File(srcFile.getParent(), srcFile.name + ".opt")
        if (optJar.exists())
            optJar.delete()
        val file = JarFile(srcFile)
        val enumeration = file.entries()
        val jarOutputStream = JarOutputStream(FileOutputStream(optJar))
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            val entryName = jarEntry.getName()
            val zipEntry = ZipEntry(entryName)
            val inputStream = file.getInputStream(jarEntry)
            jarOutputStream.putNextEntry(zipEntry)

            //找到需要插入代码的class，通过ASM动态注入字节码
            if (GlobalConfig.REGISTER_CLASS_FILE_NAME == entryName) {
                println("insert register code to class >> " + entryName)

                val classReader = ClassReader(inputStream)
                // 构建一个ClassWriter对象，并设置让系统自动计算栈和本地变量大小
                val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
                val classVisitor = AppLifecycleClassVisitor(classWriter)
                //开始扫描class文件
                classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)

                val bytes = classWriter.toByteArray()
                //将注入过字节码的class，写入临时jar文件里
                jarOutputStream.write(bytes)
            } else {
                //不需要修改的class，原样写入临时jar文件里
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            inputStream.close()
            jarOutputStream.closeEntry()
        }

        jarOutputStream.close()
        file.close()

        //删除原来的jar文件
        if (srcFile.exists()) {
            srcFile.delete()
        }
        //重新命名临时jar文件，新的jar包里已经包含了我们注入的字节码了
        optJar.renameTo(srcFile)
        println("AppLifecycleCodeInjector srcFile=${srcFile.getAbsolutePath()}")
    }


    fun execute1(inputStream: InputStream): ByteArray {
        println("开始执行ASM方法======>>>>>>>>1")
        val classReader = ClassReader(inputStream)
        // 构建一个ClassWriter对象，并设置让系统自动计算栈和本地变量大小
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val classVisitor = AppLifecycleClassVisitor(classWriter)
        //开始扫描class文件
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)

        return classWriter.toByteArray()
    }

    inner class AppLifecycleClassVisitor(val mClassVisitor: ClassVisitor) :
        ClassVisitor(Opcodes.ASM5, mClassVisitor) {


        override fun visitMethod(
            access: Int,
            name: String?,
            desc: String?,
            signature: String?,
            exception: Array<out String>?
        ): MethodVisitor {
            println("visit method: " + name)
            var methodVisitor = mClassVisitor.visitMethod(access, name, desc, signature, exception)
            if ("init" == name) {
                methodVisitor = LoadAppLifecycleMethodAdapter(methodVisitor, access, name, desc)
            }
            return methodVisitor
        }


    }

    inner class LoadAppLifecycleMethodAdapter(
        mv: MethodVisitor,
        access: Int,
        name: String,
        desc: String?
    ) : AdviceAdapter(Opcodes.ASM5, mv, access, name, desc) {

        override fun onMethodEnter() {
            super.onMethodEnter()
            println("-------onMethodEnter------")
            proxyAppLifecycleClassList.forEach { proxyClassName ->
                println("开始注入代码：${proxyClassName}")
                val fullName = GlobalConfig.PROXY_CLASS_PACKAGE_NAME.replace(
                    "/",
                    "."
                ) + "." + proxyClassName.substring(0, proxyClassName.length - 6)
                println("full classname = ${fullName}")
                mv.visitLdcInsn(fullName)
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    GlobalConfig.INJECT_CLASS_NAME,
                    GlobalConfig.INJECT_METHOD_NAME,
                    GlobalConfig.INJECT_PARAMS_DESC,
                    false
                )
            }
        }

        override fun onMethodExit(opcode: Int) {
            super.onMethodExit(opcode)
            println("-------onMethodExit------")
        }

    }

}