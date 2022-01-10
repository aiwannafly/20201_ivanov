#include "WireWorldQt.h"

#include "GamesIDs.h"
#include "GameQt.h"
#include "Factory.h"

namespace {
    GameQt *create(size_t height, size_t width) {
        return new WireWorldQt(height, width);
    }

    bool wireWorld = Factory<GameQt, std::string, size_t, size_t>::getInstance()
            ->registerCreator(GamesIDs::WIREWORLD_ID, create);
}
