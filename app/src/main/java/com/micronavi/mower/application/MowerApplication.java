/*********************************************************************
 *
 *  This file is part of the [OPEN_MICRO_MOWER_ANDROID] project.
 *  Licensed under the MIT License for non-commercial purposes.
 *  Author: Brook Li
 *  Email: lguitech@126.com
 *
 *  For more details, refer to the LICENSE file or contact [lguitech@126.com].
 *
 *  Commercial use requires a separate license.
 *
 *  This software is provided "as is", without warranty of any kind.
 *
 *********************************************************************/

package com.micronavi.mower.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import com.micronavi.mower.component.MapDesc;
import com.micronavi.mower.service.BTService;
import java.util.List;

public class MowerApplication extends Application {
    private static final String TAG = "MowerApplication";
    private static Context context;
    @Override
    public void onCreate() {
        String curProcessName = getCurrentProcessName(this);
        if (!curProcessName.equals(getPackageName())) {
            return;
        }

        super.onCreate();
        context = getApplicationContext();

        MapDesc.getInstance().initMapData();

        Intent startIntent=new Intent(MowerApplication.this, BTService.class);
        startService(startIntent);

    }

    public String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    public static Context getContext()
    {
        return context;
    }
}
