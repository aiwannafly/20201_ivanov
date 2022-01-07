#ifndef WIREWORLD2D_GAMELIFE2D_H
#define WIREWORLD2D_GAMELIFE2D_H

#include <string>

#include "Game2D.h"
#include "VectorField.h"

enum class TGameLifeCell {
    DEAD, ALIVE
};

class GameLife2D: Game2D<TGameLifeCell> {
public:
    GameLife2D(size_t height, size_t width) : height_(height), width_(width) {
        field_ = new VectorField<TGameLifeCell>(height, width);
    }

    void set(size_t i, size_t j, TGameLifeCell value) override {
        field_->set(i, j, value);
    }

    TGameLifeCell get(size_t i, size_t j) override {
        return field_->get(i, j);
    }

    bool proceedTick() override;

    bool setFieldFromFile(const std::string &fileName) override {
        return true;
    }

private:
    size_t height_;
    size_t width_;
    VectorField<TGameLifeCell> *field_;

    size_t getCountOfAlive(VectorField<TGameLifeCell> &field, int x, int y) const;
};

#endif //WIREWORLD2D_GAMELIFE2D_H
