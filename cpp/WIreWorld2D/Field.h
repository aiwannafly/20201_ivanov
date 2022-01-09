#ifndef WIREWORLD2D_FIELD_H
#define WIREWORLD2D_FIELD_H

#include <cstddef>

template <class TCell>
class Field {
public:
    Field() = default;

    virtual ~Field() = default;

    virtual void set(size_t x, size_t y, TCell value) = 0;

    virtual TCell get(size_t x, size_t y) = 0;

};

#endif //WIREWORLD2D_FIELD_H
