#include "RandomStrategy.h"

#include "Factory.h"
#include "Factory.cpp"

namespace {
    Strategy *create(size_t orderNumber, TChoiceMatrix &history,
                     TScoreMap &scoreMap, TConfigs &configs) {
        return new RandomStrategy(orderNumber, history, scoreMap, configs);
    }
}

bool randB = Factory<Strategy, std::string, size_t, TChoiceMatrix &, TScoreMap &, TConfigs &>
::getInstance()->registerCreator(randomID, create);

TChoice RandomStrategy::getChoice() {
    int randInt = rand();
    if (randInt % 2 == 0) {
        return COOP;
    }
    return DEF;
}
