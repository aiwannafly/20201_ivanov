#include "handle_malloc.h"

#include <assert.h>
#include <ctype.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <memory.h>
#include <unistd.h>

#define LARGE_MEMORY_SIZE (100 * 1000000)
#define MIN_BLOCK_ALLOC_SIZE (1024)
#define BLOCK_COUNT(nbytes) ((nbytes + sizeof (THeader) - 1) / sizeof (THeader) + 1)
#define MAX_FILE_ROW_LEN (300)
#define MAX_MEM_FILE_NAME_LEN (50)
#define MAX_PID_LEN (20)
#define BORDER "============================================"

/* the reason why the union type is used is to make the */
/* proper aligning for the blocks in the memory */
typedef union THeader {
    struct {
        union THeader *Next;
        size_t BlockCount;
        size_t BytesCount;
        bool IsFree;
    } H;
    long AlignLong;     /* if the block may contain these types, then it's able to contain anything else */
    double AlignDouble;
} THeader;

/* The list of memory blocks is represented approximately in the following way: */
/* All the nodes are sorted from bottom to top by their addresses in the memory. */
/* [NEXT, BLOCK_COUNT| *BLOCK_COUNT HEADER BLOCKS*] ---> [NEXT, BLOCK_COUNT| *BLOCK_COUNT HEADER BLOCKS*] ---> ... */

static void CheckMemoryLeaks();

static THeader base = {0};        /* empty block for the beginning of a list */
static THeader baseLarge = {0};   /* base of a large block list */
static THeader *firstFree = NULL; /* the address of the first free block */
static THeader *firstFreeLarge = NULL;
static size_t globalBlockCount = 0;
static bool leaksCheckEnabled = false;

/* adds the address to the list of free to use memory blocks */
void Free(void *address) {
    assert(address);
    THeader *headerAddr = (THeader *) address - 1;
    if (headerAddr->H.IsFree) {
        if (WARNING_PRINTING_ENABLED) {
            fprintf(stderr, "\n%s\n%s\n", BORDER, BORDER);
            fprintf(stderr, "Attempting double free on address %d\n", (int) address);
        }
        return;
    }
    THeader *cur = NULL;
    /* one of the memory blocks list property is a strict order from bottom to top according
     * to the blocks addresses. When we add the new memory, we should not break the property. */
    for (cur = firstFree; headerAddr <= cur || cur->H.Next <= headerAddr; cur = cur->H.Next) {
        assert(cur->H.IsFree);
        assert(cur->H.Next->H.IsFree);
        if (cur == headerAddr) {
            /* in this case the block is free already */
            return;
        }
        if (cur >= cur->H.Next && (headerAddr > cur || headerAddr < cur->H.Next)) {
            /* it means that we got to the end of a list of blocks */
            /* and we should add new block to the end or to the beginning */
            break;
        }
    }
    if (headerAddr + headerAddr->H.BlockCount == cur->H.Next) {
        /* in this case we do merging with an upper neighbour
         * to make the memory more dense */
        headerAddr->H.BlockCount += cur->H.Next->H.BlockCount;
        headerAddr->H.Next = cur->H.Next->H.Next;
        globalBlockCount--;
    } else {
        headerAddr->H.Next = cur->H.Next;
    }
    if (cur + cur->H.BlockCount == headerAddr) {
        cur->H.BlockCount += headerAddr->H.BlockCount;
        cur->H.Next = headerAddr->H.Next;
        globalBlockCount--;
    } else {
        cur->H.Next = headerAddr;
    }
    headerAddr->H.IsFree = true;
    firstFree = cur;
}

/* gets memory from the OS and adds it to the list of free memory blocks */
static THeader *GetMemory(size_t blockCount) {
    if (blockCount < MIN_BLOCK_ALLOC_SIZE) {
        blockCount = MIN_BLOCK_ALLOC_SIZE;
    }
    char *memory = sbrk(blockCount * sizeof(THeader));
    if ((char *) -1 == memory) {
        return NULL; /* OS can not provide us with the additional memory */
    }
    globalBlockCount++;
    THeader *result = (THeader *) memory;
    result->H.BlockCount = blockCount;
    THeader *freeZone = result + 1;
    Free((void *) (freeZone));
    return result;
}

void *Malloc(size_t nbytes) {
    if (!leaksCheckEnabled) {
        atexit(CheckMemoryLeaks);
        leaksCheckEnabled = true;
    }
    if (0 == nbytes) {
        return NULL;
    }
    /* count of blocks with type 'THeader' required for storing
     * the memory itself and one first extra block for malloc information */
    size_t blockCount = BLOCK_COUNT(nbytes);
    if (NULL == firstFree) {
        /* when malloc work is started we create our memory block list
         * at the our base address. */
        firstFree = &base;
        firstFreeLarge = &baseLarge;
        base.H.Next = firstFree; /* the list should be cyclic */
        baseLarge.H.Next = firstFreeLarge;
        base.H.BlockCount = 0;
        baseLarge.H.BlockCount = 0;
        base.H.IsFree = true;
        baseLarge.H.IsFree = true;
    }
    THeader *previous = firstFree;
    if (nbytes >= LARGE_MEMORY_SIZE) {
        previous = firstFreeLarge;
    }
    for (THeader *current = previous->H.Next;; previous = current, current = current->H.Next) {
        assert(current->H.IsFree);
        assert(current->H.Next->H.IsFree);
        if (current->H.BlockCount >= blockCount) {
            if (current->H.BlockCount == blockCount) { /* exactly required nbytes */
                previous->H.Next = current->H.Next; /* removing the block from list of free blocks */
            } else { /* then we just cut off the tail */
                current->H.BlockCount -= blockCount;
                current += current->H.BlockCount;
                current->H.BlockCount = blockCount;
                globalBlockCount++;
            }
            firstFree = previous;
            current->H.BytesCount = nbytes;
            current->H.IsFree = false;
            current++; /* user should get address of a memory, not of a header block */
            return (void *) current;
        }
        if (current == firstFree || current == firstFreeLarge) {
            current = GetMemory(blockCount); /* we allocated some memory and put it in
            * some unknown position of the memory blocks list. Now we will return to
            * the beginning of the list and try to find the block. */
            if (NULL == current) { /* we have no more memory */
                return NULL;
            }
            previous = current;
            current = previous->H.Next;
        }
    }
}

