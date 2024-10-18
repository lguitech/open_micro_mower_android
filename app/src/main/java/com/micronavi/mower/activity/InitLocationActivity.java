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

package com.micronavi.mower.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.micronavi.mower.R;
import com.micronavi.mower.bean.LocationInfo;
import com.micronavi.mower.component.JoystickView;
import com.micronavi.mower.service.BTService;
import java.text.DecimalFormat;

import static java.lang.Thread.sleep;

public class InitLocationActivity extends Activity implements ServiceConnection, View.OnClickListener {
    private static final String TAG = "InitLocationActivity";
    private Thread threadLocation = null;
    private boolean quitTag = false;
    private BTService mService;

    private TextView txtViewLocStateMemo;
    private TextView txtViewLocMemoValue;
    private TextView txtViewSatNum;
    private TextView txtViewLongitude;
    private TextView txtViewLatitude;
    private TextView txtViewYaw;
    private ImageView ivGoStraight;


    private JoystickView joystick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = getLayoutInflater();
        View rootView = inflater.inflate(R.layout.activity_init_location, null, false);

        initView(rootView);
        initJoyStick(rootView);

        setContentView(rootView);

        Intent serviceIntent = new Intent(this , BTService.class);
        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);

        initThread();
    }
    @Override
    protected void onDestroy()
    {
        stopTest();
        stopGoStraight();
        unInitThread();
        unbindService(this);
        super.onDestroy();
    }

    private void initView(View rootView)
    {
        txtViewLocStateMemo = rootView.findViewById(R.id.txtLocMemo);
        txtViewLocMemoValue = rootView.findViewById(R.id.txtLocMemoValue);

        txtViewSatNum = rootView.findViewById(R.id.txtSatNumValue);

        txtViewLongitude = rootView.findViewById(R.id.txtLongitudeValue);
        txtViewLatitude = rootView.findViewById(R.id.txtLatitudeValue);
        txtViewYaw = rootView.findViewById(R.id.txtYawValue);

        ivGoStraight = rootView.findViewById(R.id.ivGoStraight);
        ivGoStraight.setOnClickListener(this);

    }

    private void initJoyStick(View rootView)
    {

        joystick =  rootView.findViewById(R.id.joystickView);

        joystick.setOnJoystickMoveListener((angle, power, direction) -> {
            if (mService != null) {
                angle = 90 - angle;
                if (angle < 0) {
                    angle += 360;
                }
                else if (angle > 360) {
                    angle -=360;
                }
                mService.sendRemoteControl(angle, power);
            }

        },JoystickView.DEFAULT_LOOP_INTERVAL);
    }

    private void initThread()
    {
        if (threadLocation != null) {
            return;
        }
        quitTag = true;

        threadLocation = new Thread(() -> {
            LocationInfo info = new LocationInfo();
            while(quitTag) {
                //get location
                if (mService != null) {
                    mService.getLocationInfo(info);
                    onReceived_LocationInfo(info);
                }
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        threadLocation.start();
    }

    private void unInitThread()
    {
        quitTag = false;
        try {
            threadLocation.interrupt();
            threadLocation.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadLocation = null;
    }

    private String getMemoByIndex(int index)
    {
        String strMemo = "";
        switch (index) {
            case LocationInfo.LOCATION_STATE_INVALID:
                strMemo = getString(R.string.Invalid);
                break;
            case LocationInfo.LOCATION_STATE_SP:
                strMemo = getString(R.string.SinglePoint);
                break;
            case LocationInfo.LOCATION_STATE_FLOAT:
                strMemo = getString(R.string.FloatPoint);
                break;
            case LocationInfo.LOCATION_STATE_FIX:
                strMemo = getString(R.string.Fixed);
                break;
            default:
                break;
        }
        return strMemo;
    }

    private int getBackColorByIndex(int index)
    {
        int colorBack = 0;
        if (index == LocationInfo.LOCATION_STATE_INVALID ||
                index == LocationInfo.LOCATION_STATE_SP
        )
        {
            colorBack = Color.rgb(255, 0, 0);
        }
        else if (index == LocationInfo.LOCATION_STATE_FLOAT) {
            colorBack = Color.rgb(255,255,0);
        }
        else if (index == LocationInfo.LOCATION_STATE_FIX) {
            colorBack = Color.rgb(0,255,0);
        }
        return colorBack;
    }
    private int getFrontColorByIndex(int index)
    {
        int colorFront = 0;
        if (index == LocationInfo.LOCATION_STATE_INVALID ||
                index == LocationInfo.LOCATION_STATE_SP
        )
        {
            colorFront = Color.rgb(255,255,255);
        }
        else if (index == LocationInfo.LOCATION_STATE_FLOAT) {
            colorFront = Color.rgb(0,0,0);
        }
        else if (index == LocationInfo.LOCATION_STATE_FIX) {
            colorFront = Color.rgb(0,0,0);
        }
        return colorFront;
    }



    private void onReceived_LocationInfo(LocationInfo info) {
        runOnUiThread(() -> {
            String strMemoLoc = "";
            if (info.isExpired()) {
                strMemoLoc = getString(R.string.DataExpired);
                info.loc_index = LocationInfo.LOCATION_STATE_INVALID;
            }
            else {
                strMemoLoc = getMemoByIndex(info.loc_index);
            }

            txtViewLocMemoValue.setText(strMemoLoc);

            int colorBack = getBackColorByIndex(info.loc_index);
            int colorFront = getFrontColorByIndex(info.loc_index);

            txtViewLocStateMemo.setBackgroundColor(colorBack);
            txtViewLocStateMemo.setTextColor(colorFront);
            txtViewLocMemoValue.setBackgroundColor(colorBack);
            txtViewLocMemoValue.setTextColor(colorFront);


            txtViewSatNum.setText(String.valueOf(info.sat_num));

            DecimalFormat format = new DecimalFormat("#.00000000");
            String str = format.format(info.x);
            txtViewLongitude.setText(str);
            str = format.format(info.y);
            txtViewLatitude.setText(str);

            format = new DecimalFormat("#.0");
            str = format.format(Math.toDegrees(info.yaw));
            txtViewYaw.setText(str);
        });
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        BTService.MyBinder myBinder = (BTService.MyBinder) service;
        mService = (BTService) myBinder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    private boolean isGoStraight = false;
    private Thread threadGoStraight = null;
    private void startGoStraight()
    {
        if (isGoStraight) {
            return;
        }
        isGoStraight = true;
        threadGoStraight = new Thread(() -> {
            while(isGoStraight) {
                if (mService != null) {
                    mService.sendRemoteControl(90, 60);
                }
                try {
                    Thread.sleep(50);
                } catch (Exception ignored ) {}
            }
        });
        threadGoStraight.start();

        ivGoStraight.setBackgroundResource(R.drawable.background_pressed);
    }
    private void stopGoStraight()
    {
        if (!isGoStraight) {
            return;
        }
        for (int i=0; i< 5; i++) {
            mService.sendRemoteControl(0, 0);
            try {
                Thread.sleep(20);
            } catch (Exception ignored) {}
        }

        isGoStraight = false;
        try {
            threadGoStraight.interrupt();
            threadGoStraight.join();
            threadGoStraight = null;
        }
        catch (Exception ignored){}

        ivGoStraight.setBackgroundResource(R.drawable.background_normal);
    }

    private boolean inTest = false;
    private Thread threadTest = null;

    private void startTest()
    {
        if (inTest) {
            return;
        }
        inTest = true;
        threadTest = new Thread(() -> {
            while (inTest) {
                if (mService != null) {
                    mService.sendRemoteControl(90, 100);
                }
                try {
                    Thread.sleep(50);
                } catch (Exception ignored) {}
            }
        });
        threadTest.start();

    }

    private void stopTest()
    {
        if (!inTest) {
            return;
        }
        inTest = false;
        try {
            threadTest.interrupt();
            threadTest.join();
        } catch (Exception ignored) {   }

    }
    @Override
    public void onClick(View view) {
        if (view == ivGoStraight) {
            if (isGoStraight) {
                stopGoStraight();
            }
            else {
                startGoStraight();
            }
        }
     }
}