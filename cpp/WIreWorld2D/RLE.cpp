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
