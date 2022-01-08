#include "GameLifeQt.h"

#include "GameQt.h"
#include "Factory.h"

constexpr char GAME_LIFE_ID[] = "Game Life";

namespace {
    GameQt *create(size_t height, size_t width) {
        return new GameLifeQt(height, width);
    }

    bool gameLife = Factory<GameQt, std::string, size_t, size_t>::getInstance()
    ->registerCreator(GAME_LIFE_ID, create);
}
