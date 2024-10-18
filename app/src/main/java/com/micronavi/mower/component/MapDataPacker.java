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


import com.micronavi.mower.util.ZipTool;
import java.nio.charset.StandardCharsets;

public class MapDataPacker {
    private static  final String TAG = "route_desc";
    private static final int MAX_LEN = 2 * 1024 * 1024;
    private byte[] byteData;
    private int len_data;

    private static MapDataPacker instance = null;

    public static MapDataPacker getInstance()
    {
        if (instance == null) {
            instance = new MapDataPacker();
        }
        return instance;
    }


    public void setMapData(String strValue)
    {
        ZipTool zipTool = new ZipTool();
        byte[] byteArray = strValue.getBytes(StandardCharsets.UTF_8);
        byte[] byteOutput = new byte[MAX_LEN];
        len_data = zipTool.doCompress(byteArray, byteArray.length, byteOutput);
        byteData = new byte[len_data];
        System.arraycopy(byteOutput, 0, byteData, 0, len_data);
    }


    public boolean getDataSection(byte[] byteParam, int pos_start, int index_begin, int index_end)
    {
        if (index_begin >= 0 && index_end < len_data) {
            System.arraycopy(byteData, index_begin, byteParam, pos_start, index_end - index_begin + 1);
            return true;
        }
        else {
            return false;
        }
    }
    public int getDataLen()
    {
        return len_data;
    }


}
