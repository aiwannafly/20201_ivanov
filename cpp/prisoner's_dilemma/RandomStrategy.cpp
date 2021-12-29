#include "RandomStrategy.h"

#include "Factory.h"

namespace {
    Strategy *create(size_t orderNumber, TChoicesList &history,
                     TScoreMap &scoreMap, TConfigs &configs) {
        return new RandomStrategy(orderNumber, history, scoreMap, configs);
    }
}

bool randB = Factory<Strategy, std::string, size_t, TChoicesList &, TScoreMap &, TConfigs &>
::getInstance()->registerCreator(randomID, create);

TChoice RandomStrategy::getChoice() {
    int randInt = rand();
    if (randInt % 2 == 0) {
        return COOP;
    }
    return DEF;
}
