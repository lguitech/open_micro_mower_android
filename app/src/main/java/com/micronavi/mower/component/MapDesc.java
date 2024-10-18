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


import android.util.Log;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.micronavi.mower.bean.BoundingBox;
import com.micronavi.mower.bean.GeoPoint;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MapDesc {
    private static final String TAG =MapDesc.class.getName();
    private static MapDesc instance = null;

    public static MapDesc getInstance() {
        if (instance == null) {
            instance = new MapDesc();
        }
        return instance;
    }

    private final Lock lock = new ReentrantLock();
    private boolean checkFinished = false;
    private final BoundingBox boundingBox = new BoundingBox();
    private final LinkedList<MapObject> listObj = new LinkedList<>();

    private final GeoPoint ptGnss = new GeoPoint();
    private final MapObject objWork = new MapObject(MapObject.OBJ_TYPE_ID_WORK, getAvailableID());;

    public void setCheckFinished(boolean value) {
        checkFinished = value;
    }

    public boolean getCheckFinished() {
        return checkFinished;
    }

    public LinkedList<MapObject> getObjectList() {
        return listObj;
    }

    public ArrayList<MapObject> getObjectListCopy()
    {
        lock.lock();
        try {
            ArrayList<MapObject> arrayList = new ArrayList<>(listObj);
            arrayList.add(objWork);
            return arrayList;
        }
        finally {
            lock.unlock();
        }
    }
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }


    private void updateBoundingBox(MapObject mapObject) {
        BoundingBox bbox = mapObject.getBoundingBox();
        if (bbox.isValid()) {
            this.boundingBox.left = Math.min(bbox.left, this.boundingBox.left);
            this.boundingBox.right = Math.max(bbox.right, this.boundingBox.right);
            this.boundingBox.bottom = Math.min(bbox.bottom, this.boundingBox.bottom);
            this.boundingBox.top = Math.max(bbox.top, this.boundingBox.top);
        }
    }

    private void resetBoundingBox() {
        boundingBox.left = boundingBox.bottom = Double.MAX_VALUE;
        boundingBox.right = boundingBox.top = Double.MIN_VALUE;
    }

    public void reCalBoundingBox() {
        resetBoundingBox();
        for (int i = 0; i < listObj.size(); i++) {
            MapObject mapObject = listObj.get(i);
            this.updateBoundingBox(mapObject);
        }
    }

    public void delObject(MapObject mapObject)
    {
        lock.lock();
        try {
            listObj.remove(mapObject);
        }
        finally {
            lock.unlock();
        }

    }

    private int getAvailableID() {
        if (listObj.isEmpty()) {
            return 0;
        }
        int idMax = 0;
        for (MapObject obj : listObj) {
            if (obj.getObjID() > idMax) {
                idMax = obj.getObjID();
            }
        }
        return idMax + 1;
    }

    public MapObject newObject(int objType)
    {
        lock.lock();
        try {
            int objID = getAvailableID();
            MapObject mapObject = new MapObject(objType, objID);
            listObj.add(mapObject);
            checkFinished = false;

            return mapObject;
        }
        finally {
            lock.unlock();
        }
    }

    public MapObject newObject(int objType, int objID)
    {
        lock.lock();
        try {
            MapObject mapObject = new MapObject(objType, objID);
            listObj.add(mapObject);
            checkFinished = false;

            return mapObject;
        }
        finally {
            lock.unlock();
        }
    }

    public boolean hasHomeObj()
    {
        for (MapObject obj : listObj) {
            if (obj.getObjID() == MapObject.OBJ_TYPE_ID_HOME) {
                return true;
            }
        }
        return false;
    }

    public void setGnssLocation(GeoPoint point) {
        ptGnss.setValue(point);
    }

    public void addWorkLocation(double x, double y)
    {
        objWork.addPoint(x, y);
    }
    public void resetWorkLocation()
    {
        objWork.getListPoint().clear();
    }


    public GeoPoint getGnssLocation()
    {
        return ptGnss;
    }


    public String convertToString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"boundingBox\": {")
                .append("\"left\":").append(boundingBox.left).append(",")
                .append("\"bottom\":").append(boundingBox.bottom).append(",")
                .append("\"right\":").append(boundingBox.right).append(",")
                .append("\"top\":").append(boundingBox.top).append("},")
                .append("\"objectList\":[");
        for (MapObject mapObject : listObj) {
            if (mapObject.getObjType() == MapObject.OBJ_TYPE_ID_WORK) {
                continue;
            }
            ArrayList<GeoPoint> listPoint = new ArrayList<>();
            mapObject.getListPointCopy(listPoint);
            int pointListNum = listPoint.size();
            BoundingBox boundingBox = mapObject.getBoundingBox();
            sb.append("{\"boundingBox\": {")
                    .append("\"left\":").append(boundingBox.left).append(",")
                    .append("\"bottom\":").append(boundingBox.bottom).append(",")
                    .append("\"right\":").append(boundingBox.right).append(",")
                    .append("\"top\":").append(boundingBox.top).append("},")
                    .append("\"objID\":").append(mapObject.getObjID()).append(",")
                    .append("\"objType\":").append(mapObject.getObjType()).append(",")
                    .append("\"pointListNum\":").append(pointListNum).append(",")
                    .append("\"strName\":\"").append(mapObject.objName).append("\",")
                    .append(" \"listPoint\": [");

            for (GeoPoint point : listPoint) {
                sb.append("{\"x\":").append(point.x).append(",")
                        .append("\"y\":").append(point.y).append("},");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.append("]},");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("]}");
        Log.d("jsonString", sb.toString());
        return sb.toString();
    }


    public void initMapData() {
        try {
            readDataFromFile();
        } catch (Exception ignored) {
        }
    }

    public void writeDataToFile() {
        MapDataFile.writeMapToFile(this.convertToString());
    }

    private void readDataFromFile() {
        resetBoundingBox();
        try {
            String strJson = MapDataFile.readMapFromFile();
            if (strJson != null) {
                //解析对象
                JSONObject jsonObjMain = JSONObject.parseObject(strJson);

                JSONArray jsonArray = jsonObjMain.getJSONArray("objectList");
                if (jsonArray != null && !jsonArray.isEmpty()) {

                    for (int i = 0; i < jsonArray.size(); i++) {

                        JSONObject jsonMapObj = jsonArray.getJSONObject(i);

                        int objID = jsonMapObj.getInteger("objID");
                        int objType = jsonMapObj.getInteger("objType");

                        MapObject mapObject = newObject(objType, objID);

                        JSONArray listPoint = jsonMapObj.getJSONArray("listPoint");

                        for (int j = 0; j < listPoint.size(); j++) {
                            JSONObject jsonObjPoint = listPoint.getJSONObject(j);
                            double x = jsonObjPoint.getDouble("x");
                            double y = jsonObjPoint.getDouble("y");
                            mapObject.addPoint(x, y);
                        }
                        this.updateBoundingBox(mapObject);
                    }
                }
            }
        }
        catch (Exception e ) {
            MapDataFile.deleteMapFile();
        }
    }
}

