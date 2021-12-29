#ifndef PRISONER_DILEMMA_METASTRATEGY_H
#define PRISONER_DILEMMA_METASTRATEGY_H

#include "Strategy.h"

constexpr char metaID[] = "meta";

class MetaStrategy : public Strategy {
public:
    MetaStrategy(size_t orderNumber, TChoicesList &history,
                 TScoreMap &scoreMap, TConfigs &configsFileName);

    ~MetaStrategy() override = default;

    TChoice getChoice() override;

private:
    size_t strategiesCounter_ = 0;
    std::vector<std::string> strategiesNames_;
};

#endif //PRISONER_DILEMMA_METASTRATEGY_H
