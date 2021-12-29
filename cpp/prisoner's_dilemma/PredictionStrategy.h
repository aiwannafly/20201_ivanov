#ifndef PRISONER_DILEMMA_PREDICTIONSTRATEGY_H
#define PRISONER_DILEMMA_PREDICTIONSTRATEGY_H

#include "Strategy.h"

constexpr char predictionID[] = "pred";

class PredictionStrategy : public Strategy {
public:
    PredictionStrategy(size_t orderNumber, TChoicesList &history,
                       TScoreMap &scoreMap, TConfigs &configs) :
            Strategy(orderNumber, history,scoreMap, configs) {};

    ~PredictionStrategy() override = default;

    TChoice getChoice() override;
};



#endif //PRISONER_DILEMMA_PREDICTIONSTRATEGY_H
