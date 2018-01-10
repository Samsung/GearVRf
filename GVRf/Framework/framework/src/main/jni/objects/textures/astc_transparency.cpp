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
 * JNI
 ***************************************************************************/

#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <inttypes.h>

#include "astc_transparency.h"

namespace gvr {

/*
 * This file will extensively reference the documentation in the following URL:
 *
 * https://www.khronos.org/registry/OpenGL/extensions/KHR/KHR_texture_compression_astc_hdr.txt
 *
 * Any 'section', 'table' or 'figure' referenced will be referring to that URL.
 *
 */
#define EXTRA_VERBOSE_LOGGING 0
#define MAX_WEIGHTS 64     // found from Section C.2.24 Illegal Encodings
#define MIN_WEIGHT_BITS 24 // found from Section C.2.24 Illegal Encodings
#define MAX_WEIGHT_BITS 96 // found from Section C.2.24 Illegal Encodings
//#define HAS_128BIT 1
 
int MAGIC_NUMBER = 0x5CA1AB13;  // for identifying the format.

char default_filename[] = "astc_transparency.astc";

typedef struct astc_header
{
    uint8_t magic[4];
    uint8_t blockdim_x;
    uint8_t blockdim_y;
    uint8_t blockdim_z;
    uint8_t xsize[3];
    uint8_t ysize[3];
    uint8_t zsize[3];
} astc_header_t;

bool hasAlpha(int color_endpoint_mode)
{
    bool alpha = false;
    // Section C.2.11
    switch(color_endpoint_mode)
    {
        case 4:    // LDR Luminance+Alpha, direct
        case 5:    // LDR Luminance+Alpha, base+offset
        case 10:   // LDR RGB, base+scale plus two A
        case 12:   // LDR RGBA, direct
        case 13:   // LDR RGBA, base+offset
        case 14:   // HDR RGB, direct + LDR Alpha
        case 15:   // HDR RGB, direct + HDR Alpha
            alpha = true;
            break;
        default:
            alpha = false;
            break;
    }

    return alpha;
}

typedef struct decoded_block_ {
    int num_weights;
    int weight_bits;
    int is_dual_plane;
    int is_valid_block;
    int width;
    int height;
} decoded_block_t;

void decode_r_value(int full_R_value, int H, int *trits, int *quints, int *bits)
{
    // To understand 'trits', 'quints', and 'bits' you must read the section C.2.12 on
    // Integer Sequence Encoding.  
    //
    // In short a 'trit' is a base-3 value.  You can pack 5 'trits' into 8 bits. 
    // A 'quint' is a base-5 value.  You can pack 3 'quints' into 7 bits.
    // Thankfully, a 'bit' is literally a bit :).
    //
    // The 'R' value referenced here is composed of (R2 | R1 | R0) from 
    // Table C.2.8 - 2D Block Mode Layout
    //
    // The 'R' value tells us the weight range and the number of 'trits', 'quints' and 'bits'
    // used to represent the weight.  See Table C.2.7 - Weight Range Encodings
    // 
    // 'H' represents the HDR bit and is also from the 2D Block Mode Layout (Table C.2.8)
    //
    *trits = 0;
    *quints = 0;
    *bits = 0;

    if(H == 0)
    {
        switch(full_R_value)
        {
            case 2:
                *bits = 1;
                break;
            case 3:
                *trits = 1;
                break;
            case 4:
                *bits = 2;
                break;
            case 5:
                *quints = 1;
                break;
            case 6:
                *trits = 1;
                *bits = 1;
                break;
            case 7:
                *bits = 3;
                break;
        }
    }
    else
    {
        switch(full_R_value)
        {
            case 2:
                *quints = 1;
                *bits = 1;
                break;
            case 3:
                *trits = 1;
                *bits = 2;
                break;
            case 4:
                *bits = 4;
                break;
            case 5:
                *quints = 1;
                *bits = 2;
                break;
            case 6:
                *trits = 1;
                *bits = 3;
                break;
            case 7:
                *bits = 5;
                break;
        }
    }
}

// This will decode the information stored in the "block_mode" section of an ASTC block.
// It will tell us whether the block contains HDR or LDR data, whether it contains dual-plane
// data, the width and height of the number of texels in the block, and the number of bits
// of the weights used to encode the block.
//
// See: Section C.2.10 Block Mode
//    and
//    : Table C.2.8 - 2D Block Mode Layout
//
// A number of the variables used here correspond to the variables used in that Table.
//
void decode_block(uint32_t block_mode, decoded_block_t *decoded_block)
{
    int first_two_bits = (block_mode & 0x3);
    int r0 = 0;
    int r1 = 0;
    int r2 = 0;
    int selector = 0;
    int  A = 0;
    int  B = 0;
    int  H = 0;
    int  D = 0;

    // If the first two bits are 0, then the bottom 5 rows of the
    // table apply...
    if(first_two_bits == 0x00)
    {
        // The 'selector' is bits 7 & 8 which distinguish which row
        // of the table applies to retrieve the values we're looking for.
        selector == (block_mode >> 7) & 0x3;
        r1 = (block_mode >> 2) & 0x1;
        r2 = (block_mode >> 3) & 0x1;
        r0 = (block_mode >> 4) & 0x1;
        A = (block_mode >> 5) & 0x3;

        switch(selector)
        {
            case 0x0:
                D = (block_mode >> 9) & 0x1;
                H = (block_mode >> 10) & 0x1;
                decoded_block->width = 12;
                decoded_block->height = A + 2;
                break;
            case 0x1:
                D = (block_mode >> 9) & 0x1;
                H = (block_mode >> 10) & 0x1;
                decoded_block->width = A + 2;
                decoded_block->height = 12;
                break;
            case 0x2:
                B = (block_mode >> 9) & 0x3;
                decoded_block->width = A + 6;
                decoded_block->height = B + 6;
                break;
            case 0x3:
                if(A == 0)
                {
                    D = (block_mode >> 9) & 0x1;
                    H = (block_mode >> 10) & 0x1;
                    decoded_block->width = 6;
                    decoded_block->height = 10;
                }
                else
                {
                    D = (block_mode >> 9) & 0x1;
                    H = (block_mode >> 10) & 0x1;
                    decoded_block->width = 10;
                    decoded_block->height = 6;
                }
                break;
        }

    }
    else // The first 5 rows of the table apply here.
    {   
        r1 = block_mode & 0x1;
        r2 = (block_mode >> 1) & 0x1;
        // the 'selector' is bits 2 & 3 which distinguish which
        // row of the table to reference for retrieving values.
        selector = (block_mode >> 2) & 0x3;
        r0 = (block_mode >> 4) & 0x1;
        A = (block_mode >> 5) & 0x3;
        B = 0;
        H = 0;
        D = 0;

        switch(selector)
        {
            case 0x0:
                B = (block_mode >> 7) & 3;
                H = (block_mode >> 9) & 1;
                D = (block_mode >> 10) & 1;
                decoded_block->width = B + 4;
                decoded_block->height = A + 2;
                break;
            case 0x1:
                B = (block_mode >> 7) & 3;
                H = (block_mode >> 9) & 1;
                D = (block_mode >> 10) & 1;
                decoded_block->width = B + 8;
                decoded_block->height = A + 2;
                break;
            case 0x2:
                B = (block_mode >> 7) & 3;
                H = (block_mode >> 9) & 1;
                D = (block_mode >> 10) & 1;
                decoded_block->width = A + 2;
                decoded_block->height = B + 8;
                break;
            case 0x3:
                B = (block_mode >> 7) & 1;
                int bit = (block_mode >> 8) & 1;
                H = (block_mode >> 9) & 1;
                D = (block_mode >> 10) & 1;
                if(bit)
                {
                    decoded_block->width = B + 2;
                    decoded_block->height = A + 2;
                }
                else
                {
                    decoded_block->width = A + 2;
                    decoded_block->height = B + 6;
                }
                break;

        }

    }

    // The number of weights will be the width * height of the block.  It'll be doubled if
    // this block contains dual-plane information.
    decoded_block->num_weights = decoded_block->width * decoded_block->height * (D+1);
    decoded_block->is_dual_plane = D;

#if EXTRA_VERBOSE_LOGGING
    LOGD("width: %0X\n", decoded_block->width);
    LOGD("height: %0X\n", decoded_block->height);
    LOGD("D: %0X\n", D);

    LOGD("num_weights: %0X\n", decoded_block->num_weights);
    LOGD("is_dual_plane: %0X\n", decoded_block->is_dual_plane);
#endif

    int trits;
    int quints;
    int bits;
    int num_weight_bits;

    // The 'R' value is composed of three bits that get extracted from the table.
    // The 'R' value determines how many bits are used for weights.
    int full_R_value = r0 | (r1 << 1) | (r2 << 2);
    decode_r_value(full_R_value, H, &trits, &quints, &bits);

#if EXTRA_VERBOSE_LOGGING
    LOGD("full_R_value: %0X\n", full_R_value);
    LOGD("H: %0X\n", H);
    LOGD("trits: %0X\n", trits);
    LOGD("quints: %0X\n", quints);
    LOGD("bits: %0X\n", bits);
#endif

    if(trits)
    {
        num_weight_bits = ((8 + bits * 5) * decoded_block->num_weights + 4) / 5;
    }
    else if(quints)
    {
        num_weight_bits = ((7 + bits * 3) * decoded_block->num_weights + 2) / 3;
    }
    else
    {
        num_weight_bits = bits * decoded_block->num_weights;
    }
#if EXTRA_VERBOSE_LOGGING
    LOGD("num_weight_bits: %0X\n", num_weight_bits);
#endif

    decoded_block->weight_bits = num_weight_bits;

    // initially mark this block as valid. 
    // But if it is ouside of the constraints, mark as invalid so 
    // we can skip to the next block.
    decoded_block->is_valid_block = 1;
    if(decoded_block->num_weights > MAX_WEIGHTS ||
       num_weight_bits < MIN_WEIGHT_BITS ||
       num_weight_bits > MAX_WEIGHT_BITS)
    {
        decoded_block->is_valid_block = 0;
    }

}

#ifndef HAS_128BIT
int extract_color_endpoint_mode_upper_bits(uint64_t input[], int offset, int size)
{
    uint64_t input0 = input[0];
    uint64_t input1 = input[1];

#if EXTRA_VERBOSE_LOGGING
    LOGD("extract_color_endpoint_mode_upper_bits...\n");
    LOGD("  input[0] = %" PRIx64 "\n", input0);
    LOGD("  input[1] = %" PRIx64 "\n", input1);
    LOGD("  offset = %d, size = %d\n", offset, size);
#endif

    int bitsmask = (1 << size) - 1;
    int shift_upper = 0;
    int shift_lower = offset;
    int lowerbits = 0;
    int upperbits = 0;

    if((offset + size) < 64)
    {
#if EXTRA_VERBOSE_LOGGING
    LOGD("  lower\n");
#endif
        lowerbits = size;
        upperbits = 0;
        input0 = 0;
    }
    else if((offset + size) > (64 + size))
    {
#if EXTRA_VERBOSE_LOGGING
    LOGD("  upper\n");
#endif
        lowerbits = 0;
        input1 = 0;
        upperbits = size;
        shift_upper = offset - 64;
    }
    else
    {
#if EXTRA_VERBOSE_LOGGING
    LOGD("  straddle\n");
#endif
        shift_upper = 0;
        upperbits = (offset + size) - 64;
        lowerbits = size - upperbits;
    }

#if EXTRA_VERBOSE_LOGGING
    LOGD("  upperbits = %d\n", upperbits);
    LOGD("  lowerbits = %d\n", lowerbits);
    LOGD("  shift_upper = %d\n", shift_upper);
    LOGD("  shift_lower = %d\n", shift_lower);
#endif

    int upperbitsmask = (1 << upperbits) - 1;
    int lowerbitsmask = (1 << lowerbits) - 1;

    int output_upper = (input0 >> shift_upper) & 0xFFFF;
    int output_lower = (input1 >> shift_lower) & 0xFFFF;

#if EXTRA_VERBOSE_LOGGING
    LOGD("  output_upper = %0X\n", output_upper);
    LOGD("  output_lower = %0X\n", output_lower);
#endif
    int output = ((output_upper << lowerbits) | output_lower) & bitsmask;
#if EXTRA_VERBOSE_LOGGING
    LOGD("  output = %0X\n", output);

    LOGD("done extract_color_endpoint_mode_upper_bits\n");
#endif

    return output;
}
#endif

bool detectAlpha(void *data, int datasize)
{
    int ret = -1;
    uint32_t block[4];
    astc_header_t header;
#ifdef HAS_128BIT
    __uint128_t u128_block;
#else
    uint64_t u64_block[2];
#endif

    if(data == NULL)
    {
        LOGE("data     = %p\n", data);
        LOGE("datasize = %d\n", datasize);
        LOGE("No data to work with, returning false\n");
        return false;
    }

    // the size passed in does not include the header.
    // we'll need the size that includes the header for our processing.
    datasize += sizeof(astc_header_t);

    memcpy(&header, data, sizeof(astc_header_t));

    if(memcmp((void *)&header.magic, (void *)&MAGIC_NUMBER, 4) != 0)
    {
        LOGE("magic number doesn't match\n");
        LOGE("MAGIC_NUMBER = 0X%X\n", MAGIC_NUMBER);
        LOGE("header.magic = 0X%02X%02X%02X%02X\n", 
                                                 header.magic[0],
                                                 header.magic[1],
                                                 header.magic[2],
                                                 header.magic[3]);
        return false;
    }

    int x = header.blockdim_x;
    int y = header.blockdim_y;
    int z = header.blockdim_z;

#if EXTRA_VERBOSE_LOGGING
    LOGD("x = %d\n", x);
    LOGD("y = %d\n", y);
    LOGD("z = %d\n", z);
#endif

    int xsize = header.xsize[0] |
                header.xsize[1] << 8 |
                header.xsize[2] << 16;
    int ysize = header.ysize[0] |
                header.ysize[1] << 8 |
                header.ysize[2] << 16;

#if EXTRA_VERBOSE_LOGGING
    LOGD("xsize = %d\n", xsize);
    LOGD("ysize = %d\n", ysize);
#endif

    int num_blocks = 0;
    int block_size = 16;
    int block_offset = 0;

    for(block_offset = block_size; block_offset < datasize; block_offset += block_size) {
        memcpy(&block, (uint8_t*)data+block_offset, block_size);
#if EXTRA_VERBOSE_LOGGING
        LOGD("\nblock number: %d\n", num_blocks);
#endif

        // The block mode is the first 10 bits of information in the block.
        // For overall block layout, see Figure C.1 - Block Layout Overview.
        // "Block Mode" itself is covered in section C.2.10
        int block_mode = block[0] & 0x07FF;
#if EXTRA_VERBOSE_LOGGING
        LOGD("block_mode = %0X\n", block_mode);
        LOGD("block[3]: 0x%08X\n", block[3]);
        LOGD("block[2]: 0x%08X\n", block[2]);
        LOGD("block[1]: 0x%08X\n", block[1]);
        LOGD("block[0]: 0x%08X\n", block[0]);
#endif

#ifdef HAS_128BIT
        // Some compilers appear to support an unsigned 128 bit type.
        // This is nice for us since an ASTC block is defined to be 128 bits.
        // Since we're only interested in the color endpoint modes, we mostly only
        // need the first 32bits, except when there are more than 1 partition to the
        // data.  When there is more than 1 partition, part of the endpoint information
        // is stored in the middle of the block offset from the end of the block.
        // So, if we have the block as a contiguous 128bits, it is a lot easier to
        // extract that data than if the block was split up into 32 or 64 bit chunks.
        u128_block = ((__uint128_t)block[3] << 96) | ((__uint128_t)block[2] << 64) | ((__uint128_t)block[1] << 32) | block[0];
#else
        u64_block[0] = ((uint64_t)block[3] << 32) | block[2];
        u64_block[1] = ((uint64_t)block[1] << 32) | block[0];
#endif

        // Determine if void-extent block
        // Void-Extent blocks are covered in Section C.2.23.
        // They are used when you have a block consists of a single color.
        // Thankfully, that also makes them pretty easy to decode.
        // Void-Extent is determined by a magic number in the block mode (0x01FC)
        // If it is a void-extent block, then the alpha component is stored
        // in the last 16 bits of the block.  The alpha component is encoded as
        // a UNORM16, which means even if the alpha was originally only 8 bits,
        // it was normalized to be a 16 bit value with 0xFFFF as the max value.
        // So we just need to check if the alpha is less than that value.
        if((block_mode & 0x01FC) == 0x01FC)
        {
#if EXTRA_VERBOSE_LOGGING
            LOGD("void-extent block!!\n");
#endif

            uint16_t alpha = (block[3] & 0xFFFF0000) >> 16;
#if EXTRA_VERBOSE_LOGGING
            LOGD("alpha component: 0x%02X\n", alpha);
#endif
            if(alpha < 0xFFFF)
            {
#if EXTRA_VERBOSE_LOGGING
                LOGD("hasAlpha = true\n");
                LOGD("at block number = %d\n", num_blocks);
#endif
                return true;
            }
        }


        // Ok, next we need to get all the information we can out of the block mode.
        // We need this to find out how many weights are in the block.
        // We also need to know how the weight is encoded, and whether the block is dual plane.
        // All of that so we can determine the total number of weights.
        // And we need that so we can determine how many bits are used for weights
        decoded_block_t decoded_block;
        decode_block(block_mode, &decoded_block);

        if(decoded_block.width > x ||
           decoded_block.height > y)
        {
           decoded_block.is_valid_block = 0;
        }

        if(!decoded_block.is_valid_block)
        {
#if EXTRA_VERBOSE_LOGGING
            LOGD("decode: error_block = 1\n");
#endif
            continue;
        }

        //  Once we know how many bits are used for weights,
        //  then we can count backwards from 128bits (since a block is 128 bits)
        //  to determine where the weights start inside of the block. aka the 'below weights position'.
        //  the 'below weights position' is used when determining the color endpoint modes when
        //  the number of partitions is > 1.
        int below_weights = 128 - decoded_block.weight_bits;
#if EXTRA_VERBOSE_LOGGING
        LOGD("below_weights = %0X\n", below_weights);
#endif

        // Partitions is how ASTC handles multiple, very-different colors inside a single block.
        // This is described in Section C.2.4 - Block Encoding.
        // It's actually pretty cool and pretty clever.
        // Anyway, we need to know how many partitions there are and that is stored in bits
        // 11 and 12.  See Figure C.1 - Block Layout Overview
        int num_partitions = ((block[0] >> 11) & 0x0003) + 1;
#if EXTRA_VERBOSE_LOGGING
        LOGD("partitions = %d\n", num_partitions);
#endif

        // The "Color Endpoint Mode" tells us what format the endpoints of the block are in.
        // For example:  RGB, RGBA, etc.
        // That's the information we are ultimately looking for.  If we know the endpoints
        // in that partition are RGB then none of those texels in that partitions contain 
        // transparency.
        // If there is only 1 partition, this info is pretty easy to get and that's covered
        // in Figure C.2 - Single-partition Block Layout.
        int color_endpoint_mode = 0;
        if(num_partitions == 1)
        {
            // read color endpoint mode
            color_endpoint_mode = (block[0] >> 13) & 0x0F;
#if EXTRA_VERBOSE_LOGGING
            LOGD("decode: color_formats[0] = %d\n", color_endpoint_mode);
#endif
            if(hasAlpha(color_endpoint_mode))
            {
#if EXTRA_VERBOSE_LOGGING
                LOGD("hasAlpha = true\n");
                LOGD("at block number = %d\n", num_blocks);
#endif
                return true;
            }
        }
        else // there are multiple partitions...
        {
            // There are 6 bits of the color endpoint mode information.
            // The first two bits are a selector for determining how the
            // endpoint info is distributed in the block.  Will get to 
            // that more shortly.
            // So, first, let's grab those 6 bits and then the 
            // selector from that...
            color_endpoint_mode = (block[0] >> 23) & 0x3F;
            int color_endpoint_mode_selector = color_endpoint_mode & 0x3;
            int cem[4];

            // In Figure C.4 - Multi-Partition Color Endpoint Modes,
            // It has the color endpoint information split up as
            // CEM, C0..C3, and M0..M3 depending on how many partitions.
            // We need to know how many bits of M we are dealing with to
            // reconstruct the actual color endpoing mode:
            int num_bits_of_M = 0;
            if(num_partitions == 2)
            {
                num_bits_of_M = 2;
            } 
            else if(num_partitions == 3)
            {
                num_bits_of_M = 5;
            }
            else
            {
                num_bits_of_M = 8;
            }

            // Once we know how many bits of M we're going to be working with, we can subtract
            // that off the position of where the end of the weights are located.  This will
            // allow us to get M0..M3 (color_endpoint_mode_upper_bits):
            int color_endpoint_mode_upper_bits_pos = below_weights - num_bits_of_M;
#if EXTRA_VERBOSE_LOGGING
            LOGD("color_endpoint_mode_upper_bits_pos = 0x%0X\n", color_endpoint_mode_upper_bits_pos);
#endif

            int M_mask = (1 << num_bits_of_M) - 1;
            int color_endpoint_mode_upper_bits = 0;

#ifdef HAS_128BIT
            color_endpoint_mode_upper_bits = (u128_block >> color_endpoint_mode_upper_bits_pos) & M_mask;
#if EXTRA_VERBOSE_LOGGING
            LOGD("color_endpoint_mode_upper_bits = 0x%0X\n", (uint32_t)((u128_block >> color_endpoint_mode_upper_bits_pos) & 0xFFFF));
#endif
#else
            color_endpoint_mode_upper_bits = extract_color_endpoint_mode_upper_bits(u64_block, color_endpoint_mode_upper_bits_pos, num_bits_of_M);
#endif
 


#if EXTRA_VERBOSE_LOGGING
            LOGD("M_mask = 0x%0X\n", M_mask);
            LOGD("color_endpoint_mode_upper_bits = 0x%0X\n", color_endpoint_mode_upper_bits);
            LOGD("b4 color_endpoint_mode = 0x%0X\n", color_endpoint_mode);
#endif
            // reconstruct the color endpoint mode.  note we still have the two selector bits
            // as part of this.  that'll be dealt with shortly.
            color_endpoint_mode |= (color_endpoint_mode_upper_bits << 6);


            // See Table C.2.11 - Multi-Partition Color Endpoint Modes
            // If the selector is 0, then all the endpoint pairs are of 
            // the same type and the endpoint mode is just 4 bits and
            // we can just shift 2 to get rid of the selector bits:
            if(color_endpoint_mode_selector == 0)
            {
                color_endpoint_mode = (color_endpoint_mode >> 2) & 0xF;
                for(int i=0; i<num_partitions; i++)
                {
                    cem[i] = color_endpoint_mode;
                }
            }
            else // the selector is > 0
            {
#if EXTRA_VERBOSE_LOGGING
                LOGD("color_endpoint_mode = 0x%0X\n", color_endpoint_mode);
                LOGD("color_endpoint_mode_selector = 0x%0X\n", color_endpoint_mode_selector);
#endif

                // See Figure C.4 - Multi-Partition Color Endpoint Modes
                // We need to reconstruct the color endpoint mode for each paritition
                // according to the figure:
                int index = 2;
                int class_selector_bit = color_endpoint_mode_selector - 1;
                for(int i=0; i<num_partitions; i++)
                {
                    cem[i] = (((color_endpoint_mode >> index) & 0x1) + class_selector_bit) << 2;
                    index++;
                }
                for(int i=0; i<num_partitions; i++)
                {
                    cem[i] |= (color_endpoint_mode >> index) & 0x3;
                    index+=2;
                }
            }

            // All the color endpoint modes for each of the partitions have been
            // reconstructed at this point.  Let's check them for an Alpha component:
            for(int i=0; i<num_partitions; i++)
            {
#if EXTRA_VERBOSE_LOGGING
                LOGD("decode: color_formats[%d] = %d\n", i, cem[i]);
#endif
                if(hasAlpha(cem[i]))
                {
#if EXTRA_VERBOSE_LOGGING
                    LOGD("hasAlpha = true\n");
                    LOGD("at block number = %d\n", num_blocks);
#endif
                    return true;
                }
            }
        }
        num_blocks++;
    }

#if EXTRA_VERBOSE_LOGGING
    LOGD("num_blocks = %d\n", num_blocks);
#endif

    return false;
}


bool astc_has_transparency(void *data, int datasize) {
    return detectAlpha(data, datasize);
}


} // namespace

