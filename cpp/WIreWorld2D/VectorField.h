#ifndef WIREWORLD2D_VECTORFIELD_H
#define WIREWORLD2D_VECTORFIELD_H

#include <cassert>
#include <vector>

#include "Field.h"

template <class TCell>
class VectorField : public Field<TCell> {
public:
    VectorField(size_t width, size_t height) : width_(width), height_(height) {
        field_ = std::vector<std::vector<TCell>>(height, std::vector<TCell>(width));
    };

    ~VectorField() = default;

    TCell operator()(size_t i, size_t j) {
//        assert(i < height_);
//        assert(j < width_);
        if (i < height_ && j < width_) {
            return field_[i][j];
        }
        return TCell();
    }

    void set(size_t i, size_t j, TCell value) {
        if (i >= height_ || j >= width_) {
            return;
        }
        field_[i][j] = value;
    }

    TCell get(size_t i, size_t j) {
        if (i < height_ && j < width_) {
            return field_[i][j];
        }
        return TCell();
    }

private:
    std::vector<std::vector<TCell>> field_;
    size_t height_;
    size_t width_;
};

#endif //WIREWORLD2D_VECTORFIELD_H
