#ifndef PRISONER_DILEMMA_MOSTFREQSTRATEGY_H
#define PRISONER_DILEMMA_MOSTFREQSTRATEGY_H

#include "Strategy.h"

constexpr char mostFreqID[] = "freq";

class MostFreqStrategy : public Strategy {
public:
    MostFreqStrategy() = default;

    ~MostFreqStrategy() override = default;

    TChoice getChoice() override;
};

#endif //PRISONER_DILEMMA_MOSTFREQSTRATEGY_H
