#include "AlwaysCoopStrategy.h"

#include <string>

#include "Factory.h"

constexpr char kAlwaysCoopID[] = "coop";

namespace {
    Strategy *create() {
        return new AlwaysCoopStrategy();
    }
}

bool coopB = Factory<Strategy, std::string>::getInstance()->
registerCreator(kAlwaysCoopID, create);

TChoice AlwaysCoopStrategy::getChoice() {
    return TChoice::COOP;
}
