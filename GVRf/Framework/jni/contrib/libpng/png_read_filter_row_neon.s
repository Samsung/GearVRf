#; Copyright (c) 2010-2013, The Linux Foundation. All rights reserved.
#;
#; Redistribution and use in source and binary forms, with or without
#; modification, are permitted provided that the following conditions are
#; met:
#;     * Redistributions of source code must retain the above copyright
#;       notice, this list of conditions and the following disclaimer.
#;     * Redistributions in binary form must reproduce the above
#;       copyright notice, this list of conditions and the following
#;       disclaimer in the documentation and/or other materials provided
#;       with the distribution.
#;     * Neither the name of The Linux Foundation. nor the names of its
#;       contributors may be used to endorse or promote products derived
#;       from this software without specific prior written permission.
#;
#; THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
#; WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
#; MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
#; ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
#; BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
#; CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
#; SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
#; BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
#; WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
#; OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
#; IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#;==============================================================================

        .code 32                                          @; Code is ARM ISA
#;==============================================================================

        .global     png_read_filter_row_neon

#;==============================================================================
#;       INPUTS:    r0       rowbytes:     number of bytes in current row
#;                  r1       pixel_depth:  number of bits per pixel
#;                  r2       row:          pointer to start of current row
#;                  r3       prev_row:     pointer to start of previous row
#;                  [sp,#0]  filter:       filter type
#;
#;       NOTE:      Don't touch r5-r11
#;==============================================================================
.balign 32
.type png_read_filter_row_neon, %function
png_read_filter_row_neon:

        ldr        r12,[sp,#0]

        cmp        r12,#0
        beq        DONE

        cmp        r12,#1
        beq        sub_filter

        cmp        r12,#2
        beq        up_filter

        cmp        r12,#3
        beq        avg_filter

        cmp        r12,#4
        beq        paeth_filter

        b          DONE

        #;; ---------------
        #;; SUB filter type
        #;; ---------------


sub_filter:

       stmdb  sp!, {r4}

        add        r1,r1,#7                @; bpp = bytes per pixel
        lsr        r1,r1,#3                @;     = (pixel_depth + 7) >> 3
        mov        r12,r1

        #;; r0 = rowbytes
        #;; r1 = loop counter = bpp (initially)
        #;; r2 = row pointer
        #;; r12 = bpp = loop/pointer increment value

        cmp        r1,r0
        beq        sub_filter_exit         @; exit if bpp == rowbytes

        cmp        r12,#1
        beq        sub_filter_1bpp

        cmp        r12,#2
        beq        sub_filter_2bpp

        cmp        r12,#3
        beq        sub_filter_3bpp

        cmp        r12,#4
        beq        sub_filter_4bpp

        cmp        r12,#6
        beq        sub_filter_6bpp

        cmp        r12,#8
        beq        sub_filter_8bpp

sub_filter_exit:
        b          sub_filter_DONE             @; return


sub_filter_1bpp:

        #;; ----------------------------
        #;; SUB filter, 1 byte per pixel
        #;; ----------------------------

      lsrs       r4,r0,#4                      @; r1 = floor(rowbytes/4)
                                               @;    = iteration count for loop16
      beq        sub_filter_1bpp_16bytes_done

      vmov.i8    d21, #0
      vld1.8     {d16,d17}, [r2]               @; load 16 pixels
                                               @; d16 = a b c d e f g h
                                               @; d17 = i j k l m n o p

      mov       r1, #0
sub_filter_1bpp_16bytes:




       vshl.i64   d18, d16, #8                 @; d18 = 0 a b c d e f g
       vadd.i8   d18, d16, d18                 @; d18 = a a+b b+c c+d d+e e+f f+g g+h

       vshl.i64   d18, d18, #8                 @; d18 = 0 a a+b b+c c+d d+e e+f f+g
       vadd.i8   d18, d16, d18                 @; d18 = a a+b a+b+c b+c+d c+d+e d+e+f e+f+g f+g+h

       vshl.i64   d18, d18, #8                 @; shift add continuously to propage the sum of previous
       vadd.i8   d18, d16, d18                 @; and current pixels

       vshl.i64   d18, d18, #8
       vadd.i8   d18, d16, d18

       vshl.i64   d18, d18, #8
       vadd.i8   d18, d16, d18

       vshl.i64   d18, d18, #8
       vadd.i8   d18, d16, d18

       vshl.i64   d18, d18, #8
       vadd.i8   d18, d16, d18                 @; maximum data size for shift is 64 bits i.e. doubleword.
                                               @; after computing thh value of all the pixels in the double word
                                               @; extract the last computed value which will be used by
                                               @; the next set of pixels (i.e next doubleword)
       vext.8     d22, d18, d21, #7            @; extract the updated value of d18[7] i.e a+b+c+d+e+f+h
       vadd.i8    d17, d17, d22                @; d17 = a+b+c+d+e+f+g+h+i j k l m n o p

       vshl.i64   d19, d17, #8                 @; continue shift-add as the first half
       vadd.i8    d19, d17, d19

       vshl.i64   d19, d19, #8
       vadd.i8    d19, d17, d19

       vshl.i64   d19, d19, #8
       vadd.i8    d19, d17, d19

       vshl.i64   d19, d19, #8
       vadd.i8    d19, d17, d19

       vshl.i64   d19, d19, #8
       vadd.i8    d19, d17, d19

       vshl.i64   d19, d19, #8
       vadd.i8    d19, d17, d19

       vshl.i64   d19, d19, #8
       vadd.i8    d19, d17, d19

       vst1.8     {d18,d19},[r2]!               @; store the result back

       add        r1, r1, #16                   @; add 16 to the loop counter(no of bytes completed)
       subs       r4,r4,#1                      @; decrement iteration count
       beq        sub_filter_1bpp_16bytes_adjust


       vext.8     d22, d19, d21, #7             @; more iterations to go
                                                @; extract the last computed value
       vld1.8     {d16,d17}, [r2]               @; load the next 16 bytes
       vadd.i8    d16, d16, d22                 @; set up the input by adding the previous pixel
                                                @; value to the input
       b sub_filter_1bpp_16bytes

sub_filter_1bpp_16bytes_adjust:

       cmp        r1, r0                        @; no more pixels left .. exit
       sub        r2, r2, #1                    @; more pixels remaining
                                                @; r2 points to the current pixel adjust it
                                                @; so that it points to the prev pixel for the below loop
       beq        sub_filter_DONE

sub_filter_1bpp_16bytes_done:


       vld1.8     {d0[0]},[r2]!                 @; load 1 byte (1 pixel) into D0[0]
                                                @; increment row pointer
sub_filter_1bpp_loop:
       add        r1,r1,r12                     @; loop counter += bpp
       cmp        r1,r0                         @;

       vld1.8     {d2[0]},[r2]                  @; load 1 byte (current pixel) into D2[0]

       vadd.i8    d0,d0,d2                      @; vector add 1 byte of previous pixel with
                                                @;            1 byte of current pixel
       vst1.8     {d0[0]},[r2]!                 @; store 1 byte (updated pixel) back
                                                @;  into row pointer location and increment
                                                @;  row pointer

       bne        sub_filter_1bpp_loop          @; loop back until loop counter == rowbytes

       b          sub_filter_DONE               @; return

       #;; -----------------------------
       #;; SUB filter, 2 bytes per pixel
       #;; -----------------------------
sub_filter_2bpp:

       lsrs       r4,r0,#4                      @; r1 = floor(rowbytes/4)
                                                @;    = iteration count for loop16
       beq        sub_filter_2bpp_16bytes_done

       vmov.i8    d21, #0
       vld1.8     {d16,d17}, [r2]               @; load 16 bytes to q8
                                                @; d16 = a b c d e f g h
                                                @; d17 = i j k l m n o p
       mov       r1, #0
sub_filter_2bpp_16bytes:

       vshl.i64   d18, d16, #16                 @;  each pixel is 2bytes .. shift by 16 bits to get previous pixel
       vadd.i8   d18, d16, d18                  @;  add to the current pixel

       vshl.i64   d18, d18, #16                 @; shift-add to propagate the computed sum as the case for 1bpp
       vadd.i8   d18, d16, d18

       vshl.i64   d18, d18, #16
       vadd.i8   d18, d16, d18


       vext.8     d22, d18, d21, #6             @; extract the last computed value (i.e. last 2 bytes)
       vadd.i8    d17, d17, d22                 @; add the last computed pixel to the input

       vshl.i64   d19, d17, #16
       vadd.i8    d19, d17, d19

       vshl.i64   d19, d19, #16
       vadd.i8    d19, d17, d19

       vshl.i64   d19, d19, #16
       vadd.i8    d19, d17, d19


       vst1.8     {d18,d19},[r2]!               @; store the result back


       add        r1, r1, #16                   @; add 16 to the loop counter(no of bytes completed)
       subs       r4,r4,#1                      @; decrement iteration count
       beq        sub_filter_2bpp_16bytes_adjust


       vext.8     d22, d19, d21, #6             @; extract the last computed value
                                                @; add the last computed pixel to the input
       vld1.8     {d16,d17}, [r2]
       vadd.i8    d16, d16, d22

       b sub_filter_2bpp_16bytes


sub_filter_2bpp_16bytes_adjust:

       cmp        r1, r0                        @; no more pixels left .. exit
       sub        r2, r2, #2                    @; more pixels remaining
                                                @; r2 points to the current pixel adjust it
                                                @; so that it points to the prev pixel for the below loop
       beq        sub_filter_DONE

sub_filter_2bpp_16bytes_done:

       vld1.16    {d0[0]},[r2]!                 @; load 2 bytes (1 pixel) into D0[0]
                                                @; increment row pointer
sub_filter_2bpp_loop:
       add        r1,r1,r12                     @; loop counter += bpp
       cmp        r1,r0                         @;

       vld1.16    {d2[0]},[r2]                  @; load 2 bytes (current pixel) into D2[0]
       vadd.i8    d0,d0,d2                      @; vector add 2 bytes of previous pixel with
                                                @;            2 bytes of current pixel
       vst1.16    {d0[0]},[r2]!                 @; store 2 bytes (updated pixel) back
                                                @;  into row pointer location and increment
                                                @;  row pointer

       bne        sub_filter_2bpp_loop          @; loop back until loop counter == rowbytes
                                                @
       b          sub_filter_DONE               @ ; return

       #;; -----------------------------
       #;; SUB filter, 3 bytes per pixel
       #;; -----------------------------
sub_filter_3bpp:
       vld1.32    {d0[0]},[r2], r12             @; load 4 bytes (1 pixel + 1 extra byte) into D0[0]
                                                @; increment row pointer by bpp
sub_filter_3bpp_loop:
       add        r1,r1,r12                     @; loop counter += bpp
       cmp        r1,r0                         @;

       vld1.32    {d2[0]},[r2]                  @; load 4 bytes (current pixel + 1 extra byte) into D2[0]
       vadd.i8    d0,d0,d2                      @; vector add 3 bytes of previous pixel with
                                                @;            3 bytes of current pixel
       vst1.16    {d0[0]},[r2]!                 @; store 2 bytes (updated pixel) back
                                                @;  into row pointer location and increment
                                                @;  row pointer
       vst1.8     {d0[2]},[r2]!                 @; store 1 byte (updated pixel) back
                                                @;  into row pointer location and increment
                                                @;  row pointer

       bne        sub_filter_3bpp_loop          @; loop back until loop counter == rowbytes

       b          sub_filter_DONE               @; return

       #;; -----------------------------
       #;; SUB filter, 4 bytes per pixel
       #;; -----------------------------
sub_filter_4bpp:
       vld1.32    {d0[0]},[r2]!                 @; load 4 bytes (1 pixel) into D0[0]
                                                @; increment row pointer
sub_filter_4bpp_loop:                           @
       add        r1,r1,r12                     @; loop counter += bpp
       cmp        r1,r0                         @;


       vld1.32    {d2[0]},[r2]                  @; load 4 bytes (current pixel) into D2[0]
       vadd.i8    d0,d0,d2                      @; vector add 4 bytes of previous pixel with
                                                @;            4 bytes of current pixel
       vst1.32    {d0[0]},[r2]!                 @; store 4 bytes (updated pixel) back
                                                @;  into row pointer location and increment
                                                @;  row pointer

       bne        sub_filter_4bpp_loop          @; loop back until loop counter == rowbytes

       b          sub_filter_DONE               @; return

       #;; -----------------------------
       #;; SUB filter, 6 bytes per pixel
       #;; -----------------------------
sub_filter_6bpp:
       vld1.8     {d0},[r2],r12                @; load 8 bytes (1 pixel + 2 extra bytes) into D0
                                               @; increment row pointer by bpp
sub_filter_6bpp_loop:                          @
       add        r1,r1,r12                   @; loop counter += bpp
       cmp        r1,r0                        @;

       vld1.8     {d2},[r2]                    @; load 8 bytes (1 pixel + 2 extra bytes) into D2
       vadd.i8    d0,d0,d2                     @; vector add 6 bytes of previous pixel with
                                               @;            6 bytes of current pixel
       vst1.32    {d0[0]},[r2]!                @; store 4 bytes (updated pixel) back
                                               @;  into row pointer location and increment
                                               @;  row pointer
       vst1.16    {d0[2]},[r2]!                @; store 2 bytes (updated pixel) back
                                               @;  into row pointer location and increment
                                               @;  row pointer

       bne        sub_filter_6bpp_loop         @; loop back until loop counter == rowbytes

       b          sub_filter_DONE              @; return

       #;; -----------------------------
       #;; SUB filter, 8 bytes per pixel
       #;; -----------------------------
sub_filter_8bpp:
       vld1.8     {d0},[r2]!                   @; load 8 bytes (1 pixel) into D0
                                               @; increment row pointer
sub_filter_8bpp_loop:                          @
       add        r1,r1,r12                    @; loop counter += bpp
       cmp        r1,r0                        @;
       vld1.8     {d2},[r2]                    @; load 8 bytes (current pixel) into D2
       vadd.i8    d0,d0,d2                     @; vector add 8 bytes of previous pixel with
                                               @;            8 bytes of current pixel
       vst1.8     {d0},[r2]!                   @; store 8 bytes (updated pixel) back
                                               @;  into row pointer location and increment
                                               @;  row pointer


       bne        sub_filter_8bpp_loop         @; loop back until loop counter == rowbytes
                                               @
       b          sub_filter_DONE              @ ; return

sub_filter_DONE:

       ldmia       sp!, {r4}
       bx         r14

       #;; --------------
       #;; UP filter type
       #;; --------------
up_filter:

       #;; r0 = rowbytes
       #;; r1 = pixel_depth (not required for UP filter type)
       #;; r2 = row pointer
       #;; r3 = previous row pointer


       lsrs       r1,r0,#5                     @; r1 = floor(rowbytes/32)
                                               @;    = iteration count for loop32
       beq        up_filter_32bytes_proc_done


up_filter_32bytes_proc:


       mov        r12, r2

       vld1.8     {q0},[r3]!                   @; load 32 bytes from previous
       vld1.8     {q2},[r3]!                   @;  row and increment pointer
                                               @
                                               @
       vld1.8     {q1},[r12]!                  @; load 32 bytes from current row
       vld1.8     {q3},[r12]!                  @
                                               @
                                               @
                                               @
       vadd.i8    q0,q0,q1                     @; vector add of 16 bytes
       vadd.i8    q2,q2,q3                     @
                                               @
                                               @
                                               @
       vst1.8     {q0},[r2]!                   @; store 32 bytes to current row
       vst1.8     {q2},[r2]!                   @
                                               @;  and increment pointer
       sub        r0,r0,#32                    @; subtract 32 from rowbytes
       subs       r1,r1,#1                     @; decrement iteration count
       bne        up_filter_32bytes_proc



up_filter_32bytes_proc_done:

       lsrs       r1,r0,#4                     @; r1 = floor(rowbytes/16)
                                               @;    = iteration count for loop16
       beq        up_filter_16bytes_proc_done

up_filter_16bytes_proc:

       vld1.8     {q0},[r3]!                   @; load 16 bytes from previous
                                               @;  row and increment pointer
       vld1.8     {q1},[r2]                    @; load 16 bytes from current row
       vadd.i8    q0,q0,q1                     @; vector add of 16 bytes
       vst1.8     {q0},[r2]!                   @; store 16 bytes to current row
                                               @;  and increment pointer
       sub        r0,r0,#16                    @; subtract 16 from rowbytes
       subs       r1,r1,#1                     @; decrement iteration count
       bne        up_filter_16bytes_proc

up_filter_16bytes_proc_done:

       lsrs       r1,r0,#3                     @; r1 = floor(rowbytes/8)
       beq        up_filter_8bytes_proc_done

up_filter_8bytes_proc:

       vld1.8     {d0},[r3]!                   @; load 8 bytes from previous
                                               @;  row and increment pointer
       vld1.8     {d2},[r2]                    @; load 8 bytes from current row
       vadd.i8    d0,d0,d2                     @; vector add 8 bytes
       vst1.8     {d0},[r2]!                   @; store 8 bytes to current row
                                               @;  and increment pointer
       sub        r0,r0,#8                     @; subtract 8 from rowbytes

up_filter_8bytes_proc_done:

       lsrs       r1,r0,#2                     @; r1 = floor(rowbytes/4)
       beq        up_filter_4bytes_proc_done

up_filter_4bytes_proc:

       vld1.32    {d0[0]},[r3]!                @; load 4 bytes from previous row
                                               @;  and increment pointer
       vld1.32    {d2[0]},[r2]                 @; load 4 bytes from current row
       vadd.i8    d0,d0,d2                     @; vector add 4 bytes
       vst1.32    {d0[0]},[r2]!                @; store 4 bytes to current row
                                               @;  and increment pointer
       sub        r0,r0,#4                     @; subtract 4 from rowbytes

up_filter_4bytes_proc_done:

       lsrs       r1,r0,#1                     @; r1 = floor(rowbytes/2)
       beq        up_filter_2bytes_proc_done

up_filter_2bytes_proc:

       vld1.16    {d0[0]},[r3]!                @; load 2 bytes from previous row
                                               @;  and increment pointer
       vld1.16    {d2[0]},[r2]                 @; load 2 bytes from current row
       vadd.i8    d0,d0,d2                     @; vector add 2 bytes
       vst1.16    {d0[0]},[r2]!                @; store 2 bytes to current row
                                               @;  and increment pointer
       sub        r0,r0,#2                     @; subtract 2 from rowbytes

up_filter_2bytes_proc_done:

       cmp        r0,#0
       beq        up_filter_1byte_proc_done

up_filter_1byte_proc:

       vld1.8     {d0[0]},[r3]!                @; load 1 byte from previous row
                                               @;  and increment pointer
       vld1.8     {d2[0]},[r2]                 @; load 1 byte from current row
       vadd.i8    d0,d0,d2                     @; vector add 1 byte
       vst1.8     {d0[0]},[r2]!                @; store 1 byte to current row
                                               @;  and increment pointer
up_filter_1byte_proc_done:

       b          DONE

       #;; ---------------
       #;; AVG filter type
       #;; ---------------
avg_filter:

      add        r1,r1,#7                      @; bpp = byptes per pixel
      lsr        r1,r1,#3                      @;     = (pixel_depth + 7) >> 3
      mov        r12,r1

      #;; r0 = rowbytes
      #;; r1 = loop counter = bpp (initially)
      #;; r2 = row pointer
      #;; r3 = previous row pointer
      #;; r12 = bpp = loop/pointer increment value

      cmp        r12,#1
      beq        avg_filter_1bpp

      cmp        r12,#2
      beq        avg_filter_2bpp

      cmp        r12,#3
      beq        avg_filter_3bpp

      cmp        r12,#4
      beq        avg_filter_4bpp

      cmp        r12,#6
      beq        avg_filter_6bpp

      cmp        r12,#8
      beq        avg_filter_8bpp

avg_filter_exit:
      b          DONE                           @; return

      #;; ----------------------------
      #;; AVG filter, 1 byte per pixel
      #;; ----------------------------
avg_filter_1bpp:

      cmp        r1,r0

      vld1.8     {d0[0]},[r2]                   @; load 1 byte (pixel x) from curr
                                                @;  row into d0[0]
      vld1.8     {d1[0]},[r3]!                  @; load 1 byte (pixel b) from prev
                                                @;  row into d1[0]
                                                @; increment prev row pointer
      vsra.u8    d0,d1,#1                       @; shift right pixel b by 1 and add
                                                @;  to pixel x
      vst1.8     {d0[0]},[r2]!                  @; store 1 byte (updated pixel x)
                                                @; increment curr row pointer
                                                @; updated pixel x is now pixel a
      beq        DONE

avg_filter_1bpp_loop:
      add        r1,r1,r12                      @; loop counter += bpp
      cmp        r1,r0


      vld1.8     {d2[0]},[r2]                   @; load 1 byte (pixel x) from curr
                                                @;  row into d2[0]
      vld1.8     {d1[0]},[r3]!                  @; load 1 byte (pixel b) from prev
                                                @;  row into d1[0]
      vaddl.u8   q2,d0,d1                       @; q2 = (pixel a + pixel b)
      vshrn.i16  d1,q2,#1                       @; d1[0] = (a + b)/2
      vadd.i8    d0,d2,d1                       @; d0[0] = x + ((a + b)/2)
      vst1.8     {d0[0]},[r2]!                  @; store 1 byte (updated pixel x)
                                                @; increment curr row pointer
      bne        avg_filter_1bpp_loop

      b          DONE                           @; exit loop when
                                                @;  loop counter  == rowbytes
      #;; -----------------------------
      #;; AVG filter, 2 bytes per pixel
      #;; -----------------------------
avg_filter_2bpp:

      cmp        r1,r0

      vld1.16    {d0[0]},[r2]                   @; load 2 bytes (pixel x) from curr
                                                @;  row into d0[0]
      vld1.16    {d1[0]},[r3]!                  @; load 2 bytes (pixel b) from prev
                                                @;  row into d1[0]
                                                @; increment prev row pointer
      vsra.u8    d0,d1,#1                       @; shift right pixel b by 1 and add
                                                @;  to pixel x
      vst1.16    {d0[0]},[r2]!                  @; store 2 bytes (updated pixel x)
                                                @; increment curr row pointer
                                                @; updated pixel x is now pixel a
       beq        DONE

avg_filter_2bpp_loop:
      add        r1,r1,r12                      @; loop counter += bpp
      cmp        r1,r0


      vld1.16    {d2[0]},[r2]                   @; load 2 bytes (pixel x) from curr
                                                @;  row into d2[0]
      vld1.16    {d1[0]},[r3]!                  @; load 2 bytes (pixel b) from prev
                                                @;  row into d1[0]
      vaddl.u8   q2,d0,d1                       @; q2 = (pixel a + pixel b)
      vshrn.i16  d1,q2,#1                       @; d1[0] = (a + b)/2
      vadd.i8    d0,d2,d1                       @; d0[0] = x + ((a + b)/2)
      vst1.16    {d0[0]},[r2]!                  @; store 2 bytes (updated pixel x)
                                                @; increment curr row pointer

      bne        avg_filter_2bpp_loop

      b          DONE                           @; exit loop when
                                                @;  loop counter  == rowbytes

      #;; -----------------------------
      #;; AVG filter, 3 bytes per pixel
      #;; -----------------------------
avg_filter_3bpp:

      cmp        r1,r0

      vld1.32    {d0[0]},[r2]                   @; load 4 bytes (pixel x + 1 extra
                                                @;  byte) from curr row into d0[0]
      vld1.32    {d1[0]},[r3],r12               @; load 4 bytes (pixel b + 1 extra
                                                @;  byte) from prev row into d1[0]
                                                @; increment prev row pointer
      vsra.u8    d0,d1,#1                       @; shift right pixel b by 1 and add
                                                @;  to pixel x
      vst1.16    {d0[0]},[r2]!                  @; store 2 bytes (updated pixel x)
                                                @; increment curr row pointer
      vst1.8     {d0[2]},[r2]!                  @; store 1 byte (updated pixel x)
                                                @; increment curr row pointer
                                                @; updated pixel x is now pixel a
      beq       DONE

avg_filter_3bpp_loop:
      add        r1,r1,r12                      @; loop counter += bpp
      cmp        r1,r0

      vld1.32    {d2[0]},[r2]                   @; load 4 bytes (pixel x + 1 extra
                                                @;  byte) from curr row into d2[0]
      vld1.32    {d1[0]},[r3],r12               @; load 4 bytes (pixel b + 1 extra
                                                @;  byte) from prev row into d1[0]
      vaddl.u8   q2,d0,d1                       @; q2 = (pixel a + pixel b)
      vshrn.i16  d1,q2,#1                       @; d1[0] = (a + b)/2
      vadd.i8    d0,d2,d1                       @; d0[0] = x + ((a + b)/2)
      vst1.16    {d0[0]},[r2]!                  @; store 2 bytes (updated pixel x)
                                                @; increment curr row pointer
      vst1.8     {d0[2]},[r2]!                  @; store 1 byte (updated pixel x)
                                                @; increment curr row pointer

      bne        avg_filter_3bpp_loop

      b          DONE                           @; exit loop when
                                                @;  loop counter  == rowbytes
      #;; -----------------------------
      #;; AVG filter, 4 bytes per pixel
      #;; -----------------------------
avg_filter_4bpp:

      cmp        r1,r0

      vld1.32    {d0[0]},[r2]                   @; load 4 bytes (pixel x) from curr
                                                @;  row into d0[0]
      vld1.32    {d1[0]},[r3]!                  @; load 4 bytes (pixel b) from prev
                                                @;  row into d1[0]
                                                @; increment prev row pointer
      vsra.u8    d0,d1,#1                       @; shift right pixel b by 1 and add
                                                @;  to pixel x
      vst1.32    {d0[0]},[r2]!                  @; store 4 bytes (updated pixel x)
                                                @; increment curr row pointer
                                                @; updated pixel x is now pixel a
      beq        DONE

avg_filter_4bpp_loop:
      add        r1,r1,r12                      @; loop counter += bpp
      cmp        r1,r0


      vld1.32    {d2[0]},[r2]                   @; load 4 bytes (pixel x) from curr
                                                @;  row into d2[0]
      vld1.32    {d1[0]},[r3]!                  @; load 4 bytes (pixel b) from prev
                                                @;  row into d1[0]
      vaddl.u8   q2,d0,d1                       @; q2 = (pixel a + pixel b)
      vshrn.i16  d1,q2,#1                       @; d1[0] = (a + b)/2
      vadd.i8    d0,d2,d1                       @; d0[0] = x + ((a + b)/2)
      vst1.32    {d0[0]},[r2]!                  @; store 4 bytes (updated pixel x)
                                                @; increment curr row pointer
      bne        avg_filter_4bpp_loop

      b          DONE                           @; exit loop when
                                                @;  loop counter  == rowbytes
      #;; -----------------------------
      #;; AVG filter, 6 bytes per pixel
      #;; -----------------------------
avg_filter_6bpp:

      cmp        r1,r0

      vld1.8     {d0},[r2]                      @; load 8 bytes (pixel x + 2 extra
                                                @;  bytes) from curr row into d0
      vld1.8     {d1},[r3],r12                  @; load 8 bytes (pixel b + 2 extra
                                                @;  bytes) from prev row into d1
                                                @; increment prev row pointer
      vsra.u8    d0,d1,#1                       @; shift right pixel b by 1 and add
                                                @;  to pixel x
      vst1.32    {d0[0]},[r2]!                  @; store 4 bytes (updated pixel x)
                                                @; increment curr row pointer
                                                @; updated pixel x is now pixel a
      vst1.16    {d0[2]},[r2]!                  @; store 2 bytes (updated pixel x)
                                                @; increment curr row pointer
                                                @; updated pixel x is now pixel a
      beq        DONE

avg_filter_6bpp_loop:
      add        r1,r1,r12                      @; loop counter += bpp
      cmp        r1,r0


      vld1.8     {d2},[r2]                      @; load 8 bytes (pixel x + 2 extra
                                                @;  bytes) from curr row into d2
      vld1.8     {d1},[r3],r12                  @; load 8 bytes (pixel b + 2 extra
                                                @;  bytes) from prev row into d1
      vaddl.u8   q2,d0,d1                       @; q2 = (pixel a + pixel b)
      vshrn.i16  d1,q2,#1                       @; d1 = (a + b)/2
      vadd.i8    d0,d2,d1                       @; d0 = x + ((a + b)/2)
      vst1.32    {d0[0]},[r2]!                  @; store 4 bytes (updated pixel x)
                                                @; increment curr row pointer
      vst1.16    {d0[2]},[r2]!                  @; store 2 bytes (updated pixel x)
                                                @; increment curr row pointer
      bne        avg_filter_6bpp_loop

      b          DONE                           @; exit loop when
                                                @;  loop counter  == rowbytes
      #;; -----------------------------
      #;; AVG filter, 8 bytes per pixel
      #;; -----------------------------
avg_filter_8bpp:

      cmp        r1,r0

      vld1.8     {d0},[r2]                      @; load 8 bytes (pixel x) from curr
                                                @;  row into d0
      vld1.8     {d1},[r3]!                     @; load 8 bytes (pixel b) from prev
                                                @;  row into d1
                                                @; increment prev row pointer
      vsra.u8    d0,d1,#1                       @; shift right pixel b by 1 and add
                                                @;  to pixel x
      vst1.8     {d0},[r2]!                     @; store 8 bytes (updated pixel x)
                                                @; increment curr row pointer
                                                @; updated pixel x is now pixel a
      beq        DONE
avg_filter_8bpp_loop:
      add        r1,r1,r12                      @; loop counter += bpp
      cmp        r1,r0


      vld1.8     {d2},[r2]                      @; load 8 bytes (pixel x) from curr
                                                @;  row into d2
      vld1.8     {d1},[r3]!                     @; load 8 bytes (pixel b) from prev
                                                @;  row into d1
      vaddl.u8   q2,d0,d1                       @; q2 = (pixel a + pixel b)
      vshrn.i16  d1,q2,#1                       @; d1 = (a + b)/2
      vadd.i8    d0,d2,d1                       @; d0 = x + ((a + b)/2)
      vst1.8     {d0},[r2]!                     @; store 8 bytes (updated pixel x)
                                                @; increment curr row pointer
      bne        avg_filter_8bpp_loop

      b          DONE                           @; exit loop when
                                                @;  loop counter  == rowbytes
      #;; -----------------
      #;; PAETH filter type
      #;; -----------------
paeth_filter:

      VPUSH     {q4-q7}
      add        r1,r1,#7                       @; bpp = bytes per pixel
      lsr        r1,r1,#3                       @;     = (pixel_depth + 7) >> 3
      mov        r12,r1

      #;; r0 = rowbytes
      #;; r1 = loop counter = bpp (initially)
      #;; r2 = row pointer
      #;; r3 = previous row pointer
      #;; r12 = bpp = loop/pointer increment value


      cmp        r12,#1
      beq        paeth_filter_1bpp

      cmp        r12,#2
      beq        paeth_filter_2bpp

      cmp        r12,#3
      beq        paeth_filter_3bpp

      cmp        r12,#4
      beq        paeth_filter_4bpp

      cmp        r12,#6
      beq        paeth_filter_6bpp

      cmp        r12,#8
      beq        paeth_filter_8bpp

paeth_filter_exit:
      b          paeth_filter_DONE              @; return

      #;; ------------------------------
      #;; PAETH filter, 1 byte per pixel
      #;; ------------------------------
paeth_filter_1bpp:

      cmp        r1, r0

      vld1.8     {d0[0]},[r2]                   @; load 1 byte (pixel x) from curr
                                                @;  row into d0[0]
      vld1.8     {d1[0]},[r3]!                  @; load 1 byte (pixel b) from prev
                                                @;  row into d1[0]
                                                @; increment prev row pointer
      vadd.i8    d2,d0,d1                       @; d2 = x + b = updated pixel x
      vst1.8     {d2[0]},[r2]!                  @; store 1 byte (updated pixel x)
                                                @; increment curr row pointer

      beq         paeth_filter_DONE

paeth_filter_1bpp_loop:
      add        r1,r1,r12                      @; increment curr row pointer
      cmp        r1,r0


      #;; d1[0] = c (b in the previous loop iteration)
      #;; d2[0] = a (x in the previous loop iteration)
      vld1.8     {d3[0]},[r3]!                  @; load 1 byte (pixel b) from prev
                                                @;  row into d3[0]
      vld1.8     {d0[0]},[r2]                   @; load 1 byte (pixel x) from curr
                                                @;  row into d0[0]
      vshll.u8   q4,d1,#1                       @; q4 = c<<1 = 2c
      vabdl.u8   q3,d2,d1                       @; q3 = pb = abs(a - c)
      vabdl.u8   q2,d3,d1                       @; q2 = pa = abs(b - c)
      vaddl.u8   q5,d2,d3                       @; q5 = a + b
      vabd.u16   q4,q5,q4                       @; q4 = pc = abs(a + b - 2c)

      vcle.s16   q5,q2,q3                       @; q5 = (pa <= pb)
      vcle.s16   q6,q2,q4                       @; q6 = (pa <= pc)
      vand       q5,q5,q6                       @; q5 = ((pa <= pb) && (pa <= pc))
      vcle.s16   q7,q3,q4                       @; q7 = (pb <= pc)
      vshrn.u16  d10,q5,#8                      @; d10 = ((pa <= pb) && (pa <= pc))
      vshrn.u16  d14,q7,#8                      @; d14 = (pb <= pc)
                                                @
      vand       d2,d2,d10                      @; d2 = a where 1, 0 where 0
      vbsl       d14,d3,d1                      @; d14 = b where 1, c where 0
      vmvn       d10,d10                        @; invert d10
      vand       d14,d14,d10                    @; d14 = b/c where 1, 0 where 0
      vadd.i8    d2,d2,d14                      @; d2 = p = a/b/c where appropriate
      vadd.i8    d2,d2,d0                       @; d2 = x + p (updated pixel x)
      vmov       d1,d3                          @; d1 = b (c for next iteration)
      vst1.8     {d2[0]},[r2]!                  @; store 1 byte (updated pixel x)


      bne        paeth_filter_1bpp_loop

      b          paeth_filter_DONE              @; exit loop when
                                                @;  loop counter == rowbytes
      #;; -------------------------------
      #;; PAETH filter, 2 bytes per pixel
      #;; -------------------------------
paeth_filter_2bpp:

      cmp        r1, r0

      vld1.16    {d0[0]},[r2]                   @; load 2 bytes (pixel x) from curr
                                                @;  row into d0[0]
      vld1.16    {d1[0]},[r3]!                  @; load 2 bytes (pixel b) from prev
                                                @;  row into d1[0]
                                                @; increment prev row pointer
      vadd.i8    d2,d0,d1                       @; d2 = x + b = updated pixel x
      vst1.16    {d2[0]},[r2]!                  @; store 2 bytes (updated pixel x)
                                                @; increment curr row pointer
      beq        paeth_filter_DONE

paeth_filter_2bpp_loop:
      add        r1,r1,r12                      @; loop counter += bpp
      cmp        r1,r0

      #;; d1[0] = c (b in the previous loop iteration)
      #;; d2[0] = a (x in the previous loop iteration)
      vld1.16    {d3[0]},[r3]!                  @; load 2 bytes (pixel b) from prev
                                                @;  row into d3[0]
      vld1.16    {d0[0]},[r2]                   @; load 2 bytes (pixel x) from curr
                                                @;  row into d0[0]
      vshll.u8   q4,d1,#1                       @; q4 = c<<1 = 2c
      vabdl.u8   q3,d2,d1                       @; q3 = pb = abs(a - c)
      vabdl.u8   q2,d3,d1                       @; q2 = pa = abs(b - c)
      vaddl.u8   q5,d2,d3                       @; q5 = a + b
      vabd.u16   q4,q5,q4                       @; q4 = pc = abs(a + b - 2c)

      vcle.s16   q5,q2,q3                       @; q5 = (pa <= pb)
      vcle.s16   q6,q2,q4                       @; q6 = (pa <= pc)
      vand       q5,q5,q6                       @; q5 = ((pa <= pb) && (pa <= pc))
      vcle.s16   q7,q3,q4                       @; q7 = (pb <= pc)
      vshrn.u16  d10,q5,#8                      @; d10 = ((pa <= pb) && (pa <= pc))
      vshrn.u16  d14,q7,#8                      @; d14 = (pb <= pc)

      vand       d2,d2,d10                      @; d2 = a where 1, 0 where 0
      vbsl       d14,d3,d1                      @; d14 = b where 1, c where 0
      vmvn       d10,d10                        @; invert d10
      vand       d14,d14,d10                    @; d14 = b/c where 1, 0 where 0
      vadd.i8    d2,d2,d14                      @; d2 = p = a/b/c where appropriate
      vadd.i8    d2,d2,d0                       @; d2 = x + p (updated pixel x)
      vmov       d1,d3                          @; d1 = b (c for next iteration)
      vst1.16    {d2[0]},[r2]!                  @; store 2 bytes (updated pixel x)
                                                @; increment curr row pointer
      bne        paeth_filter_2bpp_loop

      b          paeth_filter_DONE              @; exit loop when
                                                @;  loop counter == rowbytes
      #;; -------------------------------
      #;; PAETH filter, 3 bytes per pixel
      #;; -------------------------------
paeth_filter_3bpp:

      cmp        r1, r0

      vld1.32    {d0[0]},[r2]                   @; load 4 bytes (pixel x + 1 extra
                                                @;  byte) from curr row into d0[0]
      vld1.32     {d1[0]},[r3],r12              @; load 4 bytes (pixel b + 1 extra
                                                @;  byte) from prev row into d1[0]
                                                @; increment prev row pointer
      vadd.i8    d2,d0,d1                       @; d2 = x + b = updated pixel x
      vst1.16    {d2[0]},[r2]!                  @; store 2 bytes (updated pixel x)
                                                @; increment curr row pointer
      vst1.8     {d2[2]},[r2]!                  @; store 1 byte (updated pixel x)
                                                @; increment curr row pointer
      beq        paeth_filter_DONE

paeth_filter_3bpp_loop:
      add        r1,r1,r12                      @; loop counter += bpp
      cmp        r1,r0


      #;; d1[0] = c (b in the previous loop iteration)
      #;; d2[0] = a (x in the previous loop iteration)
      vld1.32    {d3[0]},[r3],r12               @; load 4 bytes (pixel b + 1 extra
                                                @;  byte) from prev row into d3[0]
      vld1.32    {d0[0]},[r2]                   @; load 4 bytes (pixel x + 1 extra
                                                @;  byte) from curr row into d0[0]
      vshll.u8   q4,d1,#1                       @; q4 = c<<1 = 2c
      vabdl.u8   q3,d2,d1                       @; q3 = pb = abs(a - c)
      vabdl.u8   q2,d3,d1                       @; q2 = pa = abs(b - c)
      vaddl.u8   q5,d2,d3                       @; q5 = a + b
      vabd.u16   q4,q5,q4                       @; q4 = pc = abs(a + b - 2c)
                                                @
      vcle.s16   q5,q2,q3                       @; q5 = (pa <= pb)
      vcle.s16   q6,q2,q4                       @; q6 = (pa <= pc)
      vand       q5,q5,q6                       @; q5 = ((pa <= pb) && (pa <= pc))
      vcle.s16   q7,q3,q4                       @; q7 = (pb <= pc)
      vshrn.u16  d10,q5,#8                      @; d10 = ((pa <= pb) && (pa <= pc))
      vshrn.u16  d14,q7,#8                      @; d14 = (pb <= pc)
                                                @
      vand       d2,d2,d10                      @; d2 = a where 1, 0 where 0
      vbsl       d14,d3,d1                      @; d14 = b where 1, c where 0
      vmvn       d10,d10                        @; invert d10
      vand       d14,d14,d10                    @; d14 = b/c where 1, 0 where 0
      vadd.i8    d2,d2,d14                      @; d2 = p = a/b/c where appropriate
      vadd.i8    d2,d2,d0                       @; d2 = x + p (updated pixel x)
      vmov       d1,d3                          @; d1 = b (c for next iteration)
      vst1.16    {d2[0]},[r2]!                  @; store 2 bytes (updated pixel x)
                                                @; increment curr row pointer
      vst1.8     {d2[2]},[r2]!                  @; store 1 byte (updated pixel x)
                                                @; increment curr row pointer
      bne        paeth_filter_3bpp_loop

      b          paeth_filter_DONE              @; exit loop when
                                                @;  loop counter == rowbytes
      #;; -------------------------------
      #;; PAETH filter, 4 bytes per pixel
      #;; -------------------------------
paeth_filter_4bpp:

     cmp        r1, r0

     vld1.32    {d0[0]},[r2]                    @; load 4 bytes (pixel x) from curr
                                                @;  row into d0[0]
     vld1.32    {d1[0]},[r3]!                   @; load 4 bytes (pixel b) from prev
                                                @;  row into d1[0]
                                                @; increment prev row pointer
     vadd.i8    d2,d0,d1                        @; d2 = x + b = updated pixel x
     vst1.32    {d2[0]},[r2]!                   @; store 4 bytes (updated pixel x)
                                                @; increment curr row pointer
     beq        paeth_filter_DONE

paeth_filter_4bpp_loop:
     add        r1,r1,r12                       @; loop counter += bpp
     cmp        r1,r0


     #;; d1[0] = c (b in the previous loop iteration)
     #;; d2[0] = a (x in the previous loop iteration)
     vld1.32    {d3[0]},[r3]!                   @; load 4 bytes (pixel b) from prev
                                                @;  row into d3[0]
     vld1.32    {d0[0]},[r2]                    @; load 4 bytes (pixel x) from curr
                                                @;  row into d0[0]
     vshll.u8   q4,d1,#1                        @; q4 = c<<1 = 2c
     vabdl.u8   q3,d2,d1                        @; q3 = pb = abs(a - c)
     vabdl.u8   q2,d3,d1                        @; q2 = pa = abs(b - c)
     vaddl.u8   q5,d2,d3                        @; q5 = a + b
     vabd.u16   q4,q5,q4                        @; q4 = pc = abs(a + b - 2c)
                                                @
     vcle.s16   q5,q2,q3                        @; q5 = (pa <= pb)
     vcle.s16   q6,q2,q4                        @; q6 = (pa <= pc)
     vand       q5,q5,q6                        @; q5 = ((pa <= pb) && (pa <= pc))
     vcle.s16   q7,q3,q4                        @; q7 = (pb <= pc)
     vshrn.u16  d10,q5,#8                       @; d10 = ((pa <= pb) && (pa <= pc))
     vshrn.u16  d14,q7,#8                       @; d14 = (pb <= pc)
                                                @
     vand       d2,d2,d10                       @; d2 = a where 1, 0 where 0
     vbsl       d14,d3,d1                       @; d14 = b where 1, c where 0
     vmvn       d10,d10                         @; invert d10
     vand       d14,d14,d10                     @; d14 = b/c where 1, 0 where 0
     vadd.i8    d2,d2,d14                       @; d2 = p = a/b/c where appropriate
     vadd.i8    d2,d2,d0                        @; d2 = x + p (updated pixel x)
     vmov       d1,d3                           @; d1 = b (c for next iteration)
     vst1.32    {d2[0]},[r2]!                   @; store 4 bytes (updated pixel x)
                                                @; increment curr row pointer
     bne        paeth_filter_4bpp_loop

     b          paeth_filter_DONE              @; exit loop when
                                               @;  loop counter == rowbytes
     #;; -------------------------------
     #;; PAETH filter, 6 bytes per pixel
     #;; -------------------------------
paeth_filter_6bpp:
     cmp        r1, r0

     vld1.8     {d0},[r2]                       @; load 8 bytes (pixel x + 2 extra
                                                @;  bytes) from curr row into d0
     vld1.8     {d1},[r3],r12                   @; load 8 bytes (pixel b + 2 extra
                                                @;  bytes) from prev row into d1
                                                @; increment prev row pointer
     vadd.i8    d2,d0,d1                        @; d2 = x + b = updated pixel x
     vst1.32    {d2[0]},[r2]!                   @; store 4 bytes (updated pixel x)
                                                @; increment curr row pointer
     vst1.16    {d2[2]},[r2]!                   @; store 2 bytes (updated pixel x)
                                                @; increment curr row pointer
     beq        paeth_filter_DONE

paeth_filter_6bpp_loop:
     add        r1,r1,r12                       @; loop counter += bpp
     cmp        r1,r0


     #;; d1[0] = c (b in the previous loop iteration)
     #;; d2[0] = a (x in the previous loop iteration)
     vld1.8     {d3},[r3],r12                   @; load 8 bytes (pixel b + 2 extra
                                                @;  bytes) from prev row into d3
     vld1.8     {d0},[r2]                       @; load 8 bytes (pixel x + 2 extra
                                                @;  bytes) from curr row into d0
     vshll.u8   q4,d1,#1                        @; q4 = c<<1 = 2c
     vabdl.u8   q3,d2,d1                        @; q3 = pb = abs(a - c)
     vabdl.u8   q2,d3,d1                        @; q2 = pa = abs(b - c)
     vaddl.u8   q5,d2,d3                        @; q5 = a + b
     vabd.u16   q4,q5,q4                        @; q4 = pc = abs(a + b - 2c)

     vcle.s16   q5,q2,q3                        @; q5 = (pa <= pb)
     vcle.s16   q6,q2,q4                        @; q6 = (pa <= pc)
     vand       q5,q5,q6                        @; q5 = ((pa <= pb) && (pa <= pc))
     vcle.s16   q7,q3,q4                        @; q7 = (pb <= pc)
     vshrn.u16  d10,q5,#8                       @; d10 = ((pa <= pb) && (pa <= pc))
     vshrn.u16  d14,q7,#8                       @; d14 = (pb <= pc)

     vand       d2,d2,d10                       @; d2 = a where 1, 0 where 0
     vbsl       d14,d3,d1                       @; d14 = b where 1, c where 0
     vmvn       d10,d10                         @; invert d10
     vand       d14,d14,d10                     @; d14 = b/c where 1, 0 where 0
     vadd.i8    d2,d2,d14                       @; d2 = p = a/b/c where appropriate
     vadd.i8    d2,d2,d0                        @; d2 = x + p (updated pixel x)
     vmov       d1,d3                           @; d1 = b (c for next iteration)
     vst1.32    {d2[0]},[r2]!                   @; store 4 bytes (updated pixel x)
                                                @; increment curr row pointer
     vst1.16    {d2[2]},[r2]!                   @; store 2 bytes (updated pixel x)
                                                @; increment curr row pointer
     bne        paeth_filter_6bpp_loop

     b          paeth_filter_DONE              @; exit loop when
                                               @;  loop counter == rowbytes
     #;; -------------------------------
     #;; PAETH filter, 8 bytes per pixel
     #;; -------------------------------
paeth_filter_8bpp:
    cmp        r1, r0

    vld1.8     {d0},[r2]                        @; load 8 bytes (pixel x) from curr
                                                @;  row into d0
    vld1.8     {d1},[r3]!                       @; load 8 bytes (pixel b) from prev
                                                @;  row into d1
                                                @; increment prev row pointer
    vadd.i8    d2,d0,d1                         @; d2 = x + b = updated pixel x
    vst1.8     {d2},[r2]!                       @; store 8 bytes (updated pixel x)
                                                @; increment curr row pointer
    beq        paeth_filter_DONE

paeth_filter_8bpp_loop:
    add        r1,r1,r12                        @; loop counter += bpp
    cmp        r1,r0


    #;; d1[0] = c (b in the previous loop iteration)
    #;; d2[0] = a (x in the previous loop iteration)
    vld1.8     {d3},[r3]!                       @; load 8 bytes (pixel b) from prev
                                                @;  row into d3
    vld1.8     {d0},[r2]                        @; load 8 bytes (pixel x) from curr
                                                @;  row into d0
    vshll.u8   q4,d1,#1                         @; q4 = c<<1 = 2c
    vabdl.u8   q3,d2,d1                         @; q3 = pb = abs(a - c)
    vabdl.u8   q2,d3,d1                         @; q2 = pa = abs(b - c)
    vaddl.u8   q5,d2,d3                         @; q5 = a + b
    vabd.u16   q4,q5,q4                         @; q4 = pc = abs(a + b - 2c)
                                                @
    vcle.s16   q5,q2,q3                         @; q5 = (pa <= pb)
    vcle.s16   q6,q2,q4                         @; q6 = (pa <= pc)
    vand       q5,q5,q6                         @; q5 = ((pa <= pb) && (pa <= pc))
    vcle.s16   q7,q3,q4                         @; q7 = (pb <= pc)
    vshrn.u16  d10,q5,#8                        @; d10 = ((pa <= pb) && (pa <= pc))
    vshrn.u16  d14,q7,#8                        @; d14 = (pb <= pc)
                                                @
    vand       d2,d2,d10                        @; d2 = a where 1, 0 where 0
    vbsl       d14,d3,d1                        @; d14 = b where 1, c where 0
    vmvn       d10,d10                          @; invert d10
    vand       d14,d14,d10                      @; d14 = b/c where 1, 0 where 0
    vadd.i8    d2,d2,d14                        @; d2 = p = a/b/c where appropriate
    vadd.i8    d2,d2,d0                         @; d2 = x + p (updated pixel x)
    vmov       d1,d3                            @; d1 = b (c for next iteration)
    vst1.8     {d2},[r2]!                       @; store 8 bytes (updated pixel x)
                                                @; increment curr row pointer
    bne        paeth_filter_8bpp_loop

    b          paeth_filter_DONE                @; exit loop when
                                                @;  loop counter == rowbytes
paeth_filter_DONE:

    VPOP       {q4-q7}
    bx         r14

DONE:
     bx   r14


.size png_read_filter_row_neon, .-png_read_filter_row_neon
     .END
