#include "RandomStrategy.h"

#include "Factory.h"

#include <experimental/random>

namespace {
    Strategy *create() {
        return new RandomStrategy();
    }
}

bool randB = Factory<Strategy, std::string>
::getInstance()->registerCreator(randomID, create);

TChoice RandomStrategy::getChoice() {
    int randInt = std::experimental::randint(0, 1);
    if (randInt == 0) {
        return TChoice::COOP;
    }
    return TChoice::DEF;
}
