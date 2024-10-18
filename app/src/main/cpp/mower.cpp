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

#include <jni.h>
#include <string>
#include "string.h"
#include "minilzo.h"

using namespace std;
extern "C" JNIEXPORT jstring JNICALL
Java_com_micronavi_mower_util_ZipTool_stringFromJNI(JNIEnv* env, jobject)
{
    string hello = "Hello from c++  ****  ";
    return env->NewStringUTF(hello.c_str());
}


extern "C" JNIEXPORT int JNICALL
Java_com_micronavi_mower_util_ZipTool_nativeCompress(JNIEnv* env, jobject,
                                                     jbyteArray input, jint len_input,
                                                     jbyteArray output)
{

    unsigned char* szInput = (unsigned char*) env->GetByteArrayElements(input, NULL);


    unsigned char* szBuffer = (unsigned char*)malloc(8*1024*1024);
    unsigned long len_output;
    char pZipWorkBuf[LZO1X_1_MEM_COMPRESS];
    lzo1x_1_compress((const unsigned char*)szInput, len_input, szBuffer, &len_output, (void*)pZipWorkBuf);

    env->SetByteArrayRegion(output,0, len_output, (jbyte*)szBuffer);

    env->ReleaseByteArrayElements(input, (jbyte*)szInput, 0);

    free(szBuffer);

    return len_output;
}

extern "C" JNIEXPORT int JNICALL
Java_com_micronavi_mower_util_ZipTool_nativeDecompress(JNIEnv* env, jobject,
                                                     jbyteArray input, jint len_input,
                                                     jbyteArray output)
{
    unsigned char* szInput = (unsigned char*) env->GetByteArrayElements(input, NULL);
    unsigned char* szBuffer = (unsigned char*)malloc(8*1024*1024);
    unsigned long len_output;

    lzo1x_decompress((const unsigned char*)szInput, len_input, szBuffer, &len_output, NULL);

    env->SetByteArrayRegion(output,0, len_output, (jbyte*)szBuffer);

    env->ReleaseByteArrayElements(input, (jbyte*)szInput, 0);

    free(szBuffer);

    return len_output;
}


/*
#include "test.h"
extern "C" JNIEXPORT int JNICALL
Java_com_micronavi_mower_util_ZipTool_nativeCompress(JNIEnv* env, jobject,
                                                     jbyteArray input, jint len_input,
                                                     jbyteArray output)
{


    unsigned long len_output;

    int aa = doTest(100, 200);


    return len_output;
}

extern "C" JNIEXPORT int JNICALL
Java_com_micronavi_mower_util_ZipTool_nativeDecompress(JNIEnv* env, jobject,
                                                       jbyteArray input, jint len_input,
                                                       jbyteArray output)
{
    unsigned long len_output;

    int aa = doTest(100, 200);


    return len_output;
}

 */