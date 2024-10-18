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

public class BoundingBox {
    private static final double TOO_SMALL_THRESHOLD = 1;
    public double left;
    public double bottom;
    public double right;
    public double top;

    public boolean isValid()
    {
        if (left <= right && bottom <=top) {
            return true;
        }
        else {
            return false;
        }
    }

    public BoundingBox()
    {
        left = bottom = Double.MAX_VALUE;
        right = top = Double.MIN_VALUE;
    }

    public void reset()
    {
        left = bottom = Double.MAX_VALUE;
        right = top = Double.MIN_VALUE;
    }

    public boolean isTooSmall()
    {
        double min_bound = Math.min(Math.abs(left - right), Math.abs(top - bottom));
        if (min_bound > TOO_SMALL_THRESHOLD) {
            return true;
        }
        else {
             return false;
        }

    }
}
