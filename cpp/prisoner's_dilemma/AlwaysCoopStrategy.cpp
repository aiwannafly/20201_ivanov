#include "AlwaysCoopStrategy.h"

#include <string>

#include "Factory.h"

constexpr char kAlwaysCoopID[] = "coop";

namespace {
    Strategy *create(size_t orderNumber, TChoicesList &history,
                     TScoreMap &scoreMap, TConfigs &configs) {
        return new AlwaysCoopStrategy(orderNumber, history, scoreMap, configs);
    }
}

bool coopB = Factory<Strategy, std::string, size_t, TChoicesList &, TScoreMap &, TConfigs &>::
getInstance()->registerCreator(kAlwaysCoopID, create);

TChoice AlwaysCoopStrategy::getChoice() {
    return COOP;
}
