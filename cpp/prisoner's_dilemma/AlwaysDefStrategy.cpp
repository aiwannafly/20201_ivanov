#include "AlwaysDefStrategy.h"

#include <string>

#include "Factory.h"
#include "Factory.cpp"

namespace {
    Strategy *create(size_t orderNumber, TChoiceMatrix &history,
                     TScoreMap &scoreMap) {
        return new AlwaysDefStrategy(orderNumber, history, scoreMap);
    }
    bool b = Factory<Strategy, std::string, Strategy* (*)(size_t, TChoiceMatrix&,
             TScoreMap&), size_t, TChoiceMatrix&, TScoreMap&>::
             getInstance()->registerCreator(alwaysDefID, create);
}

TChoice AlwaysDefStrategy::getChoice() {
    return DEFEND;
}
