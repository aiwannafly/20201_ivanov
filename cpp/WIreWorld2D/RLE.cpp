#include "RLE.h"

#include <array>
#include <algorithm>

bool getDecodedRLE(std::ifstream &file, std::string &decoded, int row, int col) {
    if (row <= 0 || col <= 0) {
        return false;
    }
    decoded.clear();
    std::array<char, 8> allowedSymbols = {' ', '\n', '\t', '$', '.', 'A', 'B', 'C'};
    size_t countOfDollars = 0; // count of rows in a rle file
    int ch = 0x00;
    size_t countOfElems = 1;
    while ((ch = file.get()) != '!' && !file.eof()) {
        if (ch == '\n' || ch == ' ' || ch == '\t') {
            continue;
        }
        if (!isdigit(ch)) {
            if (allowedSymbols.end() == std::find(allowedSymbols.begin(), allowedSymbols.end(), ch)) {
                return false;
            }
        }
        if (ch == '$') {
            countOfDollars++;
            while (decoded.length() % col != 0 ||
                decoded.length() == 0) {
                decoded.push_back('.');
            }
            if (countOfElems > 1) {
                countOfDollars += countOfElems - 1;
                for (size_t i = 0; i < countOfElems - 1; ++i) {
                    do {
                        decoded.push_back('.');
                    } while (decoded.length() % col != 0);
                }
                countOfElems = 1;
            }
        } else if (isdigit(ch)) {
            file.unget();
            file >> countOfElems;
            if (countOfElems > row * col) {
                return false;
            }
        } else {
            for (size_t i = 0; i < countOfElems; ++i) {
                decoded.push_back((char) ch);
            }
            countOfElems = 1;
        }
    }
    return true;
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

enum TCellType getEnumCondition(char ch) {
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

bool getFieldFromFile(const std::string &fileName, TField *field,
                      size_t maxWidth, size_t maxHeight,
                      int &width, int &height) {
    std::ifstream fieldFile(fileName);
    width = 0;
    height = 0;
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
    if (height > maxHeight || width > maxWidth) {
        return false;
    }
    std::string stringField;
    if (!getDecodedRLE(fieldFile, stringField, height, width)) {
        return false;
    }
    size_t xOffset = (maxWidth - width) / 2;
    size_t yOffset = (maxHeight - height) / 2;
    for (size_t i = 0; i < maxHeight; i++) {
        for (size_t j = 0; j < maxWidth; j++) {
            field->at(i)[j] = EMPTY_CELL;
        }
    }
    for (size_t i = 0; i < height; i++) {
        for (size_t j = 0; j < width; j++) {
            field->at(i + yOffset)[j + xOffset] = getEnumCondition(stringField[i * width + j]);
        }
    }
    return true;
}
