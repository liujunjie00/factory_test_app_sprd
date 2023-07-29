/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "validation_jni"
#include "utils/Log.h"

#include <stdint.h>
#include <jni.h>

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <malloc.h>


#define ROOT_MAGIC 0x524F4F54 //"ROOT"
#define ROOT_OFFSET 512
#define MAX_COMMAND_BYTES               (8 * 1024)

#ifdef USE_AUDIO_WHALE_HAL
#define AUDIO_WHALE_LOOPBACK 1
#endif

#ifndef USE_AUDIO_WHALE_HAL
#define AUDIO_WHALE_LOOPBACK 0
#endif

#ifdef TARGET_CAMERA_SENSOR_TOF_SUPPORT
#define CAMERA_SENSOR_TOF_SUPPORT 1
#endif

#ifndef TARGET_CAMERA_SENSOR_TOF_SUPPORT
#define CAMERA_SENSOR_TOF_SUPPORT 0
#endif

#ifdef TARGET_CAMERA_SENSOR_CCT_TCS3430
#define TARGET_CAMERA_SENSOR_CCT_TCS3430_FLAG 1
#endif

#ifndef TARGET_CAMERA_SENSOR_CCT_TCS3430
#define TARGET_CAMERA_SENSOR_CCT_TCS3430_FLAG 0
#endif


//1,mean support,other mean not support.
static jint Is_support_macro(JNIEnv* env, jobject thiz,jstring jmacro_name) {
    int ret = -1;
    const char* macro_name = env->GetStringUTFChars(jmacro_name,0);
    ALOGI("Is_support_macro macro_name=:%s", macro_name);
    if(macro_name == NULL){
        return -1;
    }
#ifdef BOARD_FEATUREPHONE_CONFIG
    ALOGI("Is_support_macro BOARD_FEATUREPHONE_CONFIG");
    if(strcmp(macro_name,"BOARD_FEATUREPHONE_CONFIG") == 0){
        return 1;
    }
#endif
    ALOGI("Is_support_macro ret=:%d", ret);
    return ret;
}

static jstring Validation_sendATCmd(JNIEnv* env, jobject thiz, jint phoneId, jstring cmd) {
   char result[MAX_COMMAND_BYTES] = {0};
   return env->NewStringUTF(result);
}

static jint get_audio_whale_loopback_flag (JNIEnv* env, jobject thiz) {
    ALOGI("get_audio_whale_loopback_flag...AUDIO_WHALE_LOOPBACK=:%d", AUDIO_WHALE_LOOPBACK);
    return AUDIO_WHALE_LOOPBACK;
}

static jint get_camera_sensor_tof_support_flag (JNIEnv* env, jobject thiz) {
    ALOGI("get_camera_sensor_tof_support_flag...CAMERA_SENSOR_TOF_SUPPORT=:%d", CAMERA_SENSOR_TOF_SUPPORT);
    return CAMERA_SENSOR_TOF_SUPPORT;
}

static jint get_TARGET_CAMERA_SENSOR_CCT_TCS3430_flag (JNIEnv* env, jobject thiz) {
    ALOGI("get_TARGET_CAMERA_SENSOR_CCT_TCS3430_flag...TARGET_CAMERA_SENSOR_CCT_TCS3430_FLAG=:%d", TARGET_CAMERA_SENSOR_CCT_TCS3430_FLAG);
    return TARGET_CAMERA_SENSOR_CCT_TCS3430_FLAG;
}

static const char *hardWareClassPathName =
        "com/sprd/validationtools/utils/Native";

static JNINativeMethod getMethods[] = {
        {"native_sendATCmd","(ILjava/lang/String;)Ljava/lang/String;", (void*) Validation_sendATCmd },
        {"native_get_audio_whale_loopback_flag", "()I", (void*)get_audio_whale_loopback_flag},
        {"native_get_camera_sensor_tof_support_flag", "()I", (void*)get_camera_sensor_tof_support_flag},
        {"native_get_TARGET_CAMERA_SENSOR_CCT_TCS3430_flag", "()I", (void*)get_TARGET_CAMERA_SENSOR_CCT_TCS3430_flag},
		{"native_is_support_macro","(Ljava/lang/String;)I", (void*) Is_support_macro }
};

static int registerNativeMethods(JNIEnv* env, const char* className,
        JNINativeMethod* gMethods, int numMethods) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        ALOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        ALOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }
    return JNI_TRUE;
}
        
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    //use JNI1.6
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        ALOGE("Error: GetEnv failed in JNI_OnLoad");
        return -1;
    }
    if (!registerNativeMethods(env, hardWareClassPathName, getMethods,
            sizeof(getMethods) / sizeof(getMethods[0]))) {
        ALOGE("Error: could not register native methods for HardwareFragment");
        return -1;
    }
      return JNI_VERSION_1_6;
}
