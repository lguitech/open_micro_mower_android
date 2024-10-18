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

package com.micronavi.mower.util;


import com.micronavi.mower.bean.GeoPoint;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

public class CoordUtil2 {
    private static final int DEFAULT_ZONE = 50;
    private static final boolean IS_SOUTH = false;
    private int zone;
    private boolean is_south;
    private CoordinateTransform wgsToUtm;
    private CoordinateTransform utmToWgs;
    private static final CoordUtil2 instance = new CoordUtil2();
    public static CoordUtil2 getInstance()
    {
        return instance;
    }

    private CoordUtil2()
    {
        init(DEFAULT_ZONE, IS_SOUTH);
    }

    public void resetParam(double lon, double lat)
    {
        init(lon, lat);
    }

    private void init(double lon, double lat)
    {
        int zone = (int)((lon+186.0)/6.0);
        boolean isSouth = lat < 0;
        init(zone, isSouth);
    }

    private void init(int zone, boolean isSouth)
    {
        CRSFactory crsFactory = new CRSFactory();
        String strParam = "+proj=longlat +datum=WGS84 +no_defs";
        CoordinateReferenceSystem WGS84 = crsFactory.createFromParameters("WGS84", strParam);

        strParam = "+proj=utm +zone=" + zone;
        if (isSouth) {
            strParam += " +south";
        }
        strParam += " +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs";
        //strParam = "+proj=utm +zone=" + zone + " +ellps=WGS84 +datum=WGS84 +units=m +no_defs";
        CoordinateReferenceSystem UTM = crsFactory.createFromParameters("UTM", strParam );

        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        wgsToUtm = ctFactory.createTransform(WGS84, UTM);
        utmToWgs = ctFactory.createTransform(UTM, WGS84);
        this.zone = zone;
        this.is_south = isSouth;
    }

    public void trans(GeoPoint geoPoint)
    {
        int zone = (int) ((geoPoint.x + 186.0) / 6.0);
        boolean isSouth = geoPoint.y < 0;
        if (zone != this.zone || this.is_south != isSouth) {
            init(zone, isSouth);
        }
        ProjCoordinate result = new ProjCoordinate();
        try {
            wgsToUtm.transform(new ProjCoordinate(geoPoint.x, geoPoint.y), result);
            geoPoint.utmx = result.x;
            geoPoint.utmy = result.y;
        }
        catch (Exception e) {
            geoPoint.utmx = 0.0;
            geoPoint.utmy = 0.0;
        }
    }

}
