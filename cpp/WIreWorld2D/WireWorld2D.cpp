#include "WireWorld2D.h"

#include "RLE_WireWorld.h"


TField *WireWorld2D::getField() const {
    return field_;
}

void WireWorld2D::set(size_t i, size_t j, TWireWorldCell value) {
    field_->set(i, j, value);
}

TWireWorldCell WireWorld2D::get(size_t i, size_t j) {
    return field_->get(i, j);
}

size_t WireWorld2D::getCountOfHeads(TField &field, int x, int y) {
    size_t count = 0;
    for (int i = x - 1; i <= x + 1; i++) {
        for (int j = y - 1; j <= y + 1; j++) {
            if (i == x && j == y) {
                continue;
            }
            if (i < 0 || i >= height_) {
                continue;
            }

            if (j < 0 || j >= width_) {
                continue;
            }

            if (field(i, j) == TWireWorldCell::ELECTRON_HEAD) {
                count++;
            }
        }
    }
    return count;
}

bool WireWorld2D::proceedTick() {
    bool changed = false;
    TField cellsCopy(*field_);
    for (size_t i = 0; i < height_; i++) {
        for (size_t j = 0; j < width_; j++) {
            if (cellsCopy(i, j) == TWireWorldCell::ELECTRON_HEAD) {
                field_->set(i, j, TWireWorldCell::ELECTRON_TAIL);
            }
            else if (cellsCopy(i, j) == TWireWorldCell::ELECTRON_TAIL) {
                field_->set(i, j, TWireWorldCell::CONDUCTOR);
            }
            else if (cellsCopy(i, j) == TWireWorldCell::CONDUCTOR) {
                size_t heads = getCountOfHeads(cellsCopy, static_cast<int>(i),
                                               static_cast<int>(j));
                if (heads == 1 || heads == 2) {
                    field_->set(i, j, TWireWorldCell::ELECTRON_HEAD);
                    changed = true;
                }
            }
        }
    }
    return changed;
}

bool WireWorld2D::setField(TField *field) {
    field_ = field;
    return true;
}

bool WireWorld2D::setFieldFromFile(const std::string &fileName) {
    int width = static_cast<int>(width_);
    int height = static_cast<int>(height_);
    bool status = getFieldFromFile(fileName, field_, width_, height_,
                                   width, height);
    if (!status) {
        return false;
    }
    return true;
}
