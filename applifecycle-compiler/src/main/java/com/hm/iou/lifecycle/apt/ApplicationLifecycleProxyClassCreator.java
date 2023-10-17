package com.hm.iou.lifecycle.apt;

import com.hm.iou.lifecycle.ApplicationLifecycleConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by hjy on 2018/10/23.
 */
public class ApplicationLifecycleProxyClassCreator {

    private static final String METHOD_ON_CREATE = "onCreate";
    private static final String METHOD_ON_TERMINATE = "onTerminate";
    private static final String METHOD_ON_LOW_MEMORY = "onLowMemory";
    private static final String METHOD_ON_TRIM_MEMORY = "onTrimMemory";
    private static final String METHOD_GET_PRIORITY = "getPriority";

    private static final String FIELD_APPLICATION_LIFECYCLE_CALLBACK = "mApplicationLifecycleCallback";

    public static boolean generateProxyClassCode(TypeElement typeElement, Filer filer, TypeMirror contextType) {
        TypeSpec appLifecycleProxyClass = getApplicationLifecycleProxyClass(typeElement, contextType);
        JavaFile javaFile = JavaFile.builder(ApplicationLifecycleConfig.PROXY_CLASS_PACKAGE_NAME, appLifecycleProxyClass).build();
        try {
            javaFile.writeTo(filer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static TypeSpec getApplicationLifecycleProxyClass(TypeElement typeElement, TypeMirror contextType) {
        return TypeSpec.classBuilder(getProxyClassName(typeElement.getSimpleName().toString()))
                .addSuperinterface(TypeName.get(typeElement.getInterfaces().get(0)))
                .addModifiers(Modifier.PUBLIC)
                .addField(TypeName.get(typeElement.getInterfaces().get(0)), FIELD_APPLICATION_LIFECYCLE_CALLBACK, Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(getConstructorMethod(typeElement))
                .addMethod(getPriorityMethod())
                .addMethod(getOnCreateMethod(contextType))
                .addMethod(getOnLowMemoryMethod())
                .addMethod(getOnTerminateMethod())
                .addMethod(getOnTrimMemoryMethod())
                .build();
    }

    private static String getProxyClassName(String simpleClassName) {
        return ApplicationLifecycleConfig.PROXY_CLASS_PREFIX + simpleClassName +
                ApplicationLifecycleConfig.PROXY_CLASS_SUFFIX;
    }

    private static MethodSpec getOnLowMemoryMethod() {
        return MethodSpec.methodBuilder(METHOD_ON_LOW_MEMORY)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addAnnotation(Override.class)
                .addStatement("this.$N.$N()", FIELD_APPLICATION_LIFECYCLE_CALLBACK, METHOD_ON_LOW_MEMORY)
                .build();
    }

    private static MethodSpec getOnTrimMemoryMethod() {
        return MethodSpec.methodBuilder(METHOD_ON_TRIM_MEMORY)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addAnnotation(Override.class)
                .addParameter(int.class, "level")
                .addStatement("this.$N.$N($N)", FIELD_APPLICATION_LIFECYCLE_CALLBACK, METHOD_ON_TRIM_MEMORY, "level")
                .build();
    }

    private static MethodSpec getOnTerminateMethod() {
        return MethodSpec.methodBuilder(METHOD_ON_TERMINATE)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addAnnotation(Override.class)
                .addStatement("this.$N.$N()", FIELD_APPLICATION_LIFECYCLE_CALLBACK, METHOD_ON_TERMINATE)
                .build();
    }

    private static MethodSpec getPriorityMethod() {
        return MethodSpec.methodBuilder(METHOD_GET_PRIORITY)
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addAnnotation(Override.class)
                .addStatement("return this.$N.$N()", FIELD_APPLICATION_LIFECYCLE_CALLBACK, METHOD_GET_PRIORITY)
                .build();
    }

    private static MethodSpec getConstructorMethod(TypeElement typeElement) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.$N = new $T()", FIELD_APPLICATION_LIFECYCLE_CALLBACK, ClassName.get(typeElement))
                .build();
    }

    private static MethodSpec getOnCreateMethod(TypeMirror contextType) {
        return MethodSpec.methodBuilder(METHOD_ON_CREATE)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(contextType), "context")
                .addStatement("this.$N.$N($N)", FIELD_APPLICATION_LIFECYCLE_CALLBACK, METHOD_ON_CREATE, "context")
                .build();
    }

}
