#include "AlwaysDefStrategy.h"

#include <string>

#include "Factory.h"

namespace {
    Strategy *create(size_t orderNumber, TChoicesList &history,
                     TScoreMap &scoreMap, TConfigs &configs) {
        return new AlwaysDefStrategy(orderNumber, history, scoreMap, configs);
    }
}

bool defB = Factory<Strategy, std::string, size_t, TChoicesList &, TScoreMap &, TConfigs &>::
getInstance()->registerCreator(alwaysDefID, create);

TChoice AlwaysDefStrategy::getChoice() {
    return DEF;
}
