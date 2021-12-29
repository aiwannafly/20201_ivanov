#ifndef PRISONER_DILEMMA_STRATEGY_H
#define PRISONER_DILEMMA_STRATEGY_H

#include <fstream>
#include <map>
#include <string>
#include <vector>

#include "StrategyTypesAndConstants.h"

class Strategy {
public:
    Strategy(size_t orderNumber, TChoicesList &history,
             TScoreMap &scoreMap, TConfigs &configs)
            : orderNumber_(orderNumber), history_(history),
              scoreMap_(scoreMap), configsFileName_(configs) {};

    virtual ~Strategy() = default;

    virtual TChoice getChoice() = 0;

protected:
    size_t orderNumber_ = 0;
    TChoicesList history_;
    TScoreMap scoreMap_;
    TConfigs configsFileName_;
};

#endif //PRISONER_DILEMMA_STRATEGY_H
