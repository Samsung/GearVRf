cmake_minimum_required(VERSION 3.4.1)

project (gvrf-physics C CXX)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fexceptions")
add_library(gvrf-physics SHARED
    engine/physics/bullet/bullet_gvr_utils.cpp
    engine/physics/bullet/bullet_rigidbody.cpp
    engine/physics/bullet/bullet_world.cpp
    engine/physics/physics3d/physics_3dworld_jni.cpp
    engine/physics/physics3d/physics_3drigidbody_jni.cpp)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/bullet3/include)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../../Framework/framework/src/main/jni)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../../Framework/framework/src/main/jni/contrib)

string(TOLOWER ${CMAKE_BUILD_TYPE} buildType)

add_library(gvrf SHARED IMPORTED)
set_property(TARGET gvrf PROPERTY IMPORTED_LOCATION
             ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../Framework/framework/build/intermediates/ndkBuild/${buildType}/obj/local/${ANDROID_ABI}/libgvrf.so)

add_library(Bullet SHARED IMPORTED)
set_property(TARGET Bullet PROPERTY IMPORTED_LOCATION
             ${CMAKE_CURRENT_SOURCE_DIR}/prebuilt/armeabi-v7a/libBullet.so)

target_link_libraries(gvrf-physics gvrf log Bullet GLESv3)