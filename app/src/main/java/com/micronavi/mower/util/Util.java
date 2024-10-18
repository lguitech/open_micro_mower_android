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

public class Util {
    private static final String TAG = "Util";

    public static byte[] int2byte(int value)
    {
        byte[] byteResult = new byte[4];
        byteResult[0] = (byte)(value & 0xff);
        byteResult[1] = (byte)(value >> 8 & 0xff);
        byteResult[2] = (byte)(value >> 16 & 0xff);
        byteResult[3] = (byte)(value >> 24 & 0xff);

        return byteResult;
    }

    public static int byte2int(byte[] byteData)
    {
        int result = 0;
        for (int i=0; i<4; i++) {
            result += (byteData[i] & 0xff) << ( i * 8);
        }
        return result;
    }


}