package com.hm.iou.lifecycle;

public class ApplicationLifecycleConfig {


    /**
     * 生成代理类的包名
     */
    public static final String PROXY_CLASS_PACKAGE_NAME = "com.hm.iou.lifecycle.apt.proxy";

    /**
     * 生成代理类统一的后缀
     */
    public static final String PROXY_CLASS_SUFFIX = "$$Proxy";

    /**
     * 生成代理类统一的前缀
     */
    public static final String PROXY_CLASS_PREFIX = "AppLife$$";


    public static final String APPLICATION_LIFECYCLE_CALLBACK_QUALIFIED_NAME = "com.hm.lifecycle.api.IApplicationLifecycleCallbacks";

    public static final String APPLICATION_LIFECYCLE_CALLBACK_SIMPLE_NAME = "IApplicationLifecycleCallbacks";

    public static final String CONTEXT = "android.content.Context";
}
