#ifndef WIREWORLD2D_WIREWORLD_H
#define WIREWORLD2D_WIREWORLD_H

#include <string>

#include "Game.h"
#include "VectorField.h"

class WireWorld : public Game {
public:
    WireWorld(size_t width, size_t height) : height_(height), width_(width) {
        field_ = new VectorField<int>(width, height);
    };

    ~WireWorld() = default;

    void set(size_t i, size_t j, int state) override;

    int get(size_t i, size_t j) override;

    bool proceedTick() override;

    bool setFieldFromFile(const std::string &fileName) override;

    static constexpr int EMPTY_CELL = 0;
    static constexpr int ELECTRON_TAIL = 1;
    static constexpr int ELECTRON_HEAD = 2;
    static constexpr int CONDUCTOR = 3;

private:
    size_t width_ = 0;
    size_t height_ = 0;
    VectorField<int> *field_;

    size_t getCountOfHeads(VectorField<int> &field, int x, int y) const;
};

#endif //WIREWORLD2D_WIREWORLD_H
