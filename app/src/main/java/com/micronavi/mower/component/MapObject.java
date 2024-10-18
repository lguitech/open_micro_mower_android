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



import android.content.Context;
import com.micronavi.mower.R;
import com.micronavi.mower.bean.BoundingBox;
import com.micronavi.mower.bean.GeoPoint;
import com.micronavi.mower.util.JTSUtil;
import com.micronavi.mower.util.MowerUtil;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class MapObject {
    private static final String TAG = MapObject.class.getName();
    public static final int OBJ_TYPE_ID_UNDEF = 1000;
    public static final int OBJ_TYPE_ID_BOUNDARY = 1001;   //边界
    public static final int OBJ_TYPE_ID_CHANNEL = 1002;   //通道层
    public static final int OBJ_TYPE_ID_OBSTACLE = 1003;   //障碍物
    public static final int OBJ_TYPE_ID_WORK = 1005;       //作业层
    public static final int OBJ_TYPE_ID_HOME = 1006;        //充电桩
    public static final int OBJ_TYPE_GNSS = 1009;           //实时位置


    private static final float POINT_TOO_NEAR_THRESHOLD = 0.2f;//建图时，边界节点的最小距离

    private final int objType;
    private final int objID;
    public String objName;
    private final BoundingBox boundingBox = new BoundingBox();
    private final LinkedList<GeoPoint> listPoint = new LinkedList<>();

    private final Lock lock = new ReentrantLock();

    public String getObjTypeName(Context context, int type) {
        switch (type) {
            case OBJ_TYPE_ID_BOUNDARY:
                return context.getString(R.string.Boundary);
            case OBJ_TYPE_ID_OBSTACLE:
                return context.getString(R.string.Obstacle);
            case OBJ_TYPE_ID_CHANNEL:
                return context.getString(R.string.Channel);
            case OBJ_TYPE_ID_WORK:
                return context.getString(R.string.WorkPath);
            case OBJ_TYPE_ID_HOME:
                return context.getString(R.string.Dock);

            default:
                return "";
        }
    }
    public int getObjID() {
        return objID;
    }

    public int getObjType() {
        return objType;
    }

    public MapObject(int objType, int objID) {
        this.objType = objType;
        this.objID = objID;
    }

    public LinkedList<GeoPoint> getListPoint() {
        return listPoint;
    }

    public void getListPointCopy(ArrayList<GeoPoint> list) {
        lock.lock();
        try {
            list.clear();
            for (GeoPoint geoPoint : listPoint) {
                GeoPoint newPoint = new GeoPoint(geoPoint);
                list.add(newPoint);
            }
        }
        finally {
            lock.unlock();
        }
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }


    private double toNormalRegion(double angle)
    {
        while(angle < 0) {
            angle += 2 * Math.PI;
        }
        while (angle >= 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }
        return angle ;
    }

    private int calc_angle(GeoPoint point1, GeoPoint point2)
    {
        double dy = point2.utmy - point1.utmy;
        double dx = point2.utmx - point1.utmx;
        return (int) (toNormalRegion(Math.atan2(dy, dx)) * 180.0 / Math.PI);
    }


    private boolean simpleFilter(GeoPoint geoPoint) {

        if (listPoint.isEmpty()) {
            return true;
        } else {
            GeoPoint ptLast = listPoint.getLast();

            double dis = MowerUtil.calcDistance(ptLast, geoPoint);
            if (dis > POINT_TOO_NEAR_THRESHOLD) {
                ptLast.angle = calc_angle(ptLast, geoPoint);
                return true;
            }
            return false;
        }
    }

    private void updateBoundingBox(double x, double y) {
        if (listPoint.size() == 1) {
            boundingBox.left = boundingBox.right = x;
            boundingBox.top = boundingBox.bottom = y;
        } else {
            boundingBox.left = Math.min(boundingBox.left, x);
            boundingBox.right = Math.max(boundingBox.right, x);
            boundingBox.bottom = Math.min(boundingBox.bottom, y);
            boundingBox.top = Math.max(boundingBox.top, y);
        }
    }

    public void addPoint(double x, double y) {
        lock.lock();
        try {
            GeoPoint geoPoint = new GeoPoint(x, y);
            if (this.objType == MapObject.OBJ_TYPE_ID_HOME) {
                listPoint.clear();
                listPoint.add(geoPoint);
                updateBoundingBox(x, y);
            } else {
                if (simpleFilter(geoPoint)) {
                    this.listPoint.add(geoPoint);
                    updateBoundingBox(x, y);
                }
            }
            MapDesc.getInstance().reCalBoundingBox();
        }
        finally {
            lock.unlock();
        }
    }

    public void resetPointList(LinkedList<GeoPoint> listPointNew)
    {
        lock.lock();
        try {
            listPoint.clear();
            for (GeoPoint geoPoint : listPointNew) {
                GeoPoint geoPointNew = new GeoPoint(geoPoint);
                listPoint.add(geoPointNew);
            }
        }
        finally {
            lock.unlock();
        }
    }

    private void reCalBoundingBox() {
        boundingBox.reset();
        for (GeoPoint geoPoint : listPoint) {
            boundingBox.left = Math.min(boundingBox.left, geoPoint.x);
            boundingBox.right = Math.max(boundingBox.right, geoPoint.x);
            boundingBox.bottom = Math.min(boundingBox.bottom, geoPoint.y);
            boundingBox.top = Math.max(boundingBox.top, geoPoint.y);
        }
    }

    public boolean simpleObjFilter(StringBuilder sb)
    {
        lock.lock();
        try {
            if (objType == MapObject.OBJ_TYPE_ID_HOME) {
                if (listPoint.size() == 1) {
                    return true;
                } else {
                    sb.append("点数太少，无法生成地图");
                    return false;
                }
            }
            if (listPoint.size() < 3) {
                sb.append("点数太少，无法生成地图");
                return false;
            }
            if (boundingBox.isTooSmall()) {
                sb.append("区域面积太小或通道太短，无法生成地图");
                return false;
            }
            if (objType == MapObject.OBJ_TYPE_ID_BOUNDARY || objType == MapObject.OBJ_TYPE_ID_OBSTACLE) {
                //topo 判断和修复
                JTSUtil.dealSelfIntersect_polygon(listPoint);
                reCalBoundingBox();
            }
            else if (objType == MapObject.OBJ_TYPE_ID_CHANNEL) {
                JTSUtil.dealSelfIntersect_polyline(listPoint);
                reCalBoundingBox();
            }
            this.reCalBoundingBox();
            return true;
        }
        finally {
            lock.unlock();
        }
    }
}