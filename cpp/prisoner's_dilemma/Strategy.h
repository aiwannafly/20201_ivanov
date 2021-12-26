#ifndef PRISONER_DILEMMA_STRATEGY_H
#define PRISONER_DILEMMA_STRATEGY_H

#include <map>
#include <string>
#include <vector>

#include "StrategyTypesAndConstants.h"

class Strategy {
public:
    Strategy(size_t orderNumber, TChoiceMatrix &history,
             TScoreMap &scoreMap, TConfigs &configs)
            : orderNumber_(orderNumber), history_(history),
              scoreMap_(scoreMap), configs_(configs) {};

    virtual ~Strategy() = default;

    virtual TChoice getChoice() = 0;

    TChoiceMatrix getHistory() const;

    TScoreMap getScoreMap() const;

    TConfigs getConfigs() const;

    size_t getOrderNumber() const;

private:
    size_t orderNumber_ = 0;
    TChoiceMatrix history_;
    TScoreMap scoreMap_; //protected field
    TConfigs configs_;
};

#endif //PRISONER_DILEMMA_STRATEGY_H
