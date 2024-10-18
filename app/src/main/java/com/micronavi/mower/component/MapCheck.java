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
import com.micronavi.mower.bean.GeoPoint;
import com.micronavi.mower.util.JTSUtil;
import com.micronavi.mower.util.MowerUtil;
import java.util.LinkedList;


public class MapCheck {
    private static final String TAG = "mapcheck";

    private static final String ERR_OK = "";
    private static final double CHANNEL_HOME_DIS_THRESHOLD = 1.0;
    public static String doCheck(Context context)
    {
        LinkedList<MapObject> objList = MapDesc.getInstance().getObjectList();
        GeoPoint ptHome = null;

        //分类
        LinkedList<MapObject> objListBoundary = new LinkedList<>();
        LinkedList<MapObject> objListObstacle = new LinkedList<>();
        LinkedList<MapObject> objListChannel = new LinkedList<>();
        int numObj = objList.size();
        for (int i=0; i<numObj; i++) {
            MapObject mapObject = objList.get(i);
            switch (mapObject.getObjType()) {
                case MapObject.OBJ_TYPE_ID_BOUNDARY:
                    objListBoundary.add(mapObject);
                    break;
                case MapObject.OBJ_TYPE_ID_OBSTACLE:
                    objListObstacle.add(mapObject);
                    break;
                case MapObject.OBJ_TYPE_ID_CHANNEL:
                    objListChannel.add(mapObject);
                    break;
                case MapObject.OBJ_TYPE_ID_HOME:
                    ptHome = mapObject.getListPoint().get(0);
                    break;
            }
        }

        String strResult = checkBoundary(context, objListBoundary, objListChannel);
        if (!strResult.equals("")) {
            return strResult;
        }

        strResult = checkObstacle(context, objListBoundary, objListObstacle);
        if (!strResult.equals("")) {
            return strResult;
        }

        strResult = checkChannel(context, ptHome, objListBoundary, objListChannel, objListObstacle);
        if (!strResult.equals("")) {
            return strResult;
        }

        strResult = checkHome(context, ptHome, objListBoundary, objListChannel);
        if (!strResult.equals("")) {
            return strResult;
        }

        return ERR_OK;
    }

    private static String checkHome(Context context,
                                    GeoPoint ptHome,
                                    LinkedList<MapObject> objListBoundary,
                                    LinkedList<MapObject> objListChannel)
    {
        if (ptHome == null) {
            return ERR_OK;
        }
        boolean isIntersect = false;
        for(MapObject objMap : objListBoundary) {
            LinkedList<GeoPoint> listPoint = objMap.getListPoint();
            if (JTSUtil.isPointIntersectPolygon(ptHome, listPoint)) {
                isIntersect = true;
                break;
            }
        }
        if (isIntersect) {
            return ERR_OK;
        }

        for (MapObject objMap : objListChannel) {
            LinkedList<GeoPoint> listPoint = objMap.getListPoint();
            GeoPoint ptStart = listPoint.getFirst();
            GeoPoint ptEnd = listPoint.getLast();
            if (ptStart.isEqual(ptHome)) {
                return ERR_OK;
            }
            if (ptEnd.isEqual(ptHome)) {
                return ERR_OK;
            }
        }

        return context.getString(R.string.ErrHomeLocation);
    }

    private static String checkBoundary(Context context,
                                        LinkedList<MapObject> objListBoundary,
                                        LinkedList<MapObject> objListChannel)
    {
        int numObj = objListBoundary.size();
        if (numObj == 0) {
            return context.getString(R.string.ErrNoBoundary);
        }

        if (numObj == 1) {
            return ERR_OK;
        }
        for (int i=0; i<numObj; i++) {
            for(int j= i+1; j<numObj; j++) {
                LinkedList<GeoPoint> list1 = objListBoundary.get(i).getListPoint();
                LinkedList<GeoPoint> list2 = objListBoundary.get(j).getListPoint();
                if (JTSUtil.isPolygonIntersect(list1, list2)) {
                    return context.getString(R.string.ErrBoundaryIntersect);
                }
            }
        }
        if (objListChannel.size() == 0) {
            return context.getString(R.string.ErrBoundaryNotConnect);
        }
        for (int i=0; i<numObj; i++) {
            for(int j= i+1; j<numObj; j++) {
                LinkedList<GeoPoint> list1 = objListBoundary.get(i).getListPoint();
                LinkedList<GeoPoint> list2 = objListBoundary.get(j).getListPoint();

                boolean check_ok = false;
                //所有通道
                for (MapObject objChannel : objListChannel) {
                    LinkedList<GeoPoint> listChannelPoint = objChannel.getListPoint();

                    GeoPoint ptStart = listChannelPoint.getFirst();
                    GeoPoint ptEnd = listChannelPoint.getLast();
                    if (JTSUtil.isPointIntersectPolygon(ptStart, list1) &&
                        JTSUtil.isPointIntersectPolygon(ptEnd, list2)
                    )
                    {
                        check_ok = true;
                        break;
                    }
                    if (JTSUtil.isPointIntersectPolygon(ptStart, list2) &&
                            JTSUtil.isPointIntersectPolygon(ptEnd, list1)
                    )
                    {
                        check_ok = true;
                        break;
                    }
                }
                if (!check_ok) {
                    return context.getString(R.string.ErrBoundaryNotConnect);
                }
            }
        }
        return ERR_OK;
    }

