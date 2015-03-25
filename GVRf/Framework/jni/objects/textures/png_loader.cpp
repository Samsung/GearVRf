/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/***************************************************************************
 * The PNG loader
 ***************************************************************************/
#include "png_loader.h"

#include "util/gvr_log.h"
#include <pngconf.h>

#if PNG_LIBPNG_VER >= 10400 && PNG_LIBPNG_VER <= 10502 \
    && defined(PNG_PEDANTIC_WARNINGS_SUPPORTED)

#undef png_jmpbuf
#ifdef PNG_SETJMP_SUPPORTED
#define png_jmpbuf(png_ptr) \
    (*png_set_longjmp_fn((png_ptr), (png_longjmp_ptr)longjmp, sizeof(jmp_buf)))
#else
#define png_jmpbuf(png_ptr) \
    (LIBPNG_WAS_COMPILED_WITH__PNG_NO_SETJMP)
#endif
#endif

#define FAST_SCAN_LINE(data, bpl, y) (data + (y) * bpl)

static void iod_read_fn(png_structp png_ptr, png_bytep data,
        png_size_t length) {
    AAsset* asset = (AAsset *) png_get_io_ptr(png_ptr);
    AAsset_read(asset, data, length);
}

static void setup_image(PngLoader::ImageData &image, png_structp png_ptr,
        png_infop info_ptr, float screen_gamma = 0.0) {
    if (screen_gamma != 0.0
            && png_get_valid(png_ptr, info_ptr, PNG_INFO_gAMA)) {
        double file_gamma;
        png_get_gAMA(png_ptr, info_ptr, &file_gamma);
        png_set_gamma(png_ptr, screen_gamma, file_gamma);
    }

    png_uint_32 width;
    png_uint_32 height;
    int bit_depth;
    int color_type;
    png_bytep trans_alpha = 0;
    png_color_16p trans_color_p = 0;
    int num_trans;
    png_colorp palette = 0;
    int num_palette;
    int interlace_method;
    png_get_IHDR(png_ptr, info_ptr, &width, &height, &bit_depth, &color_type,
            &interlace_method, 0, 0);
    png_set_interlace_handling(png_ptr);

    if (color_type == PNG_COLOR_TYPE_GRAY) {
        // Black & White or 8-bit grayscale
        if (bit_depth == 1 && png_get_channels(png_ptr, info_ptr) == 1) {
            png_set_invert_mono(png_ptr);
            png_read_update_info(png_ptr, info_ptr);

            image.width = width;
            image.height = height;
            image.bits = (unsigned char*) malloc(
                    width * height * sizeof(unsigned char));
            image.format = PngLoader::GrayFormat;

        } else if (bit_depth == 16
                && png_get_valid(png_ptr, info_ptr, PNG_INFO_tRNS)) {
            png_set_expand(png_ptr);
            png_set_strip_16(png_ptr);
            png_set_gray_to_rgb(png_ptr);

            image.width = width;
            image.height = height;
            image.bits = (unsigned char*) malloc(
                    width * height * 4 * sizeof(unsigned char));
            image.format = PngLoader::RGBAFormat;

            png_read_update_info(png_ptr, info_ptr);
        } else {

            LOGE("PNG Format not supported");

        }
    } else if (color_type == PNG_COLOR_TYPE_PALETTE
            && png_get_PLTE(png_ptr, info_ptr, &palette, &num_palette)
            && num_palette <= 256) {
        // 1-bit and 8-bit color
        if (bit_depth != 1)
            png_set_packing(png_ptr);
        png_read_update_info(png_ptr, info_ptr);
        png_get_IHDR(png_ptr, info_ptr, &width, &height, &bit_depth,
                &color_type, 0, 0, 0);
        PngLoader::ImageFormat format;
        if (bit_depth == 1) {
            format = PngLoader::GrayFormat;

            image.width = width;
            image.height = height;
            image.bits = (unsigned char*) malloc(
                    width * height * sizeof(unsigned char));
            image.format = PngLoader::GrayFormat;
        } else {
            LOGE("PNG Format Indexed8 not supported");
        }

    } else {
        // 32-bit
        if (bit_depth == 16)
            png_set_strip_16(png_ptr);

        png_set_expand(png_ptr);

        if (color_type == PNG_COLOR_TYPE_GRAY_ALPHA)
            png_set_gray_to_rgb(png_ptr);

        // Format_ARGB32;
        PngLoader::ImageFormat format = PngLoader::RGBAFormat;
        // Only add filler if no alpha, or we can get 5 channel data.
        if (!(color_type & PNG_COLOR_MASK_ALPHA)
                && !png_get_valid(png_ptr, info_ptr, PNG_INFO_tRNS)) {

            png_set_filler(png_ptr, 0xff,
#ifdef QX_PLATFORM_MACOSX
                    PNG_FILLER_BEFORE
#else
                    PNG_FILLER_AFTER
#endif
                    );
            // We want 4 bytes, but it isn't an alpha channel (XRGB32)
            image.width = width;
            image.height = height;
            image.bits = (unsigned char*) malloc(
                    width * height * 4 * sizeof(unsigned char));
            image.format = PngLoader::RGBAFormat;

            return;
        }

        image.width = width;
        image.height = height;
        image.bits = (unsigned char*) malloc(
                width * height * 4 * sizeof(unsigned char));
        image.format = PngLoader::RGBAFormat;

        png_read_update_info(png_ptr, info_ptr);
    }

}

