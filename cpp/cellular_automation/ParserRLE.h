#ifndef WIREWORLD2D_PARSERRLE_H
#define WIREWORLD2D_PARSERRLE_H

#include <algorithm>
#include <array>
#include <fstream>
#include <string>

#include "Field.h"

/*
 * Cell is a type of field cells. Decoder should
 * be a function, that does
 * char -> Cell
 * So decoder is a dictionary
 */
template<class Cell>
class ParserRLE {
public:
    using Decoder = Cell (*)(char);

    explicit ParserRLE(const std::string &fileName, Decoder decoder):
    decoder_(decoder) {
        file_ = std::ifstream(fileName);
    }

    ParserRLE(const ParserRLE &) = delete;

    void operator=(const ParserRLE &) = delete;

    void setFile(const std::string &fileName) {
        file_ = std::ifstream(fileName);
    }

    bool parse(Field<Cell> *field) {
        if (!file_.is_open()) {
            return false;
        }
        bool status = extractSizes();
        if (!status) {
            return false;
        }
        if (!getDecodedRLE()) {
            return false;
        }
        for (size_t i = 0; i < height_; i++) {
            for (size_t j = 0; j < width_; j++) {
                field->set(i, j, decoder_(stringField_[i * width_ + j]));
            }
        }
        return true;
    }

private:
    Decoder decoder_;
    std::ifstream file_;
    std::string stringField_;
    size_t width_ = 0;
    size_t height_ = 0;
    constexpr static char EMPTY_CELL = '.';
    constexpr static char END_CELL = '!';
    constexpr static char SKIP_CELL = '$';

    bool getDecodedRLE() {
        stringField_.clear();
        size_t countOfSkips = 0;
        int ch = 0;
        size_t countOfElems = 1;
        while ((ch = file_.get()) != END_CELL && !file_.eof()) {
            if (isspace(ch)) {
                continue;
            }
            if (ch == SKIP_CELL) {
                countOfSkips++;
                while (stringField_.length() % width_ != 0 ||
                       stringField_.length() == 0) {
                    stringField_.push_back(EMPTY_CELL);
                }
                if (countOfElems > 1) {
                    countOfSkips += countOfElems - 1;
                    for (size_t i = 0; i < countOfElems - 1; ++i) {
                        do {
                            stringField_.push_back(EMPTY_CELL);
                        } while (stringField_.length() % width_ != 0);
                    }
                    countOfElems = 1;
                }
            } else if (isdigit(ch)) {
                file_.unget();
                file_ >> countOfElems;
                if (countOfElems > width_ * height_) {
                    return false;
                }
            } else {
                for (size_t i = 0; i < countOfElems; ++i) {
                    stringField_.push_back((char) ch);
                }
                countOfElems = 1;
            }
        }
        return true;
    }

    bool extractSizes() {
        while (!file_.eof()) {
            int ch = file_.get();
            if (ch == '#') {
                while ((ch = file_.get()) != '\n'){};
            }
            if (ch == 'x') {
                width_ = extractSize();
                if (width_ == 0) {
                    return false;
                }
            } else if (ch == 'y') {
                height_ = extractSize();
                if (height_ == 0) {
                    return false;
                }
            }
            if (height_ > 0 && width_ > 0 && ch == '\n') {
                break;
            }
        }
        return true;
    }

    int extractSize() {
        if (!file_.is_open()) {
            return 0;
        }
        int ch;
        while (!isdigit(ch = file_.get())) {
            if (ch != ' ' && ch != '=') {
                return 0;
            }
        }
        int size = 0;
        do {
            size *= 10;
            size *= 10;
            size += ch - '0';
        } while (isdigit(ch = file_.get()));
        return size;
    }

};

#endif //WIREWORLD2D_PARSERRLE_H
