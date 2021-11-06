#ifndef PRISONER_DILEMMA_RUNNER_H
#define PRISONER_DILEMMA_RUNNER_H

#include <fstream>
#include <iostream>
#include <map>
#include <memory>
#include <string>
#include <vector>

#include "Strategy.h"

typedef enum TStatus {
    OK, WRONG_MODE, WRONG_STEPS, WRONG_CONFIGS,
    MATRIX_FILE_NOT_OPENED, WRONG_MATRIX, NOT_ENOUGH_STRATS,
    WRONG_STRATEGY_NAME, TOO_MANY_STRATS, CONFIGS_FILE_NOT_OPENED
} TStatus;

typedef enum TMode {
    DETAILED, FAST, TOURNAMENT
} TMode;

class Runner {
public:
    explicit Runner(const std::vector<std::string> &params);

    Runner();

    TStatus getStatus();

    bool runGame(std::ostream &stream);

    void printErrorMessage(std::ostream &stream);

    void setMode(TMode mode);

    bool setStrategies(const std::vector<std::string> &strategyNames);

    void setStepsCount(size_t stepsCount);

    bool setScoreMap(std::ifstream &matrixFile);

    void setConfigs(TConfigs &configs);

private:
    TMode mode_;
    std::vector<std::string> names_;
    std::map<std::string, std::unique_ptr<Strategy>> strategies_;
    std::map<std::string, size_t> gameScores_;
    size_t strategiesCount_ = 0;
    size_t stepsCount_;
    TScoreMap scoreMap_;
    TConfigs configs_;
    TChoiceMatrix choiceMatrix_;
    TStatus status_;
    std::map<TStatus, std::string> errorMessages_{
            {OK,                     "Everything is alright"},
            {WRONG_MODE,             "Such mode does not exist"},
            {WRONG_STEPS,            "Wrong steps value, it's not a number"},
            {WRONG_CONFIGS,          "Wrong configs format"},
            {MATRIX_FILE_NOT_OPENED, "File with matrix could not be opened"},
            {WRONG_MATRIX,           "Score matrix has a wrong format"},
            {NOT_ENOUGH_STRATS,      "Too few strategies for the chosen mode"},
            {WRONG_STRATEGY_NAME,    "Strategy with the entered name does not exist"},
            {TOO_MANY_STRATS,        "Too many strategies for the chosen mode"}
    };

    bool parseMatrix(std::ifstream &matrixFile);

    bool runTournament(std::ostream &stream);

    void printStepResults(std::ostream &stream, std::array<size_t, combLen> results, size_t stepNumber);

    void printGameResults(std::ostream &stream);

    void printTotalResults(std::ostream &stream);
};

#endif
