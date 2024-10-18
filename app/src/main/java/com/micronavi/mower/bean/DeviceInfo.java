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

public class DeviceInfo {
    public String addr;
    public String name;
    public int rssi;
    public DeviceInfo(String addr, String name,int rssi){
        this.addr = addr;
        this.name = name;
        this.rssi = rssi;
    }
}
