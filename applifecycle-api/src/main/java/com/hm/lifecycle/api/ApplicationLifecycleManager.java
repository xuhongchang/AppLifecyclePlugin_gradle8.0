package com.hm.lifecycle.api;

import android.content.Context;
import android.text.TextUtils;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by hjy on 2018/10/23.
 */

public class ApplicationLifecycleManager {

    private static List<IApplicationLifecycleCallbacks> lifecycleCallbacks = new ArrayList<>();

    /**
     * 改方法内容会在编译时写入
     * （找到@AppLifecycle注解的类 对应的代理类后）调用registerApplicationLifecycleCallbacks
     */
    public static void init() {
    }

    public static void registerApplicationLifecycleCallbacks(IApplicationLifecycleCallbacks appLifecycleCallbacks) {
        lifecycleCallbacks.add(appLifecycleCallbacks);
    }

    public static void registerApplicationLifecycleCallbacks(String appLifecycleClassName) {
        if (TextUtils.isEmpty(appLifecycleClassName)) {
            return;
        }
        try {
            Object object = Class.forName(appLifecycleClassName).getConstructor().newInstance();
            if (object instanceof IApplicationLifecycleCallbacks) {
                registerApplicationLifecycleCallbacks((IApplicationLifecycleCallbacks) object);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void onCreate(Context context) {
        if (lifecycleCallbacks.isEmpty()) {
            return;
        }
        Collections.sort(lifecycleCallbacks, new ApplicationLifecycleComparator());
        for (IApplicationLifecycleCallbacks callbacks : lifecycleCallbacks) {
            callbacks.onCreate(context);
        }
    }

    public static void onTerminate() {
        if (lifecycleCallbacks.isEmpty()) {
            return;
        }
        for (IApplicationLifecycleCallbacks callbacks : lifecycleCallbacks) {
            callbacks.onTerminate();
        }
    }

    public static void onLowMemory() {
        if (lifecycleCallbacks.isEmpty()) {
            return;
        }
        for (IApplicationLifecycleCallbacks callbacks : lifecycleCallbacks) {
            callbacks.onLowMemory();
        }
    }

    public static void onTrimMemory(int level) {
        if (lifecycleCallbacks.isEmpty()) {
            return;
        }
        for (IApplicationLifecycleCallbacks callbacks : lifecycleCallbacks) {
            callbacks.onTrimMemory(level);
        }
    }

    /**
     * 优先级比较器，优先级大的排在前面
     */
    private static class ApplicationLifecycleComparator implements Comparator<IApplicationLifecycleCallbacks> {

        @Override
        public int compare(IApplicationLifecycleCallbacks o1, IApplicationLifecycleCallbacks o2) {
            int p1 = o1.getPriority();
            int p2 = o2.getPriority();
            return p2 - p1;
        }
    }
}
