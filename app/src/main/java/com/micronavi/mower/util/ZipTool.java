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

public class ZipTool {
    static {
        System.loadLibrary("mower");
    }
    
    public String getString() {
        return stringFromJNI();
    }

    public int doCompress(byte[] byteInput, int len, byte[] byteOutput)
    {
        return nativeCompress(byteInput, len, byteOutput);
    }

    public int doDecompress(byte[] byteInput, int len ,byte[] byteOutput)
    {
        return nativeDecompress(byteInput, len, byteOutput);
    }

    public native String stringFromJNI();

    public native int nativeCompress(byte[] byteInput, int len, byte[] byteOutput);
    public native int nativeDecompress(byte[] byteInput, int len, byte[] byteOutput);
}
