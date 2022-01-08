#include "WireWorld.h"

#include "RLE_WireWorld.h"

void WireWorld::set(size_t i, size_t j, int state) {
    field_->set(i, j, state);
}

int WireWorld::get(size_t i, size_t j) {
    return field_->get(i, j);
}

size_t WireWorld::getCountOfHeads(VectorField<int> &field, int x, int y) const {
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

            if (field(i, j) == WireWorld::ELECTRON_HEAD) {
                count++;
            }
        }
    }
    return count;
}

bool WireWorld::proceedTick() {
    bool changed = false;
    VectorField<int> cellsCopy(*field_);
    for (size_t i = 0; i < height_; i++) {
        for (size_t j = 0; j < width_; j++) {
            if (cellsCopy(i, j) == WireWorld::ELECTRON_HEAD) {
                field_->set(i, j, WireWorld::ELECTRON_TAIL);
            }
            else if (cellsCopy(i, j) == WireWorld::ELECTRON_TAIL) {
                field_->set(i, j, WireWorld::CONDUCTOR);
            }
            else if (cellsCopy(i, j) == WireWorld::CONDUCTOR) {
                size_t heads = getCountOfHeads(cellsCopy, static_cast<int>(i),
                                               static_cast<int>(j));
                if (heads == 1 || heads == 2) {
                    field_->set(i, j, WireWorld::ELECTRON_HEAD);
                    changed = true;
                }
            }
        }
    }
    return changed;
}

bool WireWorld::setFieldFromFile(const std::string &fileName) {
    int width = static_cast<int>(width_);
    int height = static_cast<int>(height_);
    bool status = getFieldFromFile(fileName, field_, width_, height_,
                                   width, height);
    if (!status) {
        return false;
    }
    return true;
}
