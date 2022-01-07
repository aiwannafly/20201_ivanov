#ifndef WIREWORLD2D_VECTORFIELD_H
#define WIREWORLD2D_VECTORFIELD_H

#include <cassert>
#include <vector>

#include "Field.h"

template <class TCell>
class VectorField : public Field<TCell> {
public:
    VectorField(size_t height, size_t width) : height_(height), width_(width) {
        field_ = std::vector<std::vector<TCell>>(height, std::vector<TCell>(width));
    };

    ~VectorField() = default;

    TCell operator()(size_t x, size_t y) {
        assert(y < height_);
        assert(x < width_);
        return field_[y][x];
    }

    void set(size_t x, size_t y, TCell value) {
        assert(y < height_);
        assert(x < width_);
        field_[y][x] = value;
    }

    TCell get(size_t x, size_t y) {
        assert(y < height_);
        assert(x < width_);
        return field_[y][x];
    }

private:
    std::vector<std::vector<TCell>> field_;
    size_t height_;
    size_t width_;
};

#endif //WIREWORLD2D_VECTORFIELD_H
