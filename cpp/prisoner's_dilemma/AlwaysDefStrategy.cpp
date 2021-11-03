#include "AlwaysDefStrategy.h"

#include <string>

#include "Factory.h"
#include "Factory.cpp"

namespace {
    Strategy *create(size_t orderNumber, TChoiceMatrix &history,
                     TScoreMap &scoreMap) {
        return new AlwaysDefStrategy(orderNumber, history, scoreMap);
    }
}

bool defB = Factory<Strategy, std::string, size_t, TChoiceMatrix&, TScoreMap&>::
getInstance()->registerCreator(alwaysDefID, create);

TChoice AlwaysDefStrategy::getChoice() {
    return DEFEND;
}
