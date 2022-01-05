#include "AlwaysDefStrategy.h"

#include <string>

#include "Factory.h"

namespace {
    Strategy *create() {
        return new AlwaysDefStrategy();
    }
}

bool defB = Factory<Strategy, std::string>::
getInstance()->registerCreator(alwaysDefID, create);

TChoice AlwaysDefStrategy::getChoice() {
    return TChoice::DEF;
}
