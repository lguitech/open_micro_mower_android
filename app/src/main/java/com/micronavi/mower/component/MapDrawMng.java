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


import com.micronavi.mower.bean.BoundingBox;
import com.micronavi.mower.bean.GeoPoint;
import com.micronavi.mower.bean.ScrObject;
import com.micronavi.mower.bean.ScrPoint;

import java.util.ArrayList;

public class MapDrawMng {
    private static final String TAG = MapDrawMng.class.getName();

    private final ArrayList<ScrObject> listScrObj = new ArrayList<>();
    private int view_width = 0;
    private int view_height = 0;

    public final static int DRAW_MAP_TYPE_UNDEF = 1000;
    public final static int DRAW_MAP_TYPE_MAPPING = 1001;
    public final static int DRAW_MAP_TYPE_WORKING = 1003;
    private int drawMapType = DRAW_MAP_TYPE_UNDEF;

    private double center_x = 0;
    private double center_y = 0;

    private float scale = 0.1f;
    private static final float SCALE_MAX_VALUE = 1.0f;
    private static final float SCALE_MIN_VALUE = 0.01f;

    private static final int DIFF_ANGLE_THRESHOLD = 5;

    private ScrPoint geo_to_scr(GeoPoint geoPoint)
    {
        double dx_geo = geoPoint.utmx - center_x;
        double dy_geo = geoPoint.utmy - center_y;

        int dx_scr = (int) (dx_geo / scale);
        int dy_scr = (int) (dy_geo / scale);
        int x = view_width / 2 + dx_scr;
        int y = view_height / 2 - dy_scr;

        return new ScrPoint(x, y, geoPoint.angle);
    }

    private final ArrayList<GeoPoint> listGeoPoint = new ArrayList<>();

    private int normalizeDiff(int diff)
    {
        while (diff < -180) {
            diff += 360;
        }
        while (diff > 180) {
            diff -= 360;
        }
        return diff;
    }

    private void calcDispData()
    {
        try {
            listScrObj.clear();
            ArrayList<MapObject> objList = MapDesc.getInstance().getObjectListCopy();
            for (MapObject mapObject : objList) {
                if (
                    (drawMapType == DRAW_MAP_TYPE_MAPPING) &&
                    (mapObject.getObjType() == MapObject.OBJ_TYPE_ID_WORK)
                )
                {
                    continue;
                }

                mapObject.getListPointCopy(listGeoPoint);

                if (!listGeoPoint.isEmpty()) {
                    ScrObject scrObject = new ScrObject();
                    scrObject.objType = mapObject.getObjType();
                    scrObject.strName = mapObject.objName;

                    ScrPoint ptPrev = new ScrPoint(Integer.MIN_VALUE, Integer.MIN_VALUE);
                    if (scrObject.objType != MapObject.OBJ_TYPE_ID_WORK) {
                        for (GeoPoint ptGeo : listGeoPoint) {
                            ScrPoint ptCurr = geo_to_scr(ptGeo);

                            if (ptCurr.x != ptPrev.x || ptCurr.y != ptPrev.y) {
                                scrObject.listPoint.add(ptCurr);
                            }
                            ptPrev.x = ptCurr.x;
                            ptPrev.y = ptCurr.y;
                        }
                    }
                    else {
                        int size = listGeoPoint.size();
                        int sumDiff = 0;

                        for (int i = 0; i < size -1; i++) {
                            GeoPoint ptGeo = listGeoPoint.get(i);
                            ScrPoint ptCurr = geo_to_scr(ptGeo);
                            sumDiff += Math.abs(normalizeDiff(ptCurr.angle - ptPrev.angle));
                            if ((ptCurr.x != ptPrev.x || ptCurr.y != ptPrev.y) && sumDiff > DIFF_ANGLE_THRESHOLD)
                            {
                                scrObject.listPoint.add(ptCurr);
                                sumDiff = 0;
                            }
                            ptPrev.x = ptCurr.x;
                            ptPrev.y = ptCurr.y;
                            ptPrev.angle = ptCurr.angle;
                        }
                        //the last one
                        GeoPoint ptGeo = listGeoPoint.get(size - 1);
                        ScrPoint ptScr = geo_to_scr(ptGeo);
                        scrObject.listPoint.add(ptScr);

                    }
                    listScrObj.add(scrObject);
                }
            }
            ScrObject scrObjGnss = new ScrObject();
            GeoPoint geoGnss = MapDesc.getInstance().getGnssLocation();
            ScrPoint scrGnss = geo_to_scr(geoGnss);
            scrObjGnss.objType = MapObject.OBJ_TYPE_GNSS;
            scrObjGnss.listPoint.add(scrGnss);
            listScrObj.add(scrObjGnss);
        }
        catch (Exception ignored) {}
    }
    public void initParam(int width, int height)
    {
        view_width = width;
        view_height = height;

        if (!MapDesc.getInstance().getObjectListCopy().isEmpty()) {
            BoundingBox boundingBox = MapDesc.getInstance().getBoundingBox();
            fit_rect(boundingBox);
        }
    }

    public void zoomValue(float value)
    {
        if (value < 1 && scale >= SCALE_MAX_VALUE) {
            return;
        }
        if (value > 1 && scale <= SCALE_MIN_VALUE) {
            return;
        }

        scale = scale /value;
    }

    public void zoomIn()
    {
        if (scale <= SCALE_MIN_VALUE) {
            return;
        }
        scale /= 2;
    }

    public void zoomOut()
    {
        if (scale >= SCALE_MAX_VALUE) {
            return;
        }
        scale *= 2;
    }

    public void move(int dx, int dy)
    {
        double dx_geo = dx * scale;
        double dy_geo = dy * scale;
        center_x -= dx_geo;
        center_y += dy_geo;
    }


    public ArrayList<ScrObject> getScrObjList()
    {
        calcDispData();
        return listScrObj;
    }

    public void setCenter_geo(GeoPoint geoPoint)
    {
        center_x = geoPoint.utmx;
        center_y = geoPoint.utmy;
    }

    public void fit_rect(BoundingBox boundingBox)
    {
        if (!boundingBox.isValid()) {
            return;
        }
        double mid_x = (boundingBox.left + boundingBox.right) / 2;
        double mid_y = (boundingBox.bottom + boundingBox.top) / 2;
        setCenter_geo(new GeoPoint(mid_x, mid_y));

        GeoPoint geoLeftBottom = new GeoPoint(boundingBox.left, boundingBox.bottom);
        GeoPoint geoRightTop = new GeoPoint(boundingBox.right, boundingBox.top);

        double scale_width = (geoRightTop.utmx - geoLeftBottom.utmx) / view_width;
        double scale_height = (geoRightTop.utmy - geoLeftBottom.utmy) / view_height;

        scale = (float) (Math.max(scale_width, scale_height));

    }

    public void setDrawMapType(int type)
    {
        drawMapType = type;
    }

}
