#include "util.h"

#include <cstdio>

int binarySearch(void *array, int begin, int end,
                 int (*comp)(const void *, const void *), size_t elemSize,
                 const void *elem) {
    if (end >= begin) {
        int mid = begin + (end - begin) / 2;
        void *current = (char *) array + mid * elemSize;
        if (comp(current, elem) == 0) {
            return mid;
        }
        if (comp(current, elem) > 0) {
            return binarySearch(array, begin, mid - 1, comp,
                                elemSize, elem);
        }
        return binarySearch(array, mid + 1, end, comp, elemSize, elem);
    }
    return -1;
}

