#ifndef FLATMAP_UTIL_H
#define FLATMAP_UTIL_H

#include <cstddef>

int binarySearch(void *array, int begin, int end,
                 int (*comp)(const void *, const void *), size_t elemSize,
                 const void *elem);

#endif //FLATMAP_UTIL_H
