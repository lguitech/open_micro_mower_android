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


import androidx.annotation.NonNull;

public class ScrPoint {
    public int x;
    public int y;
    public int angle;

    public ScrPoint() {}

    public ScrPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public ScrPoint(int x, int y, int angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }
    public ScrPoint(@NonNull ScrPoint src) {
        this.x = src.x;
        this.y = src.y;
        this.angle = src.angle;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public void set(int x, int y, int angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }
}
