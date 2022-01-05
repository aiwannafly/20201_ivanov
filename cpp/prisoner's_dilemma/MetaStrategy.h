#ifndef PRISONER_DILEMMA_METASTRATEGY_H
#define PRISONER_DILEMMA_METASTRATEGY_H

#include "Strategy.h"

constexpr char metaID[] = "meta";

class MetaStrategy : public Strategy {
public:
    MetaStrategy() = default;

    ~MetaStrategy() override = default;

    TChoice getChoice() override;

    void setConfigsFileName(const std::string &configsFileName) override;

private:
    size_t strategiesCounter_ = 0;
    std::vector<std::string> strategiesNames_;
};

#endif //PRISONER_DILEMMA_METASTRATEGY_H
