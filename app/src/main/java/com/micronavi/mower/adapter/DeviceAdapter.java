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

package com.micronavi.mower.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.micronavi.mower.R;
import com.micronavi.mower.bean.DeviceInfo;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class DeviceAdapter extends ArrayAdapter<DeviceInfo> {
    private int myResource;
    public DeviceAdapter(@NonNull Context context, int resource, List<DeviceInfo> deviceListData) {
        super(context, resource,deviceListData);
        myResource = resource;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        DeviceInfo deviceInfo = getItem(position);
        String name = "";
        int rssi = 0;
        if(deviceInfo!=null) {
            name = deviceInfo.name;
            rssi = deviceInfo.rssi;
        }
        @SuppressLint("ViewHolder") View view = LayoutInflater.from(getContext()).inflate(myResource,parent,false);
        ImageView headImg = view.findViewById(R.id.iv_type);
        if (name.substring(0, 1).equals("@") && (name.length() == 11) ) {
            headImg.setImageResource(R.drawable.ecble);
        } else {
            headImg.setImageResource(R.drawable.ble);
        }
        ((TextView)view.findViewById(R.id.tv_name)).setText(name);
        ((TextView)view.findViewById(R.id.tv_rssi)).setText((""+rssi));
        ImageView rssiImg = view.findViewById(R.id.iv_rssi);
        if(rssi >= -41) rssiImg.setImageResource(R.drawable.s5);
        else if(rssi >= -55) rssiImg.setImageResource(R.drawable.s4);
        else if(rssi >= -65) rssiImg.setImageResource(R.drawable.s3);
        else if(rssi >= -75) rssiImg.setImageResource(R.drawable.s2);
        else rssiImg.setImageResource(R.drawable.s1);
        return view;
    }
}