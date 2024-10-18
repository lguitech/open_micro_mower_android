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

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.micronavi.mower.R;
import com.micronavi.mower.bean.ChassisInfo;
import com.micronavi.mower.bean.LocationInfo;
import com.micronavi.mower.service.BTService;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements ServiceConnection, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private BluetoothBroadcastReceiver bluetoothReceiver;

    private BTService mService = null;

    private TextView txtViewBTState;

    private TextView txtViewLocStateMemo;
    private TextView txtViewLocMemoValue;

    private TextView txtViewSatNum;
    private TextView txtViewLongitude;
    private TextView txtViewLatitude;
    private TextView txtViewYaw;

    private TextView tvRobotState;
    private TextView tvWheelSpeed;
    private TextView tvAutoMode;
    private TextView tvPutterState;
    private TextView tvCutterState;
    private TextView tvStopButtonState;
    private TextView tvSafeEdgeState;
    private TextView tvBatteryState;
    private TextView tvChargeState;
    private TextView tvFrontSonar;
    private TextView tvRearSonar;

    private Button btnBluetooth;
    private Button btnInitLocation;
    private Button btnMapping;
    private Button btnRouting;
    private Button btnAbout;

    public static void requestPermissions(Activity activity) {
        try {
            int permissionCheck = ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionCheck += ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionCheck += ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            permissionCheck += ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_PHONE_STATE);
            permissionCheck += ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.MODIFY_PHONE_STATE);
            permissionCheck += ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.CAMERA);
            permissionCheck += ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.RECORD_AUDIO);
            permissionCheck += ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.BLUETOOTH);
            permissionCheck += ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.BLUETOOTH_ADMIN);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionCheck += ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.BLUETOOTH_SCAN);
                permissionCheck += ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.BLUETOOTH_CONNECT);
            }

            String[] permission = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.MODIFY_PHONE_STATE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };
            List<String> permissionsList = new ArrayList<>(Arrays.asList(permission));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionsList.add(Manifest.permission.BLUETOOTH_SCAN);
                permissionsList.add(Manifest.permission.BLUETOOTH_CONNECT);
            } else {
                permissionsList.add(Manifest.permission.BLUETOOTH);
                permissionsList.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
            String[] allPermissions = permissionsList.toArray(new String[0]);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        allPermissions, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerBTReceiver(BroadcastReceiver receiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(receiver, filter);
    }

    public void unregisterBTReceiver(BroadcastReceiver receiver) {
        unregisterReceiver(receiver);
    }

    public void updateBTState(boolean value) {
        if (value) {
            MainActivity.this.txtViewBTState.setText(R.string.Connected);
        } else {
            MainActivity.this.txtViewBTState.setText(R.string.Disconnected);
        }
    }

    private class BluetoothBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                // 连接成功
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 获取 MainActivity 的实例
                MainActivity mainActivity = (MainActivity) context;
                mainActivity.updateBTState(true);

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                // 连接断开
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 获取 MainActivity 的实例
                MainActivity mainActivity = (MainActivity) context;
                mainActivity.updateBTState(false);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = getLayoutInflater();
        View rootView = inflater.inflate(R.layout.activity_main, null, false);

        initView(rootView);

        setContentView(rootView);

        requestPermissions(this);

        //绑定service;
        Intent serviceIntent = new Intent(this , BTService.class);
        //如果未绑定，则进行绑定
        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);

        bluetoothReceiver = new BluetoothBroadcastReceiver();
        registerBTReceiver(bluetoothReceiver);

        initThread();
    }

    private void initView(View rootView)
    {
        txtViewBTState = rootView.findViewById((R.id.tvBluetoothStateValue));

        txtViewLocStateMemo = rootView.findViewById(R.id.txtLocMemo);
        txtViewLocMemoValue = rootView.findViewById(R.id.txtLocMemoValue);

        txtViewSatNum = rootView.findViewById(R.id.txtSatNumValue);

        txtViewLongitude = rootView.findViewById(R.id.txtLongitudeValue);
        txtViewLatitude = rootView.findViewById(R.id.txtLatitudeValue);
        txtViewYaw = rootView.findViewById(R.id.txtYawValue);

        tvRobotState = rootView.findViewById(R.id.tvProMowerStateValue);
        tvAutoMode = rootView.findViewById(R.id.tvProAutoModeValue);
        tvWheelSpeed = rootView.findViewById(R.id.tvProWheelSpeedValue);
        tvPutterState = rootView.findViewById(R.id.tvProPutterStateValue);
        tvCutterState = rootView.findViewById(R.id.tvProCutterStateValue);
        tvStopButtonState = rootView.findViewById(R.id.tvProStopButtonValue);
        tvSafeEdgeState = rootView.findViewById(R.id.tvProSafeEdgeValue);
        tvBatteryState = rootView.findViewById(R.id.tvProBatteryStateValue);
        tvChargeState = rootView.findViewById(R.id.tvProChargeStateValue);

        tvFrontSonar = rootView.findViewById(R.id.tvFrontSonarStateValue);
        tvRearSonar = rootView.findViewById(R.id.tvtvRearSonarStateValue);

        btnBluetooth = rootView.findViewById(R.id.btnBluetooth);
        btnMapping = rootView.findViewById(R.id.btnMapping);
        btnInitLocation = rootView.findViewById(R.id.btn_init_location);
        btnRouting = rootView.findViewById(R.id.btnRouting);
        btnAbout = rootView.findViewById(R.id.btnAbout);

        btnBluetooth.setOnClickListener(this);
        btnMapping.setOnClickListener(this);
        btnInitLocation.setOnClickListener(this);
        btnRouting.setOnClickListener(this);
        btnAbout.setOnClickListener(this);
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unInitThread();
        unbindService(this);
        unregisterBTReceiver(bluetoothReceiver);

        Log.d(TAG, "MainActivity onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }


    //发送数据请求的线程
    private Thread threadReq = null;
    private boolean quitTag = false;
    private void initThread() {
        quitTag = true;
        threadReq = new Thread(() -> {
            LocationInfo infoLocation = new LocationInfo();
            ChassisInfo infoChassis = new ChassisInfo();
            while(quitTag) {
                if (mService != null) {
                    mService.getLocationInfo(infoLocation);
                    onReceived_LocationInfo(infoLocation);

                    mService.getChassisInfo(infoChassis);
                    onReceived_ChassisInfo(infoChassis);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        threadReq.start();
    }

    private void unInitThread()
    {
        quitTag = false;
        try {
            threadReq.interrupt();
            threadReq.join(1000);
        } catch (InterruptedException ignored) { }

        threadReq = null;

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

    private String parseAutoMode(ChassisInfo info)
    {
        String strResult = "";
        if (info.auto_mode == ChassisInfo.AUTO_MODE_AUTO_CONTROL) {
            strResult = getString(R.string.AutoPilotMode_Auto);
        }
        else {
            strResult = getString(R.string.AutoPilotMode_RemoteCtl);
        }
        return strResult;
    }

    private String parseWheelSpeed(ChassisInfo info)
    {
        String strResult = "";
        strResult = getString(R.string.LeftWheel) +  ": " + info.left_speed + "，" +
                    getString(R.string.RightWheel) + ": " + info.right_speed;
        return strResult;
    }

    private String parsePutter(ChassisInfo info)
    {
        String strResult = "";
        switch (info.putter_state) {
            case ChassisInfo.PUTTER_STATE_TOP:
                strResult = getString(R.string.PutterStatusTop);
                break;
            case ChassisInfo.PUTTER_STATE_BOTTOM:
                strResult = getString(R.string.PutterStatusBottom);
                break;
            case ChassisInfo.PUTTER_STATE_FALLING:
                strResult = getString(R.string.PutterStatusDescending);
                break;
            case ChassisInfo.PUTTER_STATE_RISING:
                strResult = getString(R.string.PutterStatusAscending);
                break;
        }
        return strResult;
    }

    private String parseCutter(ChassisInfo info)
    {
        String strResult = "";
        if (info.cutter_state == ChassisInfo.CUTTER_ROTATING) {
            strResult = getString(R.string.CutterStatusRotate);
        }
        else if (info.cutter_state == ChassisInfo.CUTTER_STOPPED) {
            strResult = getString(R.string.CutterStatusStop);
        }
        else {
            strResult = getString(R.string.Unknown);
        }
        return strResult;
    }
    private String parseStopButton(ChassisInfo info)
    {
        return getString(R.string.EmergencyButtonNotPressed);
    }
    private String parseSafeEdge(ChassisInfo info)
    {
        return getString(R.string.SafetyEdgeStatusNormal);
    }
    private String parseBattery(ChassisInfo info)
    {
        return getString(R.string.BatteryStatusLevel) + ": " + info.soc + "% "  +
               getString(R.string.BatteryStatusVolt)  + ": " + info.battery_volt;
    }
    private String parseCharge(ChassisInfo info)
    {
        String strResult;
        if (info.battery_charge_state == ChassisInfo.BATTERY_NO_CHARGING) {
            strResult = getString(R.string.ChargingStatusNotCharging);
        }
        else if (info.battery_charge_state == ChassisInfo.BATTERY_CHARGING) {
            strResult = getString(R.string.ChargingStatusCharging);
        }
        else {
            strResult = getString(R.string.Unknown);
        }
        return strResult;
    }
    private String parseFrontSonar(ChassisInfo info)
    {
        return info.sonar_front_left + "/" + info.sonar_front_center + "/" + info.sonar_front_right;
    }

    private String parseRearSonar(ChassisInfo info)
    {
        return info.sonar_rear_left + "/" + info.sonar_rear_center + "/" + info.sonar_rear_right;
    }

    private  void parseSetAllState(ChassisInfo info)
    {
        tvRobotState.setText(parseRobotState(info));
        tvAutoMode.setText(parseAutoMode(info));
        tvWheelSpeed.setText(parseWheelSpeed(info));
        tvPutterState.setText(parsePutter(info));
        tvCutterState.setText(parseCutter(info));
        tvStopButtonState.setText(parseStopButton(info));
        tvSafeEdgeState.setText(parseSafeEdge(info));
        tvBatteryState.setText(parseBattery(info));
        tvChargeState.setText(parseCharge(info));

        tvFrontSonar.setText(parseFrontSonar(info));
        tvRearSonar.setText(parseRearSonar(info));
    }

    private void setAllNoUpdate()
    {
        String strValue = getString(R.string.Unknown);
        tvRobotState.setText(strValue);
        tvAutoMode.setText(strValue);
        tvWheelSpeed.setText(strValue);
        tvPutterState.setText(strValue);
        tvCutterState.setText(strValue);
        tvStopButtonState.setText(strValue);
        tvSafeEdgeState.setText(strValue);
        tvBatteryState.setText(strValue);
        tvChargeState.setText(strValue);
    }

    private void onReceived_ChassisInfo(ChassisInfo info)
    {
        runOnUiThread(() -> {
            if ( info.isExpired()) {
                setAllNoUpdate();
            }
            else {
                parseSetAllState(info);
            }
        });
    }

    private long lastClickTime = 0;
    private static final long COOLDOWN_TIME = 1000;

    @Override
    public void onClick(View v) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < COOLDOWN_TIME) {
            return;
        }
        lastClickTime = currentTime;

        if (v == btnBluetooth) {
            Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
            startActivity(intent);
        }
        else if (v == btnInitLocation) {
            Intent intent = new Intent(MainActivity.this, InitLocationActivity.class);
            startActivity(intent);
        }

        else if (v == btnMapping) {
            Intent intent = new Intent(MainActivity.this, MappingActivity.class);
            startActivity(intent);
        }
        else if (v == btnAbout) {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        }
        else if (v == btnRouting) {
            Intent intent = new Intent(MainActivity.this, RoutingActivity.class);
            startActivity(intent);
        }
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        BTService.MyBinder myBinder = (BTService.MyBinder) service;
        mService = (BTService) myBinder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "PERMISSION_GRANTED");
            }
        }
    }


}