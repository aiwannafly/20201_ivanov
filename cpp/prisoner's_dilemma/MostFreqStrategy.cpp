#include "MostFreqStrategy.h"

#include <map>
#include <string>

#include "Factory.h"

namespace {
    Strategy *create(size_t orderNumber, TChoicesList &history,
                     TScoreMap &scoreMap, TConfigs &configs) {
        return new MostFreqStrategy(orderNumber, history, scoreMap, configs);
    }
}

bool mostFreqB = Factory<Strategy, std::string, size_t, TChoicesList &, TScoreMap &, TConfigs &>::
getInstance()->registerCreator(mostFreqID, create);

TChoice MostFreqStrategy::getChoice() {
    std::map<size_t, std::map<TChoice, size_t>> choiceCounts;
    for (size_t i = 0; i < 3; i++) {
        choiceCounts[i][COOP] = 0;
        choiceCounts[i][DEF] = 0;
    }
    for (auto currentLine: history_) {
        for (size_t j = 0; j < 3; j++) {
            choiceCounts[j][currentLine[j]] += 1;
        }
    }
    std::array<TChoice, combLen> mostUsedChoices = {DEF, DEF, DEF};
    for (size_t i = 0; i < 3; i++) {
        if (choiceCounts[i][COOP] > choiceCounts[i][DEF]) {
            mostUsedChoices[i] = COOP;
        }
    }
    std::array<TChoice, combLen> firstComb = mostUsedChoices;
    firstComb[orderNumber_] = DEF;
    std::array<TChoice, combLen> secondComb = mostUsedChoices;
    firstComb[orderNumber_] = COOP;
    if (scoreMap_[firstComb][orderNumber_] > scoreMap_[secondComb][orderNumber_]) {
        return DEF;
    }
    return COOP;
}
