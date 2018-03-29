cmake_minimum_required(VERSION 3.4.1)

project (gvrf-physics C CXX)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fexceptions")
add_library(gvrf-physics SHARED
    engine/bullet/bullet_gvr_utils.cpp
    engine/bullet/bullet_rigidbody.cpp
    engine/bullet/bullet_world.cpp
    engine/bullet/bullet_fixedconstraint.cpp
    engine/bullet/bullet_point2pointconstraint.cpp
    engine/bullet/bullet_hingeconstraint.cpp
    engine/bullet/bullet_sliderconstraint.cpp
    engine/bullet/bullet_conetwistconstraint.cpp
    engine/bullet/bullet_generic6dofconstraint.cpp
    engine/physics_world_jni.cpp
    engine/physics_rigidbody_jni.cpp
    engine/physics_constraint_jni.cpp
    engine/physics_fixedconstraint_jni.cpp
    engine/physics_point2pointconstraint_jni.cpp
    engine/physics_hingeconstraint_jni.cpp
    engine/physics_sliderconstraint_jni.cpp
    engine/physics_conetwistconstraint_jni.cpp
    engine/physics_genericconstraint_jni.cpp)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/bullet3/include)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../../Framework/framework/src/main/jni)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../../Framework/framework/src/main/jni/contrib)

string(TOLOWER ${CMAKE_BUILD_TYPE} buildType)

add_library(gvrf SHARED IMPORTED)
set_property(TARGET gvrf PROPERTY IMPORTED_LOCATION
             ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../Framework/framework/build/intermediates/ndkBuild/${buildType}/obj/local/${ANDROID_ABI}/libgvrf.so)

add_library(Bullet SHARED IMPORTED)
set_property(TARGET Bullet PROPERTY IMPORTED_LOCATION
             ${CMAKE_CURRENT_SOURCE_DIR}/prebuilt/${ANDROID_ABI}/libBullet.so)

target_link_libraries(gvrf-physics gvrf log Bullet EGL GLESv3)
