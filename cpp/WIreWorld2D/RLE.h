#ifndef WIREWORLD2D_RLE_H
#define WIREWORLD2D_RLE_H

#include <fstream>
#include <string>

bool getDecodedRLE(std::ifstream &file, std::string &decoded, int row, int col);

#endif //WIREWORLD2D_RLE_H
