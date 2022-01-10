#include "GameLifeQt.h"

#include "GamesIDs.h"
#include "GameQt.h"
#include "Factory.h"

namespace {
    GameQt *create(size_t height, size_t width) {
        return new GameLifeQt(height, width);
    }

    bool gameLife = Factory<GameQt, std::string, size_t, size_t>::getInstance()
    ->registerCreator(GamesIDs::GAME_LIFE_ID, create);
}
