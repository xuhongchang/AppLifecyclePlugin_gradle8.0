package com.hm.iou.lifecycle.apt;

import com.google.auto.service.AutoService;
import com.hm.iou.lifecycle.ApplicationLifecycleConfig;
import com.hm.iou.lifecycle.annotation.AppLifecycle;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.hm.iou.lifecycle.annotation.AppLifecycle")
public class ApplicationLifecycleProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Elements mElements;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnv.getFiler();
        mElements = processingEnv.getElementUtils();
    }

    /**
     * 支持解析的注解
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(AppLifecycle.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(AppLifecycle.class);

        TypeMirror typeContext = mElements.getTypeElement(ApplicationLifecycleConfig.CONTEXT).asType();

        for (Element element : elements) {
            if (!element.getKind().isClass()) {
                throw new RuntimeException("Annotation AppLifecycle can only be used in class.");
            }
            TypeElement typeElement = (TypeElement) element;
            String fullClassName = typeElement.getQualifiedName().toString();
            System.out.println("process class name : " + fullClassName);

            //这里检查一下，使用了该注解的类，同时必须要实现com.hm.lifecycle.api.IApplicationLifecycleCallbacks接口，否则会报错，因为我们要实现一个代理类
            List<? extends TypeMirror> mirrorList = typeElement.getInterfaces();
            if (mirrorList.isEmpty()) {
                throw new RuntimeException(typeElement.getQualifiedName() + " must implements interface " + ApplicationLifecycleConfig.APPLICATION_LIFECYCLE_CALLBACK_QUALIFIED_NAME);
            }
            boolean checkInterfaceFlag = false;
            for (TypeMirror mirror : mirrorList) {
                if (ApplicationLifecycleConfig.APPLICATION_LIFECYCLE_CALLBACK_QUALIFIED_NAME.equals(mirror.toString())) {
                    checkInterfaceFlag = true;
                }
            }
            if (!checkInterfaceFlag) {
                throw new RuntimeException(typeElement.getQualifiedName() + " must implements interface " + ApplicationLifecycleConfig.APPLICATION_LIFECYCLE_CALLBACK_QUALIFIED_NAME);
            }

            System.out.println(fullClassName+"start to generate proxy class code.");

            ApplicationLifecycleProxyClassCreator.generateProxyClassCode(typeElement, mFiler, typeContext);
        }

        return true;
    }
}