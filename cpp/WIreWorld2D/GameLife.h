#ifndef WIREWORLD2D_GAMELIFE_H
#define WIREWORLD2D_GAMELIFE_H

#include <string>

#include "Game.h"
#include "VectorField.h"

class GameLife : Game {
public:
    GameLife(size_t height, size_t width) : height_(height), width_(width) {
        field_ = new VectorField<int>(height, width);
    }

    void set(size_t i, size_t j, int value) override {
        field_->set(i, j, value);
    }

    int get(size_t i, size_t j) override {
        return field_->get(i, j);
    }

    bool proceedTick() override;

    bool setFieldFromFile(const std::string &fileName) override {
        return true;
    }

    static constexpr int DEAD = 0;
    static constexpr int ALIVE = 1;
private:
    size_t height_;
    size_t width_;
    VectorField<int> *field_;

    size_t getCountOfAlive(VectorField<int> &field, int x, int y) const;
};

#endif //WIREWORLD2D_GAMELIFE_H