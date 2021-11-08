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
    OK, WRONG_MODE, WRONG_STEPS,
    MATRIX_FILE_NOT_OPENED, WRONG_MATRIX, NOT_ENOUGH_STRATS,
    WRONG_STRATEGY_NAME, TOO_MANY_STRATS, CONFIGS_FILE_NOT_OPENED
} TStatus;

typedef enum TMode {
    DETAILED, FAST, TOURNAMENT
} TMode;

class Runner {
public:
    explicit Runner(const std::vector<std::string> &params);

    Runner() = default;

    TStatus getStatus();

    bool runGame(std::ostream &ostream);

    void printErrorMessage(std::ostream &stream);

    bool setMode(TMode mode);

    bool setStrategies(const std::vector<std::string> &names);

    void setStepsCount(size_t stepsCount);

    bool setScoreMap(const std::string &fileName);

    bool setConfigs(const std::string &fileName);

    std::map<std::string, size_t> getGameScores();

    void disablePrinting();

private:
    std::array<std::string, 6> availableStrategies_ = {
            "random", "coop", "def", "freq", "pred", "meta"
    };
    TStatus status_ = OK;
    bool printMode_ = true;
    TMode mode_ = DETAILED;
    std::vector<std::string> names_;
    std::map<std::string, std::unique_ptr<Strategy>> strategies_;
    std::map<std::string, size_t> gameScores_;
    size_t strategiesCount_ = 0;
    size_t stepsCount_ = 10;
    TConfigs configs_;
    TChoiceMatrix choiceMatrix_;
    TScoreMap scoreMap_ = {
            {{COOP, COOP, COOP}, {7, 7, 7}},
            {{COOP, COOP, DEF},  {3, 3, 9}},
            {{COOP, DEF,  COOP}, {3, 9, 3}},
            {{DEF,  COOP, COOP}, {9, 3, 3}},
            {{COOP, DEF,  DEF},  {0, 5, 5}},
            {{DEF,  COOP, DEF},  {5, 0, 5}},
            {{DEF,  DEF,  COOP}, {5, 5, 0}},
            {{DEF,  DEF,  DEF},  {1, 1, 1}}
    };
    std::map<TStatus, std::string> errorMessages_{
            {OK,                     "Everything is alright"},
            {WRONG_MODE,             "Such mode does not exist"},
            {WRONG_STEPS,            "Wrong steps value, it's not a number"},
            {MATRIX_FILE_NOT_OPENED, "File with matrix could not be opened"},
            {WRONG_MATRIX,           "Score matrix has a wrong format"},
            {NOT_ENOUGH_STRATS,      "Too few strategies for the chosen mode"},
            {WRONG_STRATEGY_NAME,    "Strategy with the entered name does not exist"},
            {TOO_MANY_STRATS,        "Too many strategies for the chosen mode"}
    };
    std::map<std::string, TMode> modeMap_{
            {"detailed",   DETAILED},
            {"fast",       FAST},
            {"tournament", TOURNAMENT}
    };

    void initStrategies();

    bool parseMatrix(std::ifstream &matrixFile);

    bool runTournament(std::ostream &stream);

    void printStepResults(std::ostream &stream, std::array<size_t, combLen> results, size_t stepNumber);

    void printGameResults(std::ostream &stream);

    void printTotalResults(std::ostream &stream);

    bool checkStrategiesCount();
};

#endif
