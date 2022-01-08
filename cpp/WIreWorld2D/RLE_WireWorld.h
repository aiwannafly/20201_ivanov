#ifndef WIREWORLD2D_RLE_WIREWORLD_H
#define WIREWORLD2D_RLE_WIREWORLD_H

#include <fstream>
#include <string>

#include "VectorField.h"

bool getDecodedRLE(std::ifstream &file, std::string &decoded, int row, int col);

bool getFieldFromFile(const std::string &fileName, VectorField<int> *field,
                      size_t maxWidth, size_t maxHeight,
                      int &width, int &height);

#endif //WIREWORLD2D_RLE_WIREWORLD_H