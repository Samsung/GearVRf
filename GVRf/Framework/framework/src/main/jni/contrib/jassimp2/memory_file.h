/*
 * memory_file.h:
 *  An aiFile implementation to read directly from a memory buffer.
 */

#ifndef JASSIMP_MEMORY_FILE_H_
#define JASSIMP_MEMORY_FILE_H_

#include <stddef.h>
#include <assimp/cfileio.h>

struct MemoryFileData {
    unsigned char *buf;
    size_t size;
    size_t pos;
};

extern aiFile memoryFilePrototype;

#endif /* JASSIMP_MEMORY_FILE_H_ */
