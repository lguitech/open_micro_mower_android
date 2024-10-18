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

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.micronavi.mower.R;
import com.micronavi.mower.component.MapObject;

import java.util.List;

public class ObjListAdapter extends BaseAdapter {
    private List<MapObject> listMapObj;
    private LayoutInflater inflater;
    private Context context;
    public ObjListAdapter(List<MapObject> listMapObj, Context context) {
        this.listMapObj = listMapObj;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return listMapObj.size();
    }

    @Override
    public Object getItem(int position) {
        return listMapObj.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.objlist_item,null);
        MapObject objInfo = (MapObject) getItem(position);

        TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
        ImageView iv_type = (ImageView) view.findViewById(R.id.iv_type);

        int type = listMapObj.get(position).getObjType();
        tv_name.setText(listMapObj.get(position).getObjTypeName(context, type));


        if (type == MapObject.OBJ_TYPE_ID_BOUNDARY) {
            int id = Resources.getSystem().getIdentifier("ic_menu_mapmode","drawable", "android");
            iv_type.setImageResource(id);
        }
        else if (type == MapObject.OBJ_TYPE_ID_CHANNEL) {
            int id = Resources.getSystem().getIdentifier("ic_menu_directions","drawable", "android");
            iv_type.setImageResource(id);

        }
        else if (type == MapObject.OBJ_TYPE_ID_OBSTACLE) {
            int id = Resources.getSystem().getIdentifier("ic_menu_report_image","drawable", "android");
            iv_type.setImageResource(id);
        }
        else if (type == MapObject.OBJ_TYPE_ID_HOME) {
            int id = Resources.getSystem().getIdentifier("ic_menu_myplaces","drawable", "android");
            iv_type.setImageResource(id);
        }

        return view;

    }
}
