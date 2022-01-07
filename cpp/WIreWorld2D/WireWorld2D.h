#ifndef WIREWORLD2D_WIREWORLD2D_H
#define WIREWORLD2D_WIREWORLD2D_H

#include <string>

#include "Game2D.h"
#include "WireWorldFieldTypes.h"

class WireWorld2D : public Game2D<TWireWorldCell> {
public:
    WireWorld2D(size_t height, size_t width) : width_(width), height_(height){
        field_ = new TField(height, width);
    };

    ~WireWorld2D() = default;

    void set(size_t i, size_t j, TWireWorldCell value) override;

    TWireWorldCell get(size_t i, size_t j) override;

    bool proceedTick() override;

    bool setFieldFromFile(const std::string &fileName) override;

    bool setField(TField *field);

    TField *getField() const;

private:
    size_t width_ = 0;
    size_t height_ = 0;
    TField *field_;

    size_t getCountOfHeads(TField &field, int x, int y);
};

#endif //WIREWORLD2D_WIREWORLD2D_H
