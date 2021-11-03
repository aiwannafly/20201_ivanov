#include "AlwaysCoopStrategy.h"

#include <string>
#include "Factory.h"

namespace {
    Strategy *create(size_t orderNumber, TChoiceMatrix &history,
                     TScoreMap &scoreMap) {
        return new AlwaysCoopStrategy(orderNumber, history, scoreMap);
    }
    bool b = Factory<Strategy, std::string, Strategy* (*)(size_t, TChoiceMatrix&,
            TScoreMap&), size_t, TChoiceMatrix&, TScoreMap&>::
            getInstance()->registerCreator(alwaysCoopID, create);
}

TChoice AlwaysCoopStrategy::getChoice()  {
    return COOPERATE;
}
