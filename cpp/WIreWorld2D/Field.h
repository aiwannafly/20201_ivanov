#ifndef WIREWORLD2D_FIELD_H
#define WIREWORLD2D_FIELD_H

#include <cassert>
#include <vector>

template <class TCell>
class Field {
public:
    Field(size_t height, size_t width) : height_(height), width_(width) {
        field_ = std::vector<std::vector<TCell>>(height, std::vector<TCell>(width));
    };

    ~Field() = default;

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

#endif //WIREWORLD2D_FIELD_H
