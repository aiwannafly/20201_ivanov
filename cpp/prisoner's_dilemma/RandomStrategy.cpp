#include "RandomStrategy.h"

#include <random>

#include "Factory.h"

namespace {
    Strategy *create(size_t orderNumber, TChoiceMatrix &history,
                     TScoreMap &scoreMap) {
        return new RandomStrategy(orderNumber, history, scoreMap);
    }
    bool b = Factory<Strategy, std::string, Strategy* (*)(size_t, TChoiceMatrix&,
            TScoreMap&), size_t, TChoiceMatrix&, TScoreMap&>::getInstance()->registerCreator(randomID, create);
}

TChoice RandomStrategy::getChoice() {
    int randInt = rand();
    if (randInt % 2 == 0) {
        return COOPERATE;
    }
    return DEFEND;
}