static void gvrf_png_warning(png_structp, png_const_charp message) {
    LOGE("libpng warning: %s", message);
}

void PngLoader::loadFromAsset(AAsset *file) {
    pFileDescriptor = file;
    pOutImage.bits = NULL;

    png_ptr = png_create_read_struct(PNG_LIBPNG_VER_STRING, 0, 0, 0);
    if (!png_ptr)
        return;

    png_set_error_fn(png_ptr, 0, 0, gvrf_png_warning);

    info_ptr = png_create_info_struct(png_ptr);
    if (!info_ptr) {
        png_destroy_read_struct(&png_ptr, 0, 0);
        png_ptr = 0;
        return;
    }

    end_info = png_create_info_struct(png_ptr);
    if (!end_info) {
        png_destroy_read_struct(&png_ptr, &info_ptr, 0);
        png_ptr = 0;
        return;
    }

    if (setjmp(png_jmpbuf(png_ptr))) {
        png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
        png_ptr = 0;
        return;
    }

    png_set_read_fn(png_ptr, file, iod_read_fn);
    png_read_info(png_ptr, info_ptr);

    if (setjmp(png_jmpbuf(png_ptr))) {
        png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
        png_ptr = 0;
        amp.deallocate();
        return;
    }

    setup_image(pOutImage, png_ptr, info_ptr, gamma);

    if (pOutImage.bits == NULL) {
        png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
        png_ptr = 0;
        amp.deallocate();
        return;
    }

    png_uint_32 width;
    png_uint_32 height;
    int bit_depth;
    int color_type;
    png_get_IHDR(png_ptr, info_ptr, &width, &height, &bit_depth, &color_type, 0,
            0, 0);
    unsigned char *data = pOutImage.bits;
    int bpp = 0;
    switch (pOutImage.format) {
    case GrayFormat:
        bpp = 1;
        break;
    case RGBFormat:
        bpp = 3;
        break;
    case RGBAFormat:
        bpp = 4;
        break;
    default:
        break;
    }
    int bpl = bpp * pOutImage.width;
    amp.row_pointers = new png_bytep[height];

    for (uint y = 0; y < height; y++)
        amp.row_pointers[y] = data + y * bpl;

    png_read_image(png_ptr, amp.row_pointers);
    amp.deallocate();

    png_read_end(png_ptr, end_info);

    png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
    png_ptr = 0;
    amp.deallocate();
}

