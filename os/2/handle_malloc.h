#ifndef MALLOC_HANDLE_MALLOC_H
#define MALLOC_HANDLE_MALLOC_H

#include <stddef.h>
#define WARNING_PRINTING_ENABLED 1
#define MAX_MEM_FILE_NAME_LEN (50)

void *Malloc(size_t nbytes);
void *Calloc(size_t count , size_t sizeOfElem);
void *Realloc(void *address, size_t nbytes);
void Free(void *address);

void GetMemoryFileName(char memFileName[MAX_MEM_FILE_NAME_LEN]);

#endif //MALLOC_HANDLE_MALLOC_H
