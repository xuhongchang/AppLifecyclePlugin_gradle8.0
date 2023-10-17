package com.hm.iou.lifecycle.plugin;

  object GlobalConfig {

    const val PROXY_CLASS_PREFIX = "AppLife\$\$"
    const val PROXY_CLASS_SUFFIX = "\$\$Proxy.class"

    const val PROXY_CLASS_PACKAGE_NAME = "com/hm/iou/lifecycle/apt/proxy"

    const val REGISTER_CLASS_FILE_NAME = "com/hm/iou/lifecycle/api/ApplicationLifecycleManager.class"

    const val INJECT_CLASS_NAME = "com/hm/iou/lifecycle/api/ApplicationLifecycleManager"
    const val INJECT_METHOD_NAME = "registerApplicationLifecycleCallbacks"
    const val INJECT_PARAMS_DESC = "(Ljava/lang/String;)V"

}
