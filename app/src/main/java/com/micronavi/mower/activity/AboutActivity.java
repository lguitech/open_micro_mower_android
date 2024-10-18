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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.micronavi.mower.R;
import com.micronavi.mower.bean.ChassisInfo;
import com.micronavi.mower.component.MowerData;
import com.micronavi.mower.service.BTService;
import com.alibaba.fastjson.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
public class AboutActivity extends Activity  implements ServiceConnection, View.OnClickListener{
    private static final String TAG = "about";

    private static final String url_version = "http://your-server/mower/release_info.json";
    private TextView tvMowerVersionInfo;
    private TextView tvPrompt;
    private Button btnCheckMowerVersion;
    private String strCurrMowerVersion = "";
    private String strNewMowerVersion = "";
    private BTService mService = null;
    private Thread threadChassis = null;
    private boolean quitTag = false;

    private boolean upgrade_in_progress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        strCurrMowerVersion = getString(R.string.Unknown);
        tvMowerVersionInfo = findViewById(R.id.tvMowerVersionInfo);
        tvMowerVersionInfo.setText(strCurrMowerVersion);

        tvPrompt = findViewById(R.id.tvPrompt);
        btnCheckMowerVersion = findViewById(R.id.btnCheckMowerVersion);
        btnCheckMowerVersion.setOnClickListener(this);

        //绑定service;
        Intent serviceIntent = new Intent(this, BTService.class);
        //如果未绑定，则进行绑定
        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);

        initThread();
    }
    @Override
    protected void onDestroy()
    {
        unInitThread();
        unbindService(this);
        super.onDestroy();
    }

    private void onReceived_ChassisInfo(ChassisInfo info)
    {
        //ui线程中更新界面上的数字
        runOnUiThread(() -> {
            if (info.isExpired()) {
                strCurrMowerVersion = getString(R.string.Unknown);
            }
            else {
                strCurrMowerVersion = info.versionInfo;
            }
            String strValue = getString(R.string.MowerVersionInfo) + ": " + strCurrMowerVersion;
            tvMowerVersionInfo.setText(strValue);

            if (upgrade_in_progress) {
                int result = mService.get_ota_result();
                if (result != MowerData.OTA_PRESET) {
                    switch (result)
                    {
                    case MowerData.OTA_RESULT_NETWORK_FAIL:
                        setPrompt(getString(R.string.OtaNetworkFail));
                        break;
                    case MowerData.OTA_RESULT_DOWNLOAD_FAIL:
                        setPrompt(getString(R.string.OtaDownloadFail));
                        break;
                    case MowerData.OTA_RESULT_UNZIP_FAIL:
                        setPrompt(getString(R.string.OtaUnzipFail));
                        break;
                    case MowerData.OTA_RESULT_OK:
                        setPrompt(getString(R.string.UpgradeFinished));
                        setPrompt(getString(R.string.Reboot));
                        break;
                    }
                    upgrade_in_progress = false;
                }
            }
            if (strCurrMowerVersion.equalsIgnoreCase(strNewMowerVersion)) {
                clearPrompt();
            }

        });
    }
    private void  initThread()
    {
        if (threadChassis != null) {
            return;
        }
        quitTag = true;

        threadChassis = new Thread(() -> {
            ChassisInfo infoChassis = new ChassisInfo();
            while(quitTag) {
                if (mService != null) {
                    mService.getChassisInfo(infoChassis);
                    onReceived_ChassisInfo(infoChassis);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {  }
            }
        });
        threadChassis.start();
    }

    private void unInitThread()
    {
        quitTag = false;
        try {
            threadChassis.interrupt();
            threadChassis.join(1000);
        } catch (InterruptedException ignored) {}
        threadChassis = null;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        BTService.MyBinder myBinder = (BTService.MyBinder) service;
        mService = (BTService) myBinder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onClick(View view) {
        if (view == btnCheckMowerVersion) {

            checkMowerUpdate();
        }
    }

    private void setPrompt(String strContent)
    {
        runOnUiThread(() -> {
            String strNow = tvPrompt.getText().toString();
            String strNew = strNow + "\r\n" + strContent;
            tvPrompt.setText(strNew);
        });
    }
    private void enableCheckMowerButton()
    {
        runOnUiThread(() -> {
            btnCheckMowerVersion.setEnabled(true);
        });
    }

    private void clearPrompt()
    {
        runOnUiThread(() -> tvPrompt.setText(""));
    }


    private int compareVersions(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < length; i++) {
            int part1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int part2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (part1 < part2) {
                return -1;
            } else if (part1 > part2) {
                return 1;
            }
        }

        return 0;
    }
    private void checkBackendVersion() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url_version)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                setPrompt(getString(R.string.NetworkFail));
                enableCheckMowerButton();
                Log.d(TAG, "onFailure " + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    setPrompt(getString(R.string.NetworkFail));
                    enableCheckMowerButton();
                    return;
                }

                try {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    JSONObject jsonObjMain = JSONObject.parseObject(responseData);

                    strNewMowerVersion = jsonObjMain.getString("version");
                    setPrompt(getString(R.string.CurrentVersionIs) + ": " + strCurrMowerVersion);
                    setPrompt(getString(R.string.LatestVersionIs) + ": " + strNewMowerVersion);

                    if (compareVersions(strNewMowerVersion, strCurrMowerVersion) > 0) {
                        showUpdateDialog();
                    }
                    else {
                        setPrompt(getString(R.string.AlreadyUpdate));
                    }
                } catch (JSONException e) {
                    setPrompt(getString(R.string.NetworkFail));
                }
                finally {
                    enableCheckMowerButton();
                }
            }
        });
    }



    private void checkMowerUpdate()
    {
        clearPrompt();
        if (strCurrMowerVersion.equalsIgnoreCase("") ||
                strCurrMowerVersion.equalsIgnoreCase(getString(R.string.Unknown)))
        {
            setPrompt(getString(R.string.UnknowMowerVersion));
            return;
        }
        btnCheckMowerVersion.setEnabled(false);

        Thread threadCheck = new Thread(() -> {
            setPrompt(getString(R.string.CheckUpgrade));
            checkBackendVersion();
        });
        threadCheck.start();
    }


    private String getCurrentWifiSSID() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                String ssid = wifiInfo.getSSID();
                if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.substring(1, ssid.length() - 1);
                    return ssid;
                }
                else {
                    return null;
                }
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    private void showUpdateDialog() {
        runOnUiThread(() -> {
            String message = getString(R.string.CurrentVersionIs) + " " + strCurrMowerVersion +
                    getString(R.string.LatestVersionIs) + " " + strNewMowerVersion + "\r\n" +
                    getString(R.string.ConfirmUpgrade) + " ?";

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.CheckUpgrade))
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.Ok), (dialog, which) -> {
                        String ssid = getCurrentWifiSSID();
                        if (ssid == null) {
                            setPrompt(getString(R.string.LinkWIFIFirstly));
                        } else {
                            showPasswordInputDialog(ssid);
                        }
                    })
                    .setNegativeButton(getString(R.string.Cancel), (dialog, which) -> {
                        clearPrompt();
                    })
                    .create()
                    .show();
        });
    }
    private void showPasswordInputDialog(String ssid) {
        final EditText input = new EditText(this);
        input.setHint(getString(R.string.EnterWIFIpassword));
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.EnterWIFIpassword))
                .setCancelable(false)
                .setMessage(getString(R.string.WIFISSID) + " :" + ssid)
                .setView(input)
                .setPositiveButton(getString(R.string.Ok), (dialog, which) -> {
                    String password = input.getText().toString();
                    Log.d(TAG, "Wi-Fi Password: " + password);
                    //通过蓝牙发送wifi
                    mService.sendWIFI(ssid, password);
                    setPrompt(getString(R.string.StartUpgrade) + "......");
                    upgrade_in_progress = true;
                    if (mService != null) {
                        mService.preset_ota_result();
                    }
                })
                .setNegativeButton(getString(R.string.Cancel), (dialog, which) -> {
                    clearPrompt();
                    upgrade_in_progress = false;
                })
                .show();
    }
}
