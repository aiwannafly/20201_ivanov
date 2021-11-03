#ifndef PRISONER_DILEMMA_ALWAYSDEFSTRATEGY_H
#define PRISONER_DILEMMA_ALWAYSDEFSTRATEGY_H

#include "Strategy.h"

constexpr char alwaysDefID[] = "always_def";

class AlwaysDefStrategy : public Strategy{
public:
    AlwaysDefStrategy(size_t orderNumber, TChoiceMatrix &history,
                      TScoreMap &scoreMap) : Strategy(orderNumber, history,
                                                      scoreMap) {};
    ~AlwaysDefStrategy() override = default;

    TChoice getChoice() override;
};

#endif //PRISONER_DILEMMA_ALWAYSDEFSTRATEGY_H
