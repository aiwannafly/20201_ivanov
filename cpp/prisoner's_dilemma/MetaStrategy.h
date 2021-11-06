#ifndef PRISONER_DILEMMA_METASTRATEGY_H
#define PRISONER_DILEMMA_METASTRATEGY_H

#include "Strategy.h"

constexpr char metaID[] = "meta";

class MetaStrategy : public Strategy {
public:
    MetaStrategy(size_t orderNumber, TChoiceMatrix &history,
                       TScoreMap &scoreMap, TConfigs &configs) : Strategy(orderNumber, history,
                                                                          scoreMap, configs) {}

    ~MetaStrategy() override = default;

    TChoice getChoice() override;

private:
    size_t strategiesCounter_ = 0;
};

#endif //PRISONER_DILEMMA_METASTRATEGY_H
