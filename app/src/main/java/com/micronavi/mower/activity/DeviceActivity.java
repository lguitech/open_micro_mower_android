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

import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.micronavi.mower.R;
import com.micronavi.mower.adapter.DeviceAdapter;
import com.micronavi.mower.bean.DeviceInfo;
import com.micronavi.mower.iface.ConnectCallback;
import com.micronavi.mower.service.BTService;
import java.util.ArrayList;
import java.util.List;

public class DeviceActivity extends Activity implements ConnectCallback, ServiceConnection {
    private static final String TAG = "DeviceActivity";
    private BTService mService;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipRefreshLayout;
    private final List<DeviceInfo> deviceListData = new ArrayList<>();
    private DeviceAdapter listViewAdapter = null;
    private ProgressDialog connectDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        initView();
        initRefresh();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        Intent serviceIntent = new Intent(this, BTService.class);
        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
        this.unregisterReceiver(mReceiver);
    }

    public void showConnectDialog() {
        runOnUiThread(() -> {
            if (connectDialog == null) {
                connectDialog = new ProgressDialog(DeviceActivity.this);
                connectDialog.setMessage("connecting...");
            }
            connectDialog.show();
        });
    }

    public void hideConnectDialog() {
        runOnUiThread(() -> {
            connectDialog.dismiss();
        });
    }

    private void initView() {
        progressBar = findViewById(R.id.toolbar_progress);
        swipRefreshLayout = findViewById(R.id.swipe_layout);
        swipRefreshLayout.setColorSchemeColors(0x01a4ef);

        ListView listView = findViewById(R.id.list_view);
        listViewAdapter = new DeviceAdapter(this, R.layout.list_item, deviceListData);
        listView.setAdapter(listViewAdapter);

        listView.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {
            showConnectDialog();
            String strMacAdd = deviceListData.get(i).addr;
            mService.startConnect(strMacAdd, this);
        });
    }

    private void initRefresh() {
        swipRefreshLayout.setOnRefreshListener(() -> {
            swipRefreshLayout.setRefreshing(false);
            refresh();
        });
    }

    //刷新的具体实现
    private void refresh() {
        if (mService != null && !mService.isDiscovering()) {
            deviceListData.clear();
            listViewAdapter.notifyDataSetChanged();
            mService.startDiscovery();
            progressBar.setVisibility(View.VISIBLE);
        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(DeviceActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        return;
                    }
                }

                int type = device.getType();
                Log.d(TAG, device.getName() + "," + device.getType());
                if (type == BluetoothDevice.DEVICE_TYPE_CLASSIC || type == BluetoothDevice.DEVICE_TYPE_DUAL) {
                    //只扫描经典蓝牙设备
                    String strName = device.getName();
                    if (strName != null) {
                        if (strName.length() > 13) {
                            strName = strName.substring(0, 10) + "...";
                        }
                        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                            strName += "(" + getString(R.string.BTNotPaired) + ")";
                        } else {
                            strName += "(" + getString(R.string.BTPaired) + ")";
                        }

                        int rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                        String strAddr = device.getAddress();
                        DeviceInfo devInfo = new DeviceInfo(strAddr, strName, rssi);
                        deviceListData.add(devInfo);
                        listViewAdapter.notifyDataSetChanged();
                    }

               }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
    };


    public void showToast(String text){
        runOnUiThread(()->{
            Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void cb_btConnect(boolean ok, int errCode) {
        hideConnectDialog();
        if (ok) {
            showToast(getString(R.string.BTConnectSuccess));
            DeviceActivity.this.finish();
        }
        else {
            showToast(getString(R.string.BTConnectFailed));
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        BTService.MyBinder myBinder = (BTService.MyBinder) service;

        mService = (BTService) myBinder.getService();
        mService.startDiscovery();
        progressBar.setVisibility(View.VISIBLE);

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}