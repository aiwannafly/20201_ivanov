#ifndef PRISONER_DILEMMA_MOSTFREQSTRATEGY_H
#define PRISONER_DILEMMA_MOSTFREQSTRATEGY_H

#include "Strategy.h"

constexpr char mostFreqID[] = "freq";

class MostFreqStrategy : public Strategy {
public:
    MostFreqStrategy(size_t orderNumber, TChoiceMatrix &history,
                     TScoreMap &scoreMap) : Strategy(orderNumber, history,
            scoreMap) {};

    ~MostFreqStrategy() override = default;

    TChoice getChoice() override;
};


#endif //PRISONER_DILEMMA_MOSTFREQSTRATEGY_H
