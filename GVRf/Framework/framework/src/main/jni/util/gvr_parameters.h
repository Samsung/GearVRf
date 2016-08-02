/***************************************************************************
 * Parameters related enumeration
 ***************************************************************************/

#ifndef GVR_PARAMETERS_H_
#define GVR_PARAMETERS_H

namespace gvr {
enum ColorFormat {
    COLOR_565 = 0,
    COLOR_5551 = 1,
    COLOR_4444 = 2,
    COLOR_8888 = 3,
    COLOR_8888_sRGB = 4
};

enum DepthFormat {
    DEPTH_0 = 0, //No depth buffer
    DEPTH_16 = 1, //16-bit depth buffer
    DEPTH_24 = 2, //24-bit depth buffer
    DEPTH_24_STENCIL_8 = 3 //32-bit depth buffer
};

}

#endif
