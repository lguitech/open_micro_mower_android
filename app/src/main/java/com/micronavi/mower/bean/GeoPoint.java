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

import com.micronavi.mower.util.CoordUtil2;

public class GeoPoint {
    public double x;
    public double y;
    public double utmx;
    public double utmy;
    public int angle;

    public GeoPoint()
    {
        this.x = 0;
        this.y = 0;
        this.utmx = 0;
        this.utmy = 0;
    }
    public GeoPoint(double x, double y) {
        this.x = x;
        this.y = y;
        CoordUtil2.getInstance().trans(this);
    }
    public GeoPoint(double x, double y, int angle) {
        this.x = x;
        this.y = y;
        CoordUtil2.getInstance().trans(this);
        this.angle = angle;
    }

     public GeoPoint(GeoPoint param)
    {
        this.x = param.x;
        this.y = param.y;
        this.utmx = param.utmx;
        this.utmy = param.utmy;
        this.angle = param.angle;
    }
    public void setValue(GeoPoint param)
    {
        this.x = param.x;
        this.y = param.y;
        this.utmx = param.utmx;
        this.utmy = param.utmy;
        this.angle = param.angle;
    }

    public void setValue(double x, double y)
    {
        this.x = x;
        this.y = y;
        CoordUtil2.getInstance().trans(this);
    }

    public boolean isEqual(GeoPoint param)
    {
        return (this.x == param.x) && (this.y == param.y);
    }
}
