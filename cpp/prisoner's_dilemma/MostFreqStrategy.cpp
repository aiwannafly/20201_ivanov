#include "MostFreqStrategy.h"

#include <map>
#include <string>

#include "Factory.h"

namespace {
    Strategy *create() {
        return new MostFreqStrategy();
    }
}

bool mostFreqB = Factory<Strategy, std::string>::
getInstance()->registerCreator(mostFreqID, create);

TChoice MostFreqStrategy::getChoice() {
    std::map<size_t, std::map<TChoice, size_t>> choiceCounts;
    for (size_t i = 0; i < 3; i++) {
        choiceCounts[i][TChoice::COOP] = 0;
        choiceCounts[i][TChoice::DEF] = 0;
    }
    for (auto currentLine: history_) {
        for (size_t j = 0; j < 3; j++) {
            choiceCounts[j][currentLine[j]] += 1;
        }
    }
    std::array<TChoice, combLen> mostUsedChoices = {TChoice::DEF, TChoice::DEF, TChoice::DEF};
    for (size_t i = 0; i < 3; i++) {
        if (choiceCounts[i][TChoice::COOP] > choiceCounts[i][TChoice::DEF]) {
            mostUsedChoices[i] = TChoice::COOP;
        }
    }
    mostUsedChoices[orderNumber_] = TChoice::DEF;
    size_t defScore = scoreMap_[mostUsedChoices][orderNumber_];
    mostUsedChoices[orderNumber_] = TChoice::COOP;
    size_t coopScore = scoreMap_[mostUsedChoices][orderNumber_];
    if (defScore > coopScore) {
        return TChoice::DEF;
    }
    return TChoice::COOP;
}
