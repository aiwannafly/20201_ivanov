#ifndef PRISONER_DILEMMA_STRATEGY_H
#define PRISONER_DILEMMA_STRATEGY_H

#include <fstream>
#include <map>
#include <string>
#include <vector>

#include "StrategyTypesAndConstants.h"

class Strategy {
public:
    Strategy() = default;

    Strategy(const Strategy &s) = delete;

    void operator=(const Strategy &s) = delete;

    virtual ~Strategy() = default;

    virtual TChoice getChoice() = 0;

    virtual void setConfigsFileName(const std::string &configsFileName) {
        configsFileName_ = configsFileName;
    }

    void setHistory(const TChoicesList &history) {
        history_ = history;
    }

    void setScoreMap(const TScoreMap &scoreMap) {
        scoreMap_ = scoreMap;
    }

    void setOrderNumber(size_t orderNumber) {
        orderNumber_ = orderNumber;
    }

protected:
    size_t orderNumber_ = 0;
    TChoicesList history_;
    TScoreMap scoreMap_;
    TConfigs configsFileName_;
};

#endif //PRISONER_DILEMMA_STRATEGY_H
