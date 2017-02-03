/* Copyright 2016 Samsung Electronics Co., LTD
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

/*
 * memory_file.cpp:
 *  An aiFile implementation to read directly from a memory buffer.
 */

#include "memory_file.h"

#ifdef JNI_LOG
#ifdef ANDROID
#include <android/log.h>
#define lprintf(...) __android_log_print(ANDROID_LOG_VERBOSE, __func__, __VA_ARGS__)
#else
#define lprintf(...) printf (__VA_ARGS__)
#endif /* ANDROID */
#else
#define lprintf
#endif

// Memory File

static size_t memoryFileReadProc(C_STRUCT aiFile* memFile, char* buf, size_t size, size_t count) {
    MemoryFileData &fileData(*reinterpret_cast<MemoryFileData*>(memFile->UserData));

    const size_t cnt = std::min(count, (fileData.size - fileData.pos) / size);
    const size_t ofs = size * cnt;

    memcpy(buf, fileData.buf + fileData.pos, ofs);
    fileData.pos += ofs;

    return cnt;
}

static size_t memoryFileWriteProc(C_STRUCT aiFile* memFile, const char* buf, size_t, size_t) {
    lprintf("Memory file shouldn't need to be written.");
    return 0;
}

static size_t memoryFileTellProc(C_STRUCT aiFile* memFile) {
    MemoryFileData &fileData(*reinterpret_cast<MemoryFileData*>(memFile->UserData));
    return fileData.pos;
}

static size_t memoryFileSizeProc(C_STRUCT aiFile* memFile) {
    MemoryFileData &fileData(*reinterpret_cast<MemoryFileData*>(memFile->UserData));
    return fileData.size;
}

static void memoryFileFlushProc(C_STRUCT aiFile* memFile) {
    MemoryFileData &fileData(*reinterpret_cast<MemoryFileData*>(memFile->UserData));
    lprintf("Memory file shouldn't need to be flushed.");
}

static aiReturn memoryFileSeek(C_STRUCT aiFile* memFile, size_t offset, aiOrigin origin) {
    MemoryFileData &fileData(*reinterpret_cast<MemoryFileData*>(memFile->UserData));

    switch (origin) {
    case aiOrigin_SET:
        if (offset >= fileData.size) {
            return aiReturn_FAILURE;
        }
        fileData.pos = offset;
        break;

    case aiOrigin_END:
        if (offset >= fileData.size) {
            return aiReturn_FAILURE;
        }
        fileData.pos = fileData.size - offset;
        break;

    default:
        if (offset + fileData.pos >= fileData.size) {
            return aiReturn_FAILURE;
        }
        fileData.pos += offset;
        break;
    }

    return aiReturn_SUCCESS;
}

aiFile memoryFilePrototype {
    .ReadProc = memoryFileReadProc,
    .WriteProc = memoryFileWriteProc,
    .TellProc = memoryFileTellProc,
    .FileSizeProc = memoryFileSizeProc,
    .SeekProc = memoryFileSeek,
    .FlushProc = memoryFileFlushProc,
    .UserData = nullptr
};
