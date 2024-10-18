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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.micronavi.mower.R;
import com.micronavi.mower.adapter.ObjListAdapter;
import com.micronavi.mower.bean.BoundingBox;
import com.micronavi.mower.bean.ChassisInfo;
import com.micronavi.mower.bean.GeoPoint;
import com.micronavi.mower.bean.LocationInfo;
import com.micronavi.mower.component.DrawSurfaceView;
import com.micronavi.mower.component.JoystickView;
import com.micronavi.mower.component.MapCheck;
import com.micronavi.mower.component.MapDataPacker;
import com.micronavi.mower.component.MapDesc;
import com.micronavi.mower.component.MapDrawMng;
import com.micronavi.mower.component.MapObject;
import com.micronavi.mower.service.BTService;

public class MappingActivity extends Activity  implements ServiceConnection, View.OnClickListener {
    private static final String TAG = "MappingActivity";

    private static final int STATE_STOPPED = 1001;
    private static final int STATE_MAPPING = 1003;

    private static final int STATE_COLOR_STOPPED = Color.rgb(255, 255, 255);
    private static final int STATE_COLOR_MAPPING = Color.rgb(255, 255, 255);

    private BTService mService;

    private RelativeLayout outerRelativeLayout;
    private RelativeLayout outerRelativeLayoutEnlarge;

    private int stateOperate = STATE_STOPPED;
    private ObjListAdapter adapter;
    private DrawSurfaceView drawView;
    private TextView tv_mapping_state;
    private MapObject objectMapping = null;

    private Button btnStart;
    private Button btnCheck;
    private Button btnSend;
    private ImageView btnZoomIn;
    private ImageView btnZoomOut;
    private ImageView btnZoomMax;
    private ImageView iv_gnss_status;

