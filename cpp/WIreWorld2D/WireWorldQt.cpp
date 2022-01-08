#include "WireWorldQt.h"

#include "GameQt.h"
#include "Factory.h"

constexpr char WIREWORLD_ID[] = "WireWorld";

namespace {
    GameQt *create(size_t height, size_t width) {
        return new WireWorldQt(height, width);
    }

    bool wireWorld = Factory<GameQt, std::string, size_t, size_t>::getInstance()
            ->registerCreator(WIREWORLD_ID, create);
}
