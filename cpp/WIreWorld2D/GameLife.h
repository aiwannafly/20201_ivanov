#ifndef WIREWORLD2D_GAMELIFE_H
#define WIREWORLD2D_GAMELIFE_H

#include <string>
#include <memory>

#include "Game.h"
#include "VectorField.h"

class GameLife : Game {
public:
    GameLife(size_t width, size_t height) : width_(width), height_(height) {
        field_ = std::make_unique<VectorField<int>>(height, width);
    }

    ~GameLife() override = default;

    void set(size_t i, size_t j, int value) override {
        field_->set(i, j, value);
    }

    int get(size_t i, size_t j) override {
        return field_->get(i, j);
    }

    bool proceedTick() override;

    bool setFieldFromFile(const std::string &fileName) override;

    static constexpr int DEAD = 0;
    static constexpr int ALIVE = 1;
private:
    size_t height_;
    size_t width_;
    std::unique_ptr<VectorField<int>> field_;

    size_t getCountOfAlive(VectorField<int> &field, int x, int y) const;
};

#endif //WIREWORLD2D_GAMELIFE_H
