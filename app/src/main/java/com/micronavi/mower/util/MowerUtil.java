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

public class MowerUtil {
    static public double calcDistance(GeoPoint pt1, GeoPoint pt2)
    {
        double dx = pt2.utmx - pt1.utmx;
        double dy = pt2.utmy - pt1.utmy;
        return Math.sqrt(dx * dx + dy * dy);
    }

}
