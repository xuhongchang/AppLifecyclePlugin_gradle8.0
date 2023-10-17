package com.example.module_1;

import android.content.Context;
import android.util.Log;

import com.hm.iou.lifecycle.annotation.AppLifecycle;
import com.hm.lifecycle.api.IApplicationLifecycleCallbacks;

/**
 * Created by hjy on 2018/10/23.
 */
@AppLifecycle
public class ModuleDApplicationLifecycleCallbacks implements IApplicationLifecycleCallbacks {

    @Override
    public int getPriority() {
        return 7;
    }

    @Override
    public void onCreate(Context context) {
        Log.d("AppLifecycle", "onCreate(): this is in ModuleDApplicationLifecycleCallbacks.");
    }

    @Override
    public void onTerminate() {
        Log.d("AppLifecycle", "onTerminate(): this is in ModuleDApplicationLifecycleCallbacks.");
    }

    @Override
    public void onLowMemory() {

    }

    @Override
    public void onTrimMemory(int level) {

    }
}
