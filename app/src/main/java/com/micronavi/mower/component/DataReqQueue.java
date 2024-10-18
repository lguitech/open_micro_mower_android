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

import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DataReqQueue {
    private static final String TAG = "DataReqQueue";
    private final LinkedList<String> queue= new LinkedList<>();
    private final Semaphore semaphore = new Semaphore(0);

    public void put(String strData) {
        queue.addLast(strData);
        semaphore.release(1);
    }

    public String get() {
        boolean ret = false;
        try {
            ret = semaphore.tryAcquire(3, TimeUnit.SECONDS);
        }
        catch ( InterruptedException e) {
            ret = false;
        }
        if (ret) {
            return queue.removeFirst();
        }
        else {
            return null;
        }
    }
}
