#ifndef FLATMAP_UTIL_H
#define FLATMAP_UTIL_H

#include <cstddef>

constexpr int notFoundCode = -1;

template<class Type, class Comp>
int binarySearch(Type array[], int begin, int end, const Type &key, Comp comp);

#endif //FLATMAP_UTIL_H
