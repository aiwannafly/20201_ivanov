#ifndef PRISONER_DILEMMA_ALWAYSCOOPSTRATEGY_H
#define PRISONER_DILEMMA_ALWAYSCOOPSTRATEGY_H

#include "Strategy.h"

constexpr char alwaysCoopID[] = "coop";

class AlwaysCoopStrategy : public Strategy {
public:
    AlwaysCoopStrategy(size_t orderNumber, TChoiceMatrix &history,
    TScoreMap &scoreMap, TConfigs &configs) : Strategy(orderNumber, history,
            scoreMap, configs) {}

    ~AlwaysCoopStrategy() override = default;

    TChoice getChoice() override;
};

#endif //PRISONER_DILEMMA_ALWAYSCOOPSTRATEGY_H
