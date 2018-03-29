cmake_minimum_required(VERSION 3.4.1)

project (gvrf-daydream C CXX)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fexceptions")
add_library(gvrf-daydream SHARED daydream_renderer.cpp daydream_renderer_jni.cpp)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../framework/src/main/jni)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../framework/src/main/jni/contrib)
include_directories(${CMAKE_CURRENT_SOURCE_DIR})

string(TOLOWER ${CMAKE_BUILD_TYPE} buildType)

add_library(gvrf SHARED IMPORTED)
set_property(TARGET gvrf PROPERTY IMPORTED_LOCATION
             ${CMAKE_CURRENT_SOURCE_DIR}/../../../../framework/build/intermediates/ndkBuild/${buildType}/obj/local/${ANDROID_ABI}/libgvrf.so)


add_library(gvr SHARED IMPORTED)
set_property(TARGET gvr PROPERTY IMPORTED_LOCATION
            ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libgvr.so)

target_link_libraries(gvrf-daydream gvr gvrf log GLESv2 EGL)