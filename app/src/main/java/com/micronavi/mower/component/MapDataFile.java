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


import android.util.Log;
import com.micronavi.mower.application.MowerApplication;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class MapDataFile {
    private static final String TAG = "MapDataFile";
    private static final String strFileName = "map.dat";
    public static String readMapFromFile()  {
        try {
            //String strFilePath = MowerApplication.getContext().getFilesDir().getPath();
            File file = new File(MowerApplication.getContext().getExternalFilesDir(null), strFileName);
            FileInputStream fileInputStream = new FileInputStream(file);
            Reader reader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            StringBuilder result = new StringBuilder();

            String temp;
            while ((temp = bufferedReader.readLine()) != null) {
                result.append(temp);
            }
            return result.toString();
        }
        catch (IOException e) {
            return null;
        }
    }
    public static void deleteMapFile()
    {
        File file = new File(MowerApplication.getContext().getExternalFilesDir(null), strFileName);
        if (file.exists()) {
            file.delete();
        }
    }
    public static boolean writeMapToFile(String strContent)  {

        String strFilePath = "";
        try {
            File file = new File(MowerApplication.getContext().getExternalFilesDir(null), strFileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(strContent.getBytes());
            fileOutputStream.close();
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }
}
