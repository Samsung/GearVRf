#ifndef _VRLogger_h
#define _VRLogger_h

// b.mathew, 22-04-2014

#include <android/log.h>

#define ERROR 1
#define WARNING 0
#define INFO 0
#define ALL 0
#define NONE 0

#define  LOG_TAG    "gl_code"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define VR_LOGE(msg, ...) if((ERROR || ALL) && !NONE) \
					LOGI("VR ERROR : %s : Line No = %d : "msg, __PRETTY_FUNCTION__,__LINE__,## __VA_ARGS__)

#define VR_LOGW(msg, ...) if((WARNING || ALL) && !NONE) \
					LOGI("VR WARNING : %s : Line No = %d : "msg, __PRETTY_FUNCTION__,__LINE__,## __VA_ARGS__)

#define VR_LOGI(msg, ...) if((INFO || ALL) && !NONE) \
					LOGI("VR INFO : %s : Line No = %d : "msg, __PRETTY_FUNCTION__,__LINE__,## __VA_ARGS__)


#endif 