    private Thread threadLocation = null;
    private boolean quitTag = false;
    private boolean firstLocationPoint = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);

        outerRelativeLayout = findViewById(R.id.outerRelativeLayout);
        outerRelativeLayoutEnlarge = findViewById(R.id.outerRelativeLayoutEnlarge);

        ImageView triangleButton = findViewById(R.id.triangleButton);
        ImageView triangleButtonEnlarge = findViewById(R.id.triangleButtonEnlarge);

        triangleButton.setOnClickListener(v -> toggleLarger());

        triangleButtonEnlarge.setOnClickListener(v -> toggleSmaller());

        drawView = findViewById(R.id.dv_mapping);

        drawView.setDrawMapType(MapDrawMng.DRAW_MAP_TYPE_MAPPING);

        tv_mapping_state = findViewById(R.id.tv_mapping_state);
        tv_mapping_state.setText(String.format("%s: %s", getString(R.string.MappingStatus),
                getString(R.string.MappingStatusStopped)));
        iv_gnss_status = findViewById(R.id.iv_gnss_status);

        //绑定service;
        Intent serviceIntent = new Intent(this , BTService.class);
        //如果未绑定，则进行绑定
        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);


        initZoomButton();
        initOperateButton();
        initJoyStick();
        initObjList();
        initThread();

        outerRelativeLayoutEnlarge.setOnClickListener(view -> {

        });
    }


    private void toggleLarger()
    {
        outerRelativeLayout.setVisibility(View.INVISIBLE);
        outerRelativeLayoutEnlarge.setVisibility(View.VISIBLE);
    }

    private void toggleSmaller()
    {
        outerRelativeLayout.setVisibility(View.VISIBLE);
        outerRelativeLayoutEnlarge.setVisibility(View.INVISIBLE);
    }


    @Override
    protected void onDestroy()
    {
        unInitThread();
        unbindService(this);
        super.onDestroy();
    }

    private void addLocationPoint(LocationInfo info)
    {
        if (info.loc_index == LocationInfo.LOCATION_STATE_FIX ||
            info.loc_index == LocationInfo.LOCATION_STATE_FLOAT) {

            objectMapping.addPoint(info.x, info.y);
            if (firstLocationPoint) {
                drawView.setCenter_geo(new GeoPoint(info.x, info.y));
                firstLocationPoint = false;
            }
        }
    }
    private void  initThread()
    {
        if (threadLocation  != null) {
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
                    if (stateOperate == STATE_MAPPING) {
                        addLocationPoint(info);
                    }
                    drawView.calc_and_redraw();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        threadLocation.start(); //启动线程
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
    private void onReceived_LocationInfo(LocationInfo info) {
        if (!info.isExpired() && (info.loc_index != LocationInfo.LOCATION_STATE_INVALID))
        {
            MapDesc.getInstance().setGnssLocation(new GeoPoint(info.x, info.y));
        }

        runOnUiThread(() -> {
            if (info.isExpired()||
                    info.loc_index == LocationInfo.LOCATION_STATE_INVALID ||
                    info.loc_index == LocationInfo.LOCATION_STATE_SP)
            {
                iv_gnss_status.setImageResource(R.drawable.gnss_red);
            }
            else if (info.loc_index == LocationInfo.LOCATION_STATE_FLOAT)
            {
                iv_gnss_status.setImageResource(R.drawable.gnss_yellow);
            }
            else if (info.loc_index == LocationInfo.LOCATION_STATE_FIX)
            {
                iv_gnss_status.setImageResource(R.drawable.gnss_lightgreen);
            }
        });
    }

    private void initZoomButton()
    {
        btnZoomIn = findViewById(R.id.iv_zoomin);
        btnZoomOut = findViewById(R.id.iv_zoomout);
        btnZoomMax = findViewById(R.id.iv_zoommax);

        btnZoomIn.setOnClickListener(this);
        btnZoomOut.setOnClickListener(this);
        btnZoomMax.setOnClickListener(this);
    }

    private void initOperateButton()
    {
        btnStart = findViewById(R.id.ibt_start_mapping);
        btnCheck = findViewById(R.id.ibt_check_map);
        btnSend = findViewById(R.id.ibt_send_map);

        btnStart.setOnClickListener(this);
        btnCheck.setOnClickListener(this);
        btnSend.setOnClickListener(this);
    }


    private void initJoyStick()
    {
        JoystickView joystick1 =  findViewById(R.id.joystickView);
        joystick1.setOnJoystickMoveListener((angle, power, direction) -> {
            //Log.i("joystick", angle + "," + power + "," + direction);
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

        JoystickView joystick2 =  findViewById(R.id.joystickViewEnlarge);
        joystick2.setOnJoystickMoveListener((angle, power, direction) -> {
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
    private void initObjList()
    {
        ListView listView = findViewById(R.id.lv_obj);

        adapter = new ObjListAdapter(MapDesc.getInstance().getObjectList(), this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0) {
                MapObject mapObject = (MapObject) adapter.getItem(position);
                BoundingBox boundingBox = mapObject.getBoundingBox();
                if (boundingBox.isValid()) {
                    if (mapObject.getObjType() == MapObject.OBJ_TYPE_ID_HOME) {
                        GeoPoint geoPoint = new GeoPoint(boundingBox.left, boundingBox.top);
                        drawView.setCenter_geo(geoPoint);
                    }
                    else {
                        drawView.fit_rect(boundingBox);
                    }
                }
            }
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (stateOperate == STATE_STOPPED) {
                showPopupMenu(listView, position);
            }
            return true;
        });
    }


    private void showStartDialog() {
        AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(MappingActivity.this);

        customizeDialog.setTitle(getString(R.string.SelectType));
        final String[] options = {getString(R.string.Boundary),
                                  getString(R.string.Channel),
                                  getString(R.string.Obstacle),
                                  getString(R.string.Dock)};

        int[] checkedItem = {0};

        customizeDialog.setSingleChoiceItems(options, checkedItem[0], (dialog, which) -> {
            checkedItem[0] = which;
        });

        customizeDialog.setPositiveButton(getString(R.string.Ok), (dialog, which) -> {
            int selectedOptionIndex = checkedItem[0];
            int objTypeId = 0;
            switch (selectedOptionIndex) {
                case 0:
                    objTypeId = MapObject.OBJ_TYPE_ID_BOUNDARY;
                    break;
                case 1:
                    objTypeId = MapObject.OBJ_TYPE_ID_CHANNEL;
                    break;
                case 2:
                    objTypeId = MapObject.OBJ_TYPE_ID_OBSTACLE;
                    break;
                case 3:
                    objTypeId = MapObject.OBJ_TYPE_ID_HOME;
                    break;
            }
            if (objTypeId == MapObject.OBJ_TYPE_ID_HOME && MapDesc.getInstance().hasHomeObj()) {
                showToast(getString(R.string.DockExisted));
            }
            else {
                //加入新的obj
                objectMapping = MapDesc.getInstance().newObject(objTypeId);
                adapter.notifyDataSetChanged();

                stateOperate = STATE_MAPPING;
                firstLocationPoint = true;
                tv_mapping_state.setText(String.format("%s: %s", getString(R.string.MappingStatus),
                        getString(R.string.MappingStatusMapping)));
                tv_mapping_state.setTextColor(STATE_COLOR_MAPPING);

                btnStart.setText(getString(R.string.StopMapping));
                showToast(getString(R.string.StartMapping));
            }
        });

        customizeDialog.setNegativeButton(getString(R.string.Cancel), (dialog, which) -> {
            // Handle Cancel button click
        });

        AlertDialog dialog = customizeDialog.create();
        dialog.show();

    }
    private void showStopDialog(){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MappingActivity.this);
        normalDialog.setTitle(getString(R.string.StopMapping));
        normalDialog.setMessage(getString(R.string.ConfirmToEnd));
        normalDialog.setPositiveButton(getString(R.string.Ok),
                (dialog, which) -> {
                    stateOperate = STATE_STOPPED;
                    tv_mapping_state.setText(String.format("%s: %s", getString(R.string.MappingStatus),
                            getString(R.string.MappingStatusStopped)));
                    ;
                    tv_mapping_state.setTextColor(STATE_COLOR_STOPPED);
                    StringBuilder sbResult = new StringBuilder();
                    boolean result = objectMapping.simpleObjFilter(sbResult);

                    if (result) {
                        MapDesc.getInstance().writeDataToFile();
                        showToast(getString(R.string.StopMapping));
                    } else {
                        showToast(sbResult.toString());
                        //删除无效的内容
                        MapDesc.getInstance().delObject(objectMapping);
                        adapter.notifyDataSetChanged();
                        //地图需要刷新
                        drawView.calc_and_redraw();
                    }
                    btnStart.setText(getString(R.string.StartMapping));
                }).setNegativeButton(getString(R.string.Cancel),
                (dialog, which) -> {
                    //...To-do
                });
        normalDialog.show();
    }


    private void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(MappingActivity.this, view);

        popupMenu.getMenuInflater().inflate(R.menu.list_menu, popupMenu.getMenu());

        MenuItem deleteItem = popupMenu.getMenu().findItem(R.id.popup_delete);
        SpannableString spanString = new SpannableString(getString(R.string.Delete));
        spanString.setSpan(new ForegroundColorSpan(Color.RED), 0, spanString.length(), 0);
        deleteItem.setTitle(spanString);


        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.popup_delete) {
                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(MappingActivity.this);
                normalDialog.setTitle(getString(R.string.DataWillNotBeRecoverable));
                normalDialog.setMessage(getString(R.string.ConfirmToDelete));
                normalDialog.setPositiveButton(getString(R.string.Ok),
                        (dialog, which) -> {
                            //...To-do
                            MapObject mapObject = (MapObject) adapter.getItem(position);
                            MapDesc.getInstance().delObject(mapObject);
                            MapDesc.getInstance().writeDataToFile();
                            MappingActivity.this.adapter.notifyDataSetChanged();
                            drawView.calc_and_redraw();
                        });
                normalDialog.setNegativeButton(getString(R.string.Cancel),
                        (dialog, which) -> {

                        });

                normalDialog.show();
            }

            return true;
        });
        popupMenu.setOnDismissListener(menu -> {

        });
    }

    private String doCheck()
    {
        String strResult = MapCheck.doCheck(this);

        if (strResult.equalsIgnoreCase("")) {
            MapDesc.getInstance().setCheckFinished(true);
            return getString(R.string.Passed);
        }
        else {
            return strResult;
        }
    }

    private void showCheck(String strResult)
    {
        runOnUiThread(() -> {
            AlertDialog dialog = new AlertDialog.Builder(MappingActivity.this)
                    .setIcon(R.drawable.m_icon2)
                    .setTitle(getString(R.string.MapCheckResult))
                    .setMessage(strResult)
                    //设置对话框的按钮
                    .setNegativeButton(getString(R.string.Ok), (dialog1, which) -> {
                        dialog1.dismiss();

                        MapDesc.getInstance().writeDataToFile();
                        MappingActivity.this.adapter.notifyDataSetChanged();

                    }).create();
            dialog.show();
        });

    }
    private void onMapCheck()
    {
        ProgressDialog waitingDialog =
                new ProgressDialog(MappingActivity.this);
        waitingDialog.setTitle(getString(R.string.MapCheck));
        waitingDialog.setMessage(getString(R.string.Waiting));
        waitingDialog.setIndeterminate(true);
        waitingDialog.setCancelable(false);
        waitingDialog.show();
        Thread threadCheck = new Thread(() -> {
            String strResult = doCheck();
            waitingDialog.cancel();
            showCheck(strResult);
        });
        threadCheck.start();
    }



    private boolean checkChassisConnected()
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
    private boolean checkChassisIdle()
    {
        ChassisInfo infoChassis = new ChassisInfo();
        if (mService != null) {
            mService.getChassisInfo(infoChassis);
            return infoChassis.robot_state == ChassisInfo.ROBOT_STATE_IDLE;
        }
        else {
            return false;
        }
    }

        private void reqSendMap()
    {
        boolean checkFinished = MapDesc.getInstance().getCheckFinished();
        if (!checkFinished) {
            showToast(getString(R.string.MapCheckNotFinished));
            return;
        }

        if (mService == null) {
            showToast(getString(R.string.SendMapFail));
            return;
        }

        final int MAX_PROGRESS = 100;
        final ProgressDialog progressDialog =
                new ProgressDialog(MappingActivity.this);
        progressDialog.setProgress(0);
        progressDialog.setTitle(getString(R.string.SendProgress));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(MAX_PROGRESS);
        progressDialog.setCancelable(false);
        progressDialog.show();


        Thread threadSendRoute = new Thread(() -> {
            progressDialog.show();

            MapDataPacker.getInstance().setMapData(MapDesc.getInstance().convertToString());
            int numTotal = MapDataPacker.getInstance().getDataLen();

            mService.sendMapInfo(numTotal);

            boolean isSuccess = true;
            while (true) {

                int[] arrIndex = mService.getReqRouteIndex();

                if (arrIndex == null) {
                    isSuccess = false;
                    //超时失败了
                    break;
                }
                int startIndex = arrIndex[0];
                int endIndex = arrIndex[1];

                if (startIndex == -1 && endIndex == -1) {
                    isSuccess = true;
                    break;
                }

                mService.sendMapData(startIndex, endIndex);
                progressDialog.setProgress(startIndex * 100 / numTotal);
            }
            progressDialog.cancel();

            if (isSuccess) {
                showToast(getString(R.string.SendMapSuccess));
            } else {
                showToast(getString(R.string.SendMapFail));
            }

        });
        threadSendRoute.start();

    }

    private void showToast(String text){
        runOnUiThread(()->{
            Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
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

    @Override
    public void onClick(View v) {

        if (v == btnCheck) {
            if (stateOperate != STATE_STOPPED) {
                showToast(getString(R.string.StopMappinngFirst));
                return;
            }
            onMapCheck();
        }
        else if (v == btnSend) {
            if (stateOperate != STATE_STOPPED) {
                showToast(getString(R.string.StopMappinngFirst));
                return;
            }

            if (checkChassisConnected()) {
                reqSendMap();
            }
            else {
                showToast(getString(R.string.StatusUnKnownCantSendCmd));
            }
        }
        else if (v == btnZoomIn) {
            drawView.zoomIn();
        }
        else if (v == btnZoomOut) {
            drawView.zoomOut();
        }
        else if (v == btnZoomMax) {
            drawView.fit_rect(MapDesc.getInstance().getBoundingBox());
        }
        else if(v == btnStart) {
            if (stateOperate == STATE_MAPPING) {
                showStopDialog();
            }
            else {
                if (!checkChassisConnected()) {
                    showToast(getString(R.string.StatusUnKnownCantSendCmd));
                }
                else if (!checkChassisIdle()) {
                    showToast(getString(R.string.MowerNotIdle));
                }
                else {
                    showStartDialog();
                }
            }
        }
    }
}