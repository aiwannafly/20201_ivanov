#include "MostFreqStrategy.h"

#include <map>
#include <string>

#include "Factory.h"
#include "Factory.cpp"

namespace {
    Strategy *create(size_t orderNumber, TChoiceMatrix &history,
                     TScoreMap &scoreMap) {
        return new MostFreqStrategy(orderNumber, history, scoreMap);
    }
}

bool b = Factory<Strategy, std::string, Strategy* (*)(size_t, TChoiceMatrix&,
                                                      TScoreMap&), size_t, TChoiceMatrix&, TScoreMap&>::
getInstance()->registerCreator(mostFreqID, create);

TChoice MostFreqStrategy::getChoice() {
    TChoiceMatrix history = getHistory();
    TScoreMap scoreMap = getScoreMap();
    size_t orderNumber = getOrderNumber();
    std::map<size_t, std::map<TChoice, size_t>> choiceCounts;
    for (size_t i = 0; i < 3; i++) {
        choiceCounts[i][COOPERATE] = 0;
        choiceCounts[i][DEFEND] = 0;
    }
    for (auto currentLine : history) {
        for (size_t j = 0; j < 3; j++) {
            choiceCounts[j][currentLine[j]] += 1;
        }
    }
    std::array<TChoice, combLen> mostUsedChoices = {DEFEND, DEFEND, DEFEND};
    for (size_t i = 0; i < 3; i++) {
        if (choiceCounts[i][COOPERATE] > choiceCounts[i][DEFEND]) {
            mostUsedChoices[i] = COOPERATE;
        }
    }
    std::array<TChoice, combLen> firstComb = mostUsedChoices;
    firstComb[orderNumber] = DEFEND;
    std::array<TChoice, combLen> secondComb = mostUsedChoices;
    firstComb[orderNumber] = COOPERATE;
    if (scoreMap[firstComb][orderNumber] > scoreMap[secondComb][orderNumber]) {
        return DEFEND;
    }
    return COOPERATE;
}
