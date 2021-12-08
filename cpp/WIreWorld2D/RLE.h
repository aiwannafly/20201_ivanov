#ifndef WIREWORLD2D_RLE_H
#define WIREWORLD2D_RLE_H

#include <fstream>
#include <string>
#include <vector>

#ifndef TCELLTYPE
#define TCELLTYPE
enum TCellType {
    EMPTY_CELL, ELECTRON_TAIL, ELECTRON_HEAD, CONDUCTOR
};
#endif

using TField = std::vector<std::vector<TCellType>>;

bool getDecodedRLE(std::ifstream &file, std::string &decoded, int row, int col);

bool getFieldFromFile(const std::string &fileName, TField *field,
                      size_t maxWidth, size_t maxHeight,
                      int &width, int &height);

#endif //WIREWORLD2D_RLE_H
