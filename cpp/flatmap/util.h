#ifndef FLATMAP_UTIL_H
#define FLATMAP_UTIL_H

#include <cstddef>

constexpr int notFoundCode = -1;

template<class Type, class Comp>
int binarySearch(Type array[], int begin, int end, const Type &key, Comp comp) {
    assert(array);
    while (begin <= end && begin >= 0) {
        int mid = begin + (end - begin) / 2;
        if (comp(array[mid], key) == 0) {
            return mid;
        } else if (comp(array[mid], key) > 0) {
            end = mid - 1;
        } else if (comp(array[mid], key) < 0) {
            begin = mid + 1;
        }
    }
    return notFoundCode;
}

#endif //FLATMAP_UTIL_H
