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

package com.micronavi.mower.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Binder;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import com.micronavi.mower.bean.ChassisInfo;
import com.micronavi.mower.bean.LocationInfo;
import com.micronavi.mower.component.MowerData;
import com.micronavi.mower.iface.ConnectCallback;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class BTService extends Service {
    private static final String TAG = "btservice";
    private static final int NOTIFICATION_ID = 1;
    private final Handler mTimeHandler = new Handler();
    private final MyBinder mBinder = new MyBinder();
    private boolean scan_state = false;
    private BluetoothAdapter mBtAdapter;
    private BluetoothSocket mBluetoothSocket = null;
    private InputStream mInputStream = null;
    private OutputStream mOutputStream = null;
    private Thread threadRead = null;
    private boolean tagReadThread = false;

    private final MowerData mowerData = new MowerData();

    public class MyBinder extends Binder {
        public Service getService() {
            return BTService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopConnect();
    }

    private void init() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    public boolean isDiscovering() {
        return scan_state;
    }

    @SuppressLint("MissingPermission")
    public void startDiscovery() {
        if (scan_state) {
            return;
        }
        stopConnect();

        mBtAdapter.startDiscovery();
        scan_state = true;
        mTimeHandler.postDelayed(() -> {
            mBtAdapter.cancelDiscovery();
            scan_state = false;
        }, 10 * 1000);

    }

    @SuppressLint("MissingPermission")
    public void stopDiscovery() {

        mBtAdapter.cancelDiscovery();
        scan_state = false;
    }

    private void startConnProc(String strMacAdd, ConnectCallback callback) {

        stopConnect();

        BluetoothDevice bluetoothDevice = mBtAdapter.getRemoteDevice(strMacAdd);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mBluetoothSocket = bluetoothDevice
                    .createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            mBluetoothSocket.connect();
            mInputStream = mBluetoothSocket.getInputStream();
            mOutputStream = mBluetoothSocket.getOutputStream();
            initReadThread();
            if (callback != null) {
                callback.cb_btConnect(true, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.cb_btConnect(false, -1);
            }
            return;
        }
    }

    public void startConnect(String strMacAdd, ConnectCallback callback)
    {
        if (scan_state) {
            stopDiscovery();
        }

        new Thread(() -> startConnProc(strMacAdd, callback)).start();
    }

    private void stopConnect()
    {
        tagReadThread = false;

        while(threadRead != null) {
            try {
                Thread.sleep(100);
            } catch (Exception ignored) {
            }
        }
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
        }catch (Exception ignored) { }
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }
        }catch(Exception ignored) {}
        try{
            if (mBluetoothSocket != null) {
                mBluetoothSocket.close();
            }

        }catch (Exception ignored) {}


        mInputStream = null;
        mOutputStream = null;
        mBluetoothSocket = null;

    }

    public synchronized void sendMsg(byte[] byteData, int len)
    {
        if (mOutputStream == null) {
            return;
        }
        try {
            mOutputStream.write(byteData, 0, len);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initReadThread()
    {
        tagReadThread = true;

        threadRead = new Thread(() -> {
            byte[] byteBuffer = new byte[1024];
            while(tagReadThread) {
                try {
                    int len_available = mInputStream.available();
                    if (len_available > 0) {
                        int len_read = mInputStream.read(byteBuffer);

                        if (len_read != 0) {
                            mowerData.onReceivedData(byteBuffer, len_read);
                        }
                        else {
                        }
                    }
                    else {
                        Thread.sleep(50);
                    }
                }
                catch(Exception e) {
                    tagReadThread = false;
                    threadRead = null;
                }
            }
            threadRead = null;
        });
        threadRead.start();
    }

    public void getLocationInfo(LocationInfo info)
    {
        mowerData.getLocationInfo(info);
    }
    public void getChassisInfo(ChassisInfo info)
    {
        mowerData.getChassisInfo(info);
    }

    public void preset_ota_result()
    {
        mowerData.preset_ota_result();
    }
    public int get_ota_result()
    {
        return mowerData.get_ota_result();
    }

    public void sendWIFI(String ssid, String password)
    {
        byte[] byteResult = mowerData.sendWIFI(ssid, password);
        sendMsg(byteResult, byteResult.length);
    }

    public void sendStartWork()
    {
        byte[] byteResult = mowerData.sendStartWork();
        sendMsg(byteResult, byteResult.length);
    }

    public void sendStopWork()
    {
        byte[] byteResult = mowerData.sendStopWork();
        sendMsg(byteResult, byteResult.length);
    }

    public void sendGoHome()
    {
        byte[] byteResult = mowerData.sendGoHome();
        sendMsg(byteResult, byteResult.length);
    }
    public void sendResetState()
    {
        byte[] byteResult = mowerData.sendResetState();
        sendMsg(byteResult, byteResult.length);
    }

    public void sendRemoteControl(int angle, int strength)
    {
        byte[] byteResult = mowerData.sendRemoteControl(angle, strength);
        sendMsg(byteResult, byteResult.length);
    }


    public void sendMapInfo(int numTotal)
    {
        byte[] byteResult = mowerData.sendMapInfo(numTotal);
        sendMsg(byteResult, byteResult.length);
    }

    public void sendMapData(int indexStart, int indexEnd)
    {
        byte[] byteResult = mowerData.sendMapData(indexStart, indexEnd);
        sendMsg(byteResult, byteResult.length);
    }


    public int[] getReqRouteIndex()
    {
        return mowerData.getReqRouteIndex();
    }


}