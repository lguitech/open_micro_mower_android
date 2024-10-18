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

package com.micronavi.mower.component;

import com.micronavi.mower.bean.ChassisInfo;
import com.micronavi.mower.bean.LocationInfo;
import com.micronavi.mower.util.Util;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class MowerData {
    private static final String TAG = "MowerData";

    public static final int OTA_PRESET = 100;
    public static final int OTA_RESULT_OK = 0;
    public static final int OTA_RESULT_NETWORK_FAIL = -1;
    public static final int OTA_RESULT_DOWNLOAD_FAIL  =-2;
    public static final int OTA_RESULT_UNZIP_FAIL  =-3;
    public static final int OTA_RESULT_INPROGRESS  =-4;
    private static final int REQ_START_WORK = 1;
    //private static final int REQ_PAUSE_WORK = 2;
    private static final int REQ_PAUSE_WORK = 3;
    private static final int REQ_REMOTE_CONTROL = 4;
    private static final int REQ_SEND_MAP_INFO = 5;
    private static final int REQ_SEND_MAP_DATA = 6;
    private static final int REQ_GO_HOME = 9;
    private static final int REQ_GO_RESET = 10;
    private static final int REQ_SEND_WIFI = 11;


    private static final int MAX_LEN = 4096;
    private final byte[] byteBuffer = new byte[MAX_LEN];
    private int pos_tail = 0;

    private final LocationInfo infoLocation = new LocationInfo();
    private final ChassisInfo infoChassis = new ChassisInfo();

    private int ota_result;

    private final DataReqQueue queueMapData = new DataReqQueue();


    public void onReceivedData(byte[] byteInputData, int len) {
        String strInfo = new String(byteInputData, 0, len, StandardCharsets.UTF_8);
        data_parse(byteInputData, len);
    }

    private void data_parse_location(String[] strList) {

        try {
            LocationInfo info = new LocationInfo();
            info.x = Double.parseDouble(strList[1]);
            info.y = Double.parseDouble(strList[2]);
            info.z = Double.parseDouble(strList[3]);

            info.yaw = Float.parseFloat(strList[4]);
            info.pitch = Float.parseFloat(strList[5]);
            info.roll = Float.parseFloat(strList[6]);

            info.sat_num = Integer.parseInt(strList[7]);
            info.loc_index = Integer.parseInt(strList[8]);

            Date date = new Date();
            info.update_time = date.getTime();
            infoLocation.set(info);
        } catch (Exception e) {
        }
    }

    private void data_parse_chassis(String[] strList) {
        try {
            ChassisInfo info = new ChassisInfo();
            info.robot_state = Integer.parseInt(strList[1]);
            info.left_speed = Float.parseFloat(strList[2]);
            info.right_speed = Float.parseFloat(strList[3]);

            info.putter_state = Integer.parseInt(strList[4]);
            info.cutter_state = Integer.parseInt(strList[5]);
            info.auto_mode = Integer.parseInt(strList[6]);
            info.brake_state = Integer.parseInt((strList[7]));

            info.safe_edge_front = Integer.parseInt((strList[8]));
            info.safe_edge_back = Integer.parseInt((strList[9]));
            info.safe_edge_left = Integer.parseInt((strList[10]));
            info.safe_edge_right = Integer.parseInt((strList[11]));

            info.soc = Integer.parseInt(strList[12]);
            info.battery_volt = Float.parseFloat(strList[13]);
            info.battery_charge_state = Integer.parseInt(strList[14]);

            info.sonar_front_left = Float.parseFloat(strList[15]);
            info.sonar_front_center = Float.parseFloat(strList[16]);
            info.sonar_front_right = Float.parseFloat(strList[17]);

            info.sonar_rear_left = Float.parseFloat(strList[18]);
            info.sonar_rear_center = Float.parseFloat(strList[19]);
            info.sonar_rear_right = Float.parseFloat(strList[20]);


            info.versionInfo = strList[21];
            Date date = new Date();
            info.update_time = date.getTime();

            infoChassis.set(info);
        } catch (Exception e) {
        }
    }

    private void data_parse_ota(String[] strList)
    {
        ota_result = Integer.parseInt(strList[1]);
    }

    public void preset_ota_result()
    {
        ota_result = OTA_PRESET;
    }
    public int get_ota_result()
    {
        return ota_result;
    }

    private void data_parse_route(String[] strList) {
        int startIndex = Integer.parseInt(strList[1]);
        int endIndex = Integer.parseInt(strList[2]);

        queueMapData.put(startIndex + "," + endIndex);
    }

    public int[] getReqRouteIndex() {
        String strData = queueMapData.get();
        if (strData == null) {
            return null;
        } else {
            String[] list = strData.split(",");
            int[] arrInt = new int[2];
            arrInt[0] = Integer.parseInt(list[0]);
            arrInt[1] = Integer.parseInt(list[1]);
            return arrInt;
        }
    }

    public int checkCS(String bytes) {
        int num = 0;
        int endIndex = bytes.indexOf('*');

        if (endIndex == -1) {
            return 0;
        }

        for (int i = 0; i <= endIndex; i++) {
            num = (num + bytes.charAt(i)) % 256;
        }
        return num % 100;
    }
    private void data_parse(byte[] byteInputData, int len) {
        System.arraycopy(byteInputData, 0, byteBuffer, pos_tail, len);
        pos_tail += len;

        int index_start = -1;
        int index_end = -1;
        for (int i = 0; i < pos_tail; i++) {
            if (byteBuffer[i] == '$') {
                index_start = i;
            } else if (byteBuffer[i] == '\n') {
                index_end = i;
                if (index_end > index_start) {
                    int len_sentence = index_end - index_start + 1;
                    byte[] byteSentence = new byte[len_sentence];
                    System.arraycopy(byteBuffer, index_start, byteSentence, 0, len_sentence);
                    String strSentence = new String(byteSentence);

                    String[] strList = strSentence.split("[*,]");

                    int cs = checkCS(strSentence);
                    if (cs != Integer.parseInt(strList[strList.length - 1].trim())) {

                    } else {
                        if (strList[0].equals("$loc") && strList.length == 10) {
                            data_parse_location(strList);
                        } else if (strList[0].equals("$chs") && strList.length == 23) {
                            data_parse_chassis(strList);
                        } else if (strList[0].equals("$pth") && strList.length == 4) {
                            data_parse_route(strList);
                        }
                        else if (strList[0].equals("$ota") && strList.length == 3) {
                            data_parse_ota(strList);
                        }

                        index_start = -1;
                        index_end = -1;
                    }
                }
            }
        }
        if (index_start == -1) {
            pos_tail = 0;
        } else if (index_start > 0) {
            if (index_end == -1) {
                if (pos_tail - index_start > 0) {
                    for (int i = index_start; i < pos_tail; i++) {
                        byteBuffer[i - index_start] = byteBuffer[i];
                    }
                }
                pos_tail = (pos_tail - index_start);
            }
        }
    }


    public void getLocationInfo(LocationInfo info) {
        infoLocation.get(info);
    }

    public void getChassisInfo(ChassisInfo info) {
        infoChassis.get(info);
    }


    public byte calcChecksum(byte[] sentence, int len) {
        int checksum = 0;

        for (int i = 0; i < len; i++) {
            checksum ^= (sentence[i] & 0xFF);
        }

        return (byte) (checksum & 0xFF);
    }

    private byte[] packData(int cmd, byte[] byteAttach, int len_attach) {
        int len_total = 7 + len_attach;
        byte[] byteResult = new byte[len_total];
        byteResult[0] = '$';
        byteResult[1] = '$';
        byteResult[2] = (byte) len_total;
        byteResult[3] = (byte) cmd;
        if (len_attach != 0 && byteAttach != null) {
            System.arraycopy(byteAttach, 0, byteResult, 4, len_attach);
        }

        byteResult[len_total - 3] = calcChecksum(byteAttach, len_attach);
        byteResult[len_total - 2] = 0x0d;
        byteResult[len_total - 1] = 0x0a;
        return byteResult;
    }

    public byte[] sendStartWork() {
        return packData(REQ_START_WORK, null, 0);
    }

    public byte[] sendStopWork() {
        return packData(REQ_PAUSE_WORK, null, 0);
    }

    public byte[] sendGoHome() {
        return packData(REQ_GO_HOME, null, 0);
    }

    public byte[] sendResetState() {
        return packData(REQ_GO_RESET, null, 0);
    }

    public byte[] sendWIFI(String ssid, String password)
    {
        byte[] byteSSID = ssid.getBytes(StandardCharsets.UTF_8);
        byte[] bytePassword = password.getBytes(StandardCharsets.UTF_8);
        int len_ssid = byteSSID.length;
        int len_password = bytePassword.length;
        byte[] byteArray = new byte[len_ssid + len_password + 1];
        System.arraycopy(byteSSID, 0, byteArray, 0, len_ssid);
        byteArray[len_ssid] = ',';
        System.arraycopy(bytePassword, 0, byteArray, len_ssid + 1, len_password);
        return packData(REQ_SEND_WIFI, byteArray, len_ssid + len_password + 1);
    }
    public byte[] sendRemoteControl(int angle, int strength) {
        byte[] byteAngle = Util.int2byte(angle);
        byte[] byteStrength = Util.int2byte(strength);
        byte[] byteArray = new byte[8];
        System.arraycopy(byteAngle, 0, byteArray, 0, 4);
        System.arraycopy(byteStrength, 0, byteArray, 4, 4);
        return packData(REQ_REMOTE_CONTROL, byteArray, 8);
    }

    public byte[] sendMapInfo(int numTotal) {
        byte[] byteNumPoints = Util.int2byte(numTotal);
        return packData(REQ_SEND_MAP_INFO, byteNumPoints, 4);
    }

    public byte[] sendMapData(int indexStart, int indexEnd) {
        byte[] byteIndexStart = Util.int2byte(indexStart);
        byte[] byteIndexEnd = Util.int2byte(indexEnd);
        int len_attach = 8 + (indexEnd - indexStart + 1);
        byte[] byteAttach = new byte[len_attach];
        System.arraycopy(byteIndexStart, 0, byteAttach, 0, 4);
        System.arraycopy(byteIndexEnd, 0, byteAttach, 4, 4);
        MapDataPacker mapDataPacker = MapDataPacker.getInstance();
        mapDataPacker.getDataSection(byteAttach, 8, indexStart, indexEnd);

        return packData(REQ_SEND_MAP_DATA, byteAttach, len_attach);
    }
}

