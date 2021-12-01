#include "Runner.h"

#include "RLE.h"

TField &Runner::getField() {
    return cells_;
}

size_t Runner::getCountOfHeads(TField &field,
                               int x, int y) {
    size_t count = 0;
    for (int i = x - 1; i <= x + 1; i++) {
        for (int j = y - 1; j <= y + 1; j++) {
            if (i == x && j == y) {
                continue;
            }
            if (i < 0 || i >= fheight) {
                continue;
            }

            if (j < 0 || j >= fwidth) {
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
    TField cellsCopy(cells_);
    for (size_t i = yOffset_; i < yOffset_ + height_; i++) {
        for (size_t j = xOffset_; j < xOffset_ + width_; j++) {
            if (cellsCopy[i][j] == ELECTRON_HEAD) {
                cells_[i][j] = ELECTRON_TAIL;
            }
            else if (cellsCopy[i][j] == ELECTRON_TAIL) {
                cells_[i][j] = CONDUCTOR;
            }
            else if (cellsCopy[i][j] == CONDUCTOR) {
                size_t heads = getCountOfHeads(cellsCopy, static_cast<int>(i),
                                               static_cast<int>(j));
                if (heads == 1 || heads == 2) {
                    cells_[i][j] = ELECTRON_HEAD;
                    changed = true;
                }
            }
        }
    }
    return changed;
}

enum conditions getEnumCondition(char ch) {
    // turns char type condition into enum type
    switch (ch) {
        case 'A':
            return ELECTRON_HEAD;
        case 'B':
            return ELECTRON_TAIL;
        case 'C':
            return CONDUCTOR;
        default:
            return EMPTY_CELL;
    }
}

int extractSize(std::ifstream &fieldFile) {
    if (!fieldFile.is_open()) {
        return -1;
    }
    int ch;
    while (!isdigit(ch = fieldFile.get())) {
        if (ch != ' ' && ch != '=') {
            return -1;
        }
    }
    int size = 0;
    do {
        size *= 10;
        size += ch - '0';
    } while (isdigit(ch = fieldFile.get()));
    return size;
}

bool Runner::setField(TField& field) {
    cells_ = field;
    return true;
}

bool Runner::setField(const std::string &fileName) {
    std::ifstream fieldFile(fileName);
    int width = 0;
    int height = 0;
    if (!fieldFile.is_open()) {
        return false;
    }
    while (!fieldFile.eof()) {
        int ch = fieldFile.get();
        if (ch == '#') {
            while ((ch = fieldFile.get()) != '\n'){};
        }
        if (ch == 'x') {
            width = extractSize(fieldFile);
            if (width <= 0) {
                return false;
            }
        } else if (ch == 'y') {
            height = extractSize(fieldFile);
            if (height <= 0) {
                return false;
            }
        }
        if (height > 0 && width > 0 && ch == '\n') {
            break;
        }
    }
    if (height > fheight || width > fwidth) {
        return false;
    }
    std::string stringField;
    if (!getDecodedRLE(fieldFile, stringField, height, width)) {
        return false;
    }
    xOffset_ = (fwidth - width) / 2;
    yOffset_ = (fheight - height) / 2;
    for (size_t i = 0; i < height; i++) {
        for (size_t j = 0; j < width; j++) {
            cells_[i + yOffset_][j + xOffset_] =
                    getEnumCondition(stringField[i * width + j]);
        }
    }
    width_ = width;
    height_ = height;
    return true;
}
