#ifndef PRISONER_DILEMMA_RUNNER_H
#define PRISONER_DILEMMA_RUNNER_H

#include <fstream>
#include <iostream>
#include <map>
#include <memory>
#include <string>
#include <vector>

#include "Strategy.h"
#include "RunnerTypesAndConstants.h"

class Runner {
public:
    Runner(TMode mode, size_t stepsCount, const std::string &configsFileName,
           const std::string &scoreMapFileName, std::vector<std::string> &names);

    Runner() = default;

    bool runGame(std::ostream &ostream);

    TStatus getStatus();

    void setMode(TMode mode);

    void setStrategies(const std::vector<std::string> &names);

    void setStepsCount(size_t stepsCount);

    bool setScoreMapFromFile(const std::string &fileName);

    bool setConfigsFromFile(const std::string &fileName);

    static void printErrorMessage(std::ostream &stream, TStatus status);

    void setPrintingMode(bool printing);

private:
    TStatus status_ = OK;
    bool printing_ = true;
    TMode mode_ = DETAILED;
    std::vector<std::string> strategyNames_;
    std::map<std::string, std::unique_ptr<Strategy>> strategies_;
    std::map<std::string, size_t> gameScores_;
    size_t strategiesCount_ = 0;
    size_t stepsCount_ = 10;
    TConfigs configs_;
    TChoiceMatrix history_;
    TScoreMap scoreMap_ = getDefaultScoreMap();

    bool initStrategies();

    bool runTournament(std::ostream &stream);

    bool runDefaultGame(std::ostream &stream);

    bool checkStrategiesCount();

    static TScoreMap getDefaultScoreMap();
};

#endif
