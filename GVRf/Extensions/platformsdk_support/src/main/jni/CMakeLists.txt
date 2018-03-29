cmake_minimum_required(VERSION 3.4.1)

project (gvrf-platformsdk C CXX)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fexceptions")
add_library(gvrf-platformsdk SHARED
    platform_entitlement_check_jni.cpp)

include_directories(${OVR_PLATFORM_SDK}/Include)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../../Framework/framework/src/main/jni)

string(TOLOWER ${CMAKE_BUILD_TYPE} buildType)

add_library(ovrplatformloader SHARED IMPORTED)
set_property(TARGET ovrplatformloader PROPERTY IMPORTED_LOCATION
             ${OVR_PLATFORM_SDK}/Android/libs/armeabi-v7a/libovrplatformloader.so)

target_link_libraries(gvrf-platformsdk ovrplatformloader log EGL GLESv3)
