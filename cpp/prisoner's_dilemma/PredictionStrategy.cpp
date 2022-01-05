#include "PredictionStrategy.h"

#include <string>

#include "Factory.h"

namespace {
    Strategy *create() {
        return new PredictionStrategy();
    }
}

bool predB = Factory<Strategy, std::string>::
getInstance()->registerCreator(predictionID, create);

// scores an expected value with variant C, variant D, then chooses the best
TChoice PredictionStrategy::getChoice() {
    std::vector<size_t> others;
    for (size_t i = 0; i < combLen; i++) {
        if (i != orderNumber_) {
            others.push_back(i);
        }
    }
    std::map<std::array<TChoice, 2>, size_t> freqMap = {
            {{TChoice::COOP, TChoice::COOP}, 0},
            {{TChoice::COOP, TChoice::DEF},  0},
            {{TChoice::DEF,  TChoice::COOP}, 0},
            {{TChoice::DEF,  TChoice::DEF},  0}
    };
    for (const auto &comb: history_) {
        freqMap[{comb[others[0]], comb[others[1]]}] += 1;
    }
    double coopExpectedValue = 0;
    double defendExpectedValue = 0;
    TChoice choices[2] = {TChoice::COOP, TChoice::DEF};
    for (const auto &choice1: choices) {
        for (const auto &choice2: choices) {
            std::array<TChoice, 3> comb = {TChoice::COOP, TChoice::COOP, TChoice::COOP};
            comb[others[0]] = choice1;
            comb[others[1]] = choice2;
            coopExpectedValue += ((double) freqMap[{choice1, choice2}] / (double) history_.size())
                                 * (double) scoreMap_[comb][orderNumber_];
            comb[orderNumber_] = TChoice::DEF;
            defendExpectedValue += ((double) freqMap[{choice1, choice2}] / (double) history_.size())
                                   * (double) scoreMap_[comb][orderNumber_];
        }
    }
    if (coopExpectedValue > defendExpectedValue) {
        return TChoice::COOP;
    }
    return TChoice::DEF;
}
