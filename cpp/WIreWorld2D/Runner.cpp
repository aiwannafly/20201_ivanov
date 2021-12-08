#include "Runner.h"

#include "RLE.h"

Runner::Runner(size_t width, size_t height) : width_(width), height_(height){
    for (size_t i = 0; i < height; i++) {
        std::vector<TCellType> line;
        for (size_t j = 0; j < width; j++) {
            line.push_back(EMPTY_CELL);
        }
        field_.push_back(line);
    }
};

TField &Runner::getField() {
    return field_;
}

size_t Runner::getCountOfSteps() const {
    return stepsCount_;
}

size_t Runner::getCountOfHeads(TField &field, int x, int y) {
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

            if (field[i][j] == ELECTRON_HEAD) {
                count++;
            }
        }
    }
    return count;
}

bool Runner::proceedTick() {
    bool changed = false;
    TField cellsCopy(field_);
    for (size_t i = 0; i < height_; i++) {
        for (size_t j = 0; j < width_; j++) {
            if (cellsCopy[i][j] == ELECTRON_HEAD) {
                field_[i][j] = ELECTRON_TAIL;
            }
            else if (cellsCopy[i][j] == ELECTRON_TAIL) {
                field_[i][j] = CONDUCTOR;
            }
            else if (cellsCopy[i][j] == CONDUCTOR) {
                size_t heads = getCountOfHeads(cellsCopy, static_cast<int>(i),
                                               static_cast<int>(j));
                if (heads == 1 || heads == 2) {
                    field_[i][j] = ELECTRON_HEAD;
                    changed = true;
                }
            }
        }
    }
    stepsCount_++;
    return changed;
}

bool Runner::setField(TField& field) {
    field_ = field;
    return true;
}

bool Runner::setFieldFromFile(const std::string &fileName) {
    stepsCount_ = 0;
    int width = static_cast<int>(width_);
    int height = static_cast<int>(height_);
    bool status = getFieldFromFile(fileName, &field_, width_, height_,
                                   width, height);
    if (!status) {
        return false;
    }
    return true;
}

void Runner::clearSteps() {
    stepsCount_ = 0;
}
