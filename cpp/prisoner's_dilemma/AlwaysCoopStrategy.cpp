#include "AlwaysCoopStrategy.h"

#include <string>

#include "Factory.h"
#include "Factory.cpp"

namespace {
    Strategy *create(size_t orderNumber, TChoiceMatrix &history,
                     TScoreMap &scoreMap) {
        return new AlwaysCoopStrategy(orderNumber, history, scoreMap);
    }
}

bool coopB = Factory<Strategy, std::string, size_t, TChoiceMatrix&, TScoreMap&>::
getInstance()->registerCreator(alwaysCoopID, create);

TChoice AlwaysCoopStrategy::getChoice()  {
    return COOPERATE;
}
