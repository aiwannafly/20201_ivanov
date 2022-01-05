#ifndef PRISONER_DILEMMA_ALWAYSCOOPSTRATEGY_H
#define PRISONER_DILEMMA_ALWAYSCOOPSTRATEGY_H

#include "Strategy.h"

class AlwaysCoopStrategy : public Strategy {
public:
    AlwaysCoopStrategy() = default;

    ~AlwaysCoopStrategy() override = default;

    TChoice getChoice() override;
};

#endif //PRISONER_DILEMMA_ALWAYSCOOPSTRATEGY_H
