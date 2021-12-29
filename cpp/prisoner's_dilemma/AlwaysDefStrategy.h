#ifndef PRISONER_DILEMMA_ALWAYSDEFSTRATEGY_H
#define PRISONER_DILEMMA_ALWAYSDEFSTRATEGY_H

#include "Strategy.h"

constexpr char alwaysDefID[] = "def";

class AlwaysDefStrategy : public Strategy{
public:
    AlwaysDefStrategy(size_t orderNumber, TChoicesList &history,
                      TScoreMap &scoreMap, TConfigs &configs) : Strategy(orderNumber, history,
                                                                         scoreMap, configs) {};
    ~AlwaysDefStrategy() override = default;

    TChoice getChoice() override;
};

#endif //PRISONER_DILEMMA_ALWAYSDEFSTRATEGY_H