    private static String checkObstacle(Context context,
                                        LinkedList<MapObject> objListBoundary,
                                        LinkedList<MapObject> objListObstacle)
    {
        for (MapObject mapObjectObstacle : objListObstacle) {
            LinkedList<GeoPoint> listPointObstacle = mapObjectObstacle.getListPoint();
            int counter = 0;
            for (MapObject mapObjectBoundary : objListBoundary) {
                LinkedList<GeoPoint> listPointBoundary = mapObjectBoundary.getListPoint();
                if (JTSUtil.isPolygonIntersect(listPointObstacle, listPointBoundary)) {
                    counter += 1;
                }
            }
            if (counter == 0) {
                return context.getString(R.string.ErrObstacleLocation);
            }
        }

        for (MapObject mapObjectObstacle : objListObstacle) {
            LinkedList<GeoPoint> listPointObstacle = mapObjectObstacle.getListPoint();
            for (MapObject mapObjectBoundary : objListBoundary) {
                LinkedList<GeoPoint> listPointBoundary = mapObjectBoundary.getListPoint();
                if (JTSUtil.isPolygonIntersect(listPointObstacle, listPointBoundary)) {
                    LinkedList<GeoPoint>  listResult = JTSUtil.getIntersectPolygon(listPointObstacle, listPointBoundary);
                    if (listResult != null) {
                        mapObjectObstacle.resetPointList(listResult);
                    }
                }
            }
        }

        while(true) {
            int size = objListObstacle.size();
            if (size == 1) {
                break;
            }

            boolean doMerge = false;

            for (int i=0; i<size; i++) {
                MapObject mapObject1 = objListObstacle.get(i);
                for (int j=i+1; j<size; j++) {
                    MapObject mapObject2 = objListObstacle.get(j);

                    LinkedList<GeoPoint> listResult = JTSUtil.dealMergePolygon(mapObject1.getListPoint(), mapObject2.getListPoint());
                    if (listResult != null) {
                        mapObject1.resetPointList(listResult);
                        objListObstacle.remove(mapObject2);
                        MapDesc.getInstance().delObject(mapObject2);
                        doMerge = true;
                        break;
                    }
                }
                if (doMerge) {
                    break;
                }
            }
            if (!doMerge) {
                break;
            }
        }
        return ERR_OK;
    }

    private static String checkChannel(Context context,
                                       GeoPoint  ptHome,
                                       LinkedList<MapObject> objListBoundary,
                                       LinkedList<MapObject> objListChannel,
                                       LinkedList<MapObject> objListObstacle)
    {
        //通道是否连接了障碍物
        for (MapObject mapObjectChannel : objListChannel) {
            LinkedList<GeoPoint> listChannelPoint = mapObjectChannel.getListPoint();
            GeoPoint ptStart = listChannelPoint.getFirst();
            GeoPoint ptEnd = listChannelPoint.getLast();

            for (MapObject mapObjectObstacle : objListObstacle) {
                LinkedList<GeoPoint> listObstaclePoint = mapObjectObstacle.getListPoint();
                if (JTSUtil.isPointIntersectPolygon(ptStart, listObstaclePoint)) {
                    return context.getString(R.string.ErrChannelConnectObstacle);
                }
                if (JTSUtil.isPointIntersectPolygon(ptEnd, listObstaclePoint)) {
                    return context.getString(R.string.ErrChannelConnectObstacle);
                }
            }
        }

        for (MapObject mapObjectChannel : objListChannel) {
            LinkedList<GeoPoint> listChannelPoint = mapObjectChannel.getListPoint();

            GeoPoint ptStart = listChannelPoint.getFirst();
            GeoPoint ptEnd = listChannelPoint.getLast();

            int startIntersectCounter = 0;
            int endIntersectCounter = 0;

            for (MapObject mapObjectBoundary : objListBoundary) {
                LinkedList<GeoPoint> listBoundaryPoint = mapObjectBoundary.getListPoint();
                if (JTSUtil.isPointIntersectPolygon(ptStart, listBoundaryPoint)) {
                    startIntersectCounter += 1;
                }
                if (JTSUtil.isPointIntersectPolygon(ptEnd, listBoundaryPoint)) {
                    endIntersectCounter += 1;
                }
            }
            if (startIntersectCounter == 1 && endIntersectCounter == 1) {
                continue;
            }
            if (startIntersectCounter == 0 && endIntersectCounter == 0) {
                return context.getString(R.string.ErrChannelNoBoundaryConnect);
            }
            if (ptHome == null) {
                return context.getString(R.string.ErrChannleConnectOneBoundary);
            }
            else {
                if (startIntersectCounter == 1 && endIntersectCounter == 0) {
                    double dis = MowerUtil.calcDistance(ptHome, ptEnd);
                    if (dis < CHANNEL_HOME_DIS_THRESHOLD) {
                        ptEnd.setValue(ptHome);
                    }
                    else {
                        return context.getString(R.string.ErrChannleConnectOneBoundary);
                    }
                }
                else if (startIntersectCounter == 0 && endIntersectCounter == 1) {
                    double dis = MowerUtil.calcDistance(ptHome, ptStart);
                    if (dis < CHANNEL_HOME_DIS_THRESHOLD) {
                        ptStart.setValue(ptHome);
                    }
                    else {
                        return context.getString(R.string.ErrChannleConnectOneBoundary);
                    }
                }
            }
        }
        return ERR_OK;
    }
}
