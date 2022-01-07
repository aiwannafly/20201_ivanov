#include "RunnerWireWorld.h"

#include "RLE_WireWorld.h"


TField *RunnerWireWorld::getField() const {
    return field_;
}

size_t RunnerWireWorld::getCountOfHeads(TField &field, int x, int y) {
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

bool RunnerWireWorld::proceedTick() {
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

bool RunnerWireWorld::setField(TField *field) {
    field_ = field;
    return true;
}

bool RunnerWireWorld::setFieldFromFile(const std::string &fileName) {
    int width = static_cast<int>(width_);
    int height = static_cast<int>(height_);
    bool status = getFieldFromFile(fileName, field_, width_, height_,
                                   width, height);
    if (!status) {
        return false;
    }
    return true;
}
