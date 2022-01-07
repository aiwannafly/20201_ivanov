#ifndef WIREWORLD2D_GAME2D_H
#define WIREWORLD2D_GAME2D_H

#include <cstddef>

template <class CellState>
class Game2D {
public:
    Game2D() = default;

    Game2D(const Game2D &g) = delete;

    void operator=(const Game2D &g) = delete;

    virtual CellState get(size_t i, size_t j) = 0;

    virtual void set(size_t i, size_t j, CellState value) = 0;

    virtual bool setFieldFromFile(const std::string &fileName) = 0;

    virtual bool proceedTick() = 0;

};

#endif //WIREWORLD2D_GAME2D_H
