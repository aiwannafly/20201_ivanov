#ifndef PRISONER_DILEMMA_PREDICTIONSTRATEGY_H
#define PRISONER_DILEMMA_PREDICTIONSTRATEGY_H

#include "Strategy.h"

constexpr char predictionID[] = "pred";

class PredictionStrategy : public Strategy {
public:
    PredictionStrategy() = default;

    ~PredictionStrategy() override = default;

    TChoice getChoice() override;
};



#endif //PRISONER_DILEMMA_PREDICTIONSTRATEGY_H