void *Calloc(size_t count, size_t sizeOfElem) {
    void *memory = Malloc(count * sizeOfElem);
    if (NULL == memory) {
        return NULL;
    }
    char *iterator = memory;
    for (size_t i = 0; i < count * sizeOfElem; i++) {
        iterator[i] = 0;
    }
    return memory;
}

void *Realloc(void *address, size_t nbytes) {
    assert(address);
    if (0 == nbytes) {
        Free(address);
        return NULL;
    }
    THeader *headerAddr = (THeader *) address - 1;
    if (headerAddr->H.BytesCount < nbytes) {
        void *newAddress = Malloc(nbytes);
        memcpy(newAddress, address, headerAddr->H.BytesCount);
        Free(address);
        return newAddress;
    }
    if (headerAddr->H.BytesCount > nbytes) {
        size_t blockCount = BLOCK_COUNT(nbytes);
        if (headerAddr->H.BlockCount > blockCount) {
            THeader *toFree = (THeader *) address + blockCount;
            Free(toFree);
            headerAddr->H.BlockCount = blockCount;
        }
    }
    return address;
}

void GetMemoryFileName(char memFileName[MAX_MEM_FILE_NAME_LEN]) {
    int pid = getpid();
    char strPid[MAX_PID_LEN] = {0};
    char *prefix = "//proc/";
    char *postfix = "/maps";
    sprintf(strPid, "%d", pid);
    size_t prefixLen = strlen(prefix);
    size_t postfixLen = strlen(postfix);
    size_t middleLen = strlen(strPid);
    char *strings[3] = {prefix, strPid, postfix};
    size_t lengths[3] = {prefixLen, middleLen, postfixLen};
    size_t idx = 0;
    for (size_t i = 0; i < 3; i++) {
        for (size_t j = 0; j < lengths[i]; j++) {
            memFileName[idx] = strings[i][j];
            idx++;
        }
    }
}

static size_t GetCountOfLeakedBytes(size_t startAddress) {
    size_t count = 0;
    THeader *curHeader = (THeader *) startAddress;
    for (size_t i = 0; i < globalBlockCount; i++) {
        if (false == curHeader->H.IsFree) {
            count += curHeader->H.BytesCount;
        }
        curHeader += curHeader->H.BlockCount;
    }
    return count;
}

static size_t GetAddress(const char strAddress[8]) {
    size_t address = 0;
    for (size_t i = 0; i < 8; i++) {
        size_t idx = 7 - i;
        char ch = strAddress[idx];
        size_t num = 0;
        if (isdigit(ch)) {
            num = ch - '0';
        } else {
            // A, B, C, D, E, F
            num = ch - 'a' + 10;
        }
        address += num * (size_t) pow(16, (double) i);
    }
    return address;
}

static void FillRow(FILE *fp, char row[MAX_FILE_ROW_LEN]) {
    for (size_t i = 0; i < MAX_FILE_ROW_LEN; i++) {
        int ch = fgetc(fp);
        if (EOF == ch) {
            break;
        }
        if ('\n' == ch) {
            break;
        }
        row[i] = (char) ch;
    }
}

static bool IsHeapMemoryRow(char row[MAX_FILE_ROW_LEN]) {
    const char *postfix = "[heap]";
    size_t postfixLen = strlen(postfix);
    size_t rowLen = strlen(row);
    if (rowLen < postfixLen) {
        return false;
    }
    char rowPostfix[6 + 1] = {0}; // len of postfix + 1
    for (size_t i = 0; i < postfixLen; i++) {
        rowPostfix[i] = row[rowLen - postfixLen + i];
    }
    if (0 == strcmp(rowPostfix, postfix)) {
        return true;
    }
    return false;
}

static void CheckMemoryLeaks() {
    char memFileName[MAX_MEM_FILE_NAME_LEN] = {0};
    GetMemoryFileName(memFileName);
    FILE *fp = fopen(memFileName, "r");
    if (NULL == fp) {
        return;
    }
    while (1) {
        char row[MAX_FILE_ROW_LEN] = {0};
        FillRow(fp, row);
        if (0 == row[0]) {
            // end of file
            break;
        }
        if (IsHeapMemoryRow(row)) {
            //then we may find our heap blocks addresses here
            char startAddressStr[8 + 1] = {0};
            for (size_t i = 0; i < 8; i++) {
                startAddressStr[i] = row[i];
            }
            size_t startAddress = GetAddress(startAddressStr);
            size_t leakedBytes = GetCountOfLeakedBytes(startAddress);
            if (leakedBytes != 0) {
                if (WARNING_PRINTING_ENABLED) {
                    fprintf(stderr, "\n%s\n%s\n", BORDER, BORDER);
                    fprintf(stderr, "HANDLE SANITIZER: Detected memory leaks, totally "
                                    "%zu bytes were not freed.\n", leakedBytes);
                }
            }
            break;
        } else {
            continue;
        }
    }
}
