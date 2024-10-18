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

package com.micronavi.mower.bean;

import java.util.Date;

public class ChassisInfo {
    public final static int ROBOT_STATE_UNKNOWN = -1;
    public final static int ROBOT_STATE_IDLE = 0;
    public final static int ROBOT_STATE_WORKING = 1;
    public final static int ROBOT_STATE_PAUSED = 2;
    public final static int ROBOT_STATE_STUCK = 3;
    public final static int ROBOT_STATE_FAULT = 4;
    public final static int ROBOT_STATE_STRUGGLING = 5;
    public final static int ROBOT_STATE_LOCATING = 6;
    public final static int ROBOT_STATE_TRANSFERRING = 7;
    public final static int ROBOT_STATE_DOCKING = 8;
    public final static int ROBOT_STATE_GOINGHOME = 9;

    public final static int PUTTER_STATE_BOTTOM = 0;
    public final static int PUTTER_STATE_TOP = 1;
    public final static int PUTTER_STATE_FALLING = 2;
    public final static int PUTTER_STATE_RISING= 3;

    public final static int CUTTER_STOPPED = 0;
    public final static int CUTTER_ROTATING = 1;

    public final static int AUTO_MODE_REMOTE_CONTROL = 0;
    public final static int AUTO_MODE_AUTO_CONTROL = 1;

    public final static int STOP_BUTTON_NO_PRESSED = 0;
    public final static int STOP_BUTTON_PRESSED = 1;

    public final static int SAFE_EDGE_OK = 0;
    public final static int SAFE_EDGE_TOUCHED = 1;
    public final static int SAFE_EDGE_DAMAGED = 2;

    public final static int BATTERY_NO_CHARGING = 0;
    public final static int BATTERY_CHARGING = 1;

    public long update_time;

    public int robot_state = ROBOT_STATE_UNKNOWN;

    public float left_speed;
    public float right_speed;

    public int putter_state;
    public int cutter_state;
    public int auto_mode;
    public int brake_state;

    public int safe_edge_front;
    public int safe_edge_back;
    public int safe_edge_left;
    public int safe_edge_right;

    public int soc;
    public float battery_volt;
    public int battery_charge_state;

    public float sonar_front_left;
    public float sonar_front_center;
    public float sonar_front_right;

    public float sonar_rear_left;
    public float sonar_rear_center;
    public float sonar_rear_right;

    public String versionInfo;
    public boolean isExpired()
    {
        Date date = new Date();
        long mSec = date.getTime() - update_time;
        return mSec > 3000;
    }

    public synchronized void set(ChassisInfo infoChassis)
    {
        this.robot_state = infoChassis.robot_state;
        this.left_speed = infoChassis.left_speed;
        this.right_speed = infoChassis.right_speed;

        this.putter_state = infoChassis.putter_state;
        this.cutter_state = infoChassis.cutter_state;
        this.auto_mode = infoChassis.auto_mode;
        this.brake_state = infoChassis.brake_state;

        this.safe_edge_front = infoChassis.safe_edge_front;
        this.safe_edge_back = infoChassis.safe_edge_back;
        this.safe_edge_left = infoChassis.safe_edge_left;
        this.safe_edge_right = infoChassis.safe_edge_right;

        this.soc = infoChassis.soc;
        this.battery_volt = infoChassis.battery_volt;
        this.battery_charge_state = infoChassis.battery_charge_state;

        this.update_time = infoChassis.update_time;
        this.versionInfo = infoChassis.versionInfo;

        this.sonar_front_left = infoChassis.sonar_front_left;
        this.sonar_front_center = infoChassis.sonar_front_center;
        this.sonar_front_right = infoChassis.sonar_front_right;

        this.sonar_rear_left = infoChassis.sonar_rear_left;
        this.sonar_rear_center = infoChassis.sonar_rear_center;
        this.sonar_rear_right = infoChassis.sonar_rear_right;
    }

    public synchronized void get(ChassisInfo infoChassis)
    {
        infoChassis.robot_state = this.robot_state;
        infoChassis.left_speed = this.left_speed;
        infoChassis.right_speed = this.right_speed;

        infoChassis.putter_state = this.putter_state;
        infoChassis.cutter_state = this.cutter_state;
        infoChassis.auto_mode = this.auto_mode;
        infoChassis.brake_state = this.brake_state;

        infoChassis.safe_edge_front = this.safe_edge_front;
        infoChassis.safe_edge_back = this.safe_edge_back;
        infoChassis.safe_edge_left = this.safe_edge_left;
        infoChassis.safe_edge_right = this.safe_edge_right;

        infoChassis.soc = this.soc;
        infoChassis.battery_volt = this.battery_volt;
        infoChassis.battery_charge_state = this.battery_charge_state;

        infoChassis.update_time = this.update_time;
        infoChassis.versionInfo = this.versionInfo;

        infoChassis.sonar_front_left = this.sonar_front_left;
        infoChassis.sonar_front_center = this.sonar_front_center;
        infoChassis.sonar_front_right = this.sonar_front_right;

        infoChassis.sonar_rear_left = this.sonar_rear_left;
        infoChassis.sonar_rear_center = this.sonar_rear_center;
        infoChassis.sonar_rear_right = this.sonar_rear_right;
    }
}
