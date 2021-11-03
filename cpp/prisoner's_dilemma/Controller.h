#ifndef PRISONER_DILEMMA_CONTROLLER_H
#define PRISONER_DILEMMA_CONTROLLER_H

#include <fstream>
#include <map>
#include <string>
#include <vector>

#include "RandomStrategy.h"
#include "AlwaysCoopStrategy.h"
#include "AlwaysDefStrategy.h"
#include "MostFreqStrategy.h"


typedef enum TStatus {
    OK, WRONG_MODE, WRONG_STEPS, WRONG_CONFIGS,
    MATRIX_FILE_NOT_OPENED, WRONG_MATRIX, NOT_ENOUGH_STRATS,
    WRONG_STRATEGY_NAME
} TStatus;

typedef enum TMode {
    DETAILED, FAST, TOURNAMENT
} TMode;

typedef struct TConfigs {
} TConfigs;


class Controller {
public:
    Controller(int argc, char *argv[]);

    TStatus getStatus();

    bool runGame();

private:
    TMode mode_;
    std::vector<std::string> strategyNames_;
    size_t stepsCount_;
    TScoreMap scoreMap_;
    TChoiceMatrix choiceMatrix_;
    TConfigs configs_;
    TStatus status_;

    bool ParseMatrix(const std::ifstream &matrixFile);
};

#endif
