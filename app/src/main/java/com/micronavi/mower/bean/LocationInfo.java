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

public class LocationInfo {
    public final static int LOCATION_STATE_INVALID = 0;
    public final static int LOCATION_STATE_SP = 1;
    public final static int LOCATION_STATE_FLOAT = 2;
    public final static int LOCATION_STATE_FIX = 3;
    public final static int HEADING_STATE_INVALID = 0;
    public final static int HEADING_STATE_SP = 1;
    public final static int HEADING_STATE_FLOAT = 2;
    public final static int HEADING_STATE_FIX = 3;
    public long update_time;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public float roll;
    public int sat_num;
    public int loc_index;
    public boolean isExpired()
    {
        Date date = new Date();
        long mSec = date.getTime() - update_time;
        return mSec > 3000;
    }

    public synchronized void  set(LocationInfo infoLocation)
    {
        this.x = infoLocation.x;
        this.y = infoLocation.y;
        this.z = infoLocation.z;

        this.yaw = infoLocation.yaw;
        this.pitch = infoLocation.pitch;
        this.roll = infoLocation.roll;

        this.sat_num = infoLocation.sat_num;
        this.loc_index = infoLocation.loc_index;
        this.update_time = infoLocation.update_time;
    }

    public synchronized void get(LocationInfo infoLocation)
    {
        infoLocation.x = this.x;
        infoLocation.y = this.y;
        infoLocation.z = this.z;

        infoLocation.yaw = this.yaw;
        infoLocation.pitch = this.pitch;
        infoLocation.roll = this.roll;

        infoLocation.sat_num = this.sat_num;
        infoLocation.loc_index = this.loc_index;
        infoLocation.update_time = this.update_time;
    }
}
