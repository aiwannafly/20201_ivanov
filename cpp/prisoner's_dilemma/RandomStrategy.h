#ifndef PRISONER_DILEMMA_RANDOMSTRATEGY_H
#define PRISONER_DILEMMA_RANDOMSTRATEGY_H

#include "Strategy.h"

constexpr char randomID[] = "random";

class RandomStrategy : public Strategy {
public:
    RandomStrategy(size_t orderNumber, TChoiceMatrix &history,
                   TScoreMap &scoreMap) : Strategy(orderNumber, history,
                                                   scoreMap) {};
    ~RandomStrategy() override = default;

    TChoice getChoice() override;
};

#endif //PRISONER_DILEMMA_RANDOMSTRATEGY_H
