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
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.micronavi.mower.R;
import com.micronavi.mower.bean.ChassisInfo;
import com.micronavi.mower.bean.GeoPoint;
import com.micronavi.mower.bean.LocationInfo;
import com.micronavi.mower.component.DrawSurfaceView;
import com.micronavi.mower.component.MapDesc;
import com.micronavi.mower.component.MapDrawMng;
import com.micronavi.mower.service.BTService;


public class RoutingActivity extends Activity implements ServiceConnection, View.OnClickListener {
    private static final String TAG = "routing";

    private BTService mService = null;
    private DrawSurfaceView drawView;

    private ImageView ivGNSSState;
    private TextView tvMowerState;


    private ImageView btnZoomIn;
    private ImageView btnZoomOut;
    private ImageView btnZoomMax;

    private Button btnResetState;
    private Button btnStartWork;
    private Button btnStopWork;
    private Button btnGoHome;

    private int robot_state = ChassisInfo.ROBOT_STATE_UNKNOWN;

    private Thread threadLocation = null;
    private boolean quitTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing);
        drawView = findViewById(R.id.dv_mapping);
        drawView.setDrawMapType(MapDrawMng.DRAW_MAP_TYPE_WORKING);

        ivGNSSState = findViewById(R.id.iv_location_status);
        tvMowerState = findViewById(R.id.tv_mower_state);

        Intent serviceIntent = new Intent(this, BTService.class);
        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);

        MapDesc.getInstance().resetWorkLocation();
        initZoomButton();
        initOperateButton();
        initThread();
    }
    @Override
    protected void onDestroy()
    {
        unInitThread();
        unbindService(this);
        super.onDestroy();
    }



    private void initZoomButton() {
        btnZoomIn = findViewById(R.id.iv_zoomin);
        btnZoomOut = findViewById(R.id.iv_zoomout);
        btnZoomMax = findViewById(R.id.iv_zoommax);
        btnZoomIn.setOnClickListener(this);
        btnZoomOut.setOnClickListener(this);
        btnZoomMax.setOnClickListener(this);
    }

    private void initOperateButton() {
        btnResetState = findViewById(R.id.btn_reset_state);
        btnStartWork = findViewById(R.id.btn_start_working);
        btnStopWork = findViewById(R.id.btn_stop_working);
        btnGoHome = findViewById(R.id.btn_home);

        btnResetState.setOnClickListener(this);
        btnStartWork.setOnClickListener(this);
        btnStopWork.setOnClickListener(this);
        btnGoHome.setOnClickListener(this);
   }



    private void onReceived_LocationInfo(LocationInfo info) {
        if (!info.isExpired() && (info.loc_index != LocationInfo.LOCATION_STATE_INVALID))
        {
            MapDesc.getInstance().setGnssLocation(new GeoPoint(info.x, info.y));
            if (robot_state != ChassisInfo.ROBOT_STATE_LOCATING) {
                MapDesc.getInstance().addWorkLocation(info.x, info.y);
            }
        }
        runOnUiThread(() -> {
            if (info.isExpired() ||
                info.loc_index == LocationInfo.LOCATION_STATE_INVALID ||
                info.loc_index == LocationInfo.LOCATION_STATE_SP)
            {
                ivGNSSState.setImageResource(R.drawable.gnss_red);
            }
            else if (info.loc_index == LocationInfo.LOCATION_STATE_FLOAT)
            {
                ivGNSSState.setImageResource(R.drawable.gnss_yellow);
            }
            else if (info.loc_index == LocationInfo.LOCATION_STATE_FIX)
            {
                ivGNSSState.setImageResource(R.drawable.gnss_lightgreen);
            }

        });
    }


    private String parseRobotState(ChassisInfo info)
    {
        String strResult = "";
        switch(info.robot_state) {
            case ChassisInfo.ROBOT_STATE_UNKNOWN:
                strResult = getString(R.string.Unknown);
                break;
            case ChassisInfo.ROBOT_STATE_IDLE:
                strResult = getString(R.string.MowerStatusIdle);
                break;
            case ChassisInfo.ROBOT_STATE_WORKING:
                strResult = getString(R.string.MowerStatusMowing);
                break;
            case ChassisInfo.ROBOT_STATE_PAUSED:
                strResult = getString(R.string.MowerStatusPaused);
                break;
            case ChassisInfo.ROBOT_STATE_STUCK:
                strResult = getString(R.string.MowerStatusStuck);
                break;
            case ChassisInfo.ROBOT_STATE_FAULT:
                strResult = getString(R.string.MowerStatusFault);
                break;
            case ChassisInfo.ROBOT_STATE_STRUGGLING:
                strResult = getString(R.string.MowerStatusStruggling);
                break;
            case ChassisInfo.ROBOT_STATE_LOCATING:
                strResult = getString(R.string.MowerStatusLocating);
                break;
            case ChassisInfo.ROBOT_STATE_TRANSFERRING:
                strResult = getString(R.string.MowerStatusTransferring);
                break;
            case ChassisInfo.ROBOT_STATE_DOCKING:
                strResult = getString(R.string.MowerStatusDocking);
                break;
            case ChassisInfo.ROBOT_STATE_GOINGHOME:
                strResult = getString(R.string.MowerStatusReturnDock);
                break;
        }
        return strResult;
    }
    int counter = 0;
    private void onReceived_ChassisInfo(ChassisInfo info)
    {
        runOnUiThread(() -> {
            if (info.isExpired()) {
                tvMowerState.setText(String.format("%s: %s", getString(R.string.MowerStatus), getString(R.string.Unknown)));
                return;
            }
            robot_state = info.robot_state;
            StringBuilder sbState = new StringBuilder();
            sbState.append(getString(R.string.MowerStatus)).append(": ").append(parseRobotState(info));

            if (info.robot_state == ChassisInfo.ROBOT_STATE_WORKING) {
                counter = (counter + 1) % 5;
                //StringBuilder strPrompt = new StringBuilder();
                for (int i=0; i<counter; i++) {
                    sbState.append(". ");
                }
            }

            tvMowerState.setText(sbState.toString());

        });
    }


    private void  initThread()
    {
        if (threadLocation  != null) {
            return;
        }
        quitTag = true;

        threadLocation = new Thread(() -> {
            LocationInfo infoLocation = new LocationInfo();
            ChassisInfo infoChassis = new ChassisInfo();
            while(quitTag) {
                //get location
                if (mService != null) {
                    mService.getLocationInfo(infoLocation);
                    onReceived_LocationInfo(infoLocation);
                    mService.getChassisInfo(infoChassis);
                    onReceived_ChassisInfo(infoChassis);

                    drawView.calc_and_redraw();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
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
        } catch (InterruptedException ignored) { }
        threadLocation = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        BTService.MyBinder myBinder = (BTService.MyBinder) service;
        mService = (BTService) myBinder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    private boolean checkChassisStatus()
    {
        ChassisInfo infoChassis = new ChassisInfo();
        if (mService != null) {
            mService.getChassisInfo(infoChassis);
            return !infoChassis.isExpired();
        }
        else {
            return false;
        }
    }

    private void showToast(String text) {
        runOnUiThread(() -> {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        });
    }


    @Override
    public void onClick(View v) {
        if (v == btnZoomIn) {
            drawView.zoomIn();
        }
        else if (v == btnZoomOut) {
            drawView.zoomOut();
        }
        else if (v == btnZoomMax) {
            drawView.fit_rect(MapDesc.getInstance().getBoundingBox());
        }
        else if (v == btnStartWork) {
            if (checkChassisStatus()) {
                mService.sendStartWork();
                showToast(getString(R.string.SendCommandSuccess));
            }
            else{
                showToast(getString(R.string.SendCommandFail));
            }
        }
        else if (v == btnStopWork) {
            if (checkChassisStatus()) {
                mService.sendStopWork();
                showToast(getString(R.string.SendCommandSuccess));
            }
            else{
                showToast(getString(R.string.SendCommandFail));
            }
        }
        else if (v == btnGoHome) {
            if (checkChassisStatus()) {
                mService.sendGoHome();
                showToast(getString(R.string.SendCommandSuccess));
            }
            else{
                showToast(getString(R.string.SendCommandFail));
            }
        }
        else if (v == btnResetState) {
            if (checkChassisStatus()) {
                mService.sendResetState();
                MapDesc.getInstance().resetWorkLocation();
            }
            else {
                showToast(getString(R.string.SendCommandFail));
            }
        }
    }

}