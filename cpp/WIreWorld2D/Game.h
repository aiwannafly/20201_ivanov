#ifndef WIREWORLD2D_GAME_H
#define WIREWORLD2D_GAME_H

#include <cstddef>

class Game {
public:
    Game() = default;

    virtual ~Game() = default;

    Game(const Game &g) = delete;

    void operator=(const Game &g) = delete;

    virtual int get(size_t i, size_t j) = 0;

    virtual void set(size_t i, size_t j, int state) = 0;

    virtual bool setFieldFromFile(const std::string &fileName) = 0;

    virtual bool proceedTick() = 0;

};

#endif //WIREWORLD2D_GAME_H
