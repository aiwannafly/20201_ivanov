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

/*
 * This class runs Prisoner's Dilemma game: https://en.wikipedia.org/wiki/Prisoner%27s_dilemma
 * It supports the opportunity to add your own strategies, that are derived from
 * Strategy class and implements their own ways to play.
 * The game has 3 modes:
 *      1. "Default". The game will be running step by step; move to the next step
 *      happens after pressing any key + Enter. The command "quit" stops the game.
 *      2. "Fast". The game will be proceeded immediately, then the results will
 *      have been shown in the output stream.
 *      3. "Tournament". Previous modes were made for the competing of just 3 strategies.
 *      This mode is made for competing of more than 3 strategies, the game will
 *      be also proceeded immediately, then the total scores of all strategies will have
 *      be shown in the output stream.
 * You may set the count of steps for the "Tournament" and "Fast" modes by the
 * $stepsCount argument in a constructor.
 * It's also allowed to set your own score matrix for the game by setting
 * $scoreMapFileName, but the field is not necessary, the game may use a default matrix.
 * Configs file name may be used by some strategies to set their special characteristics.
 * You should strictly follow the syntax in the configs file:
 * strategy_name args ... end
 * EXAMPLE: meta pred def pred end
 */
class Runner {
public:
    Runner(TMode mode, size_t stepsCount, const std::string &configsFileName,
           const std::string &scoreMapFileName, std::vector<std::string> &names);

    Runner() = default;

    Runner(const Runner &) = delete;

    void operator=(const Runner &) = delete;

    bool runGame(std::ostream &ostream = std::cout, std::istream &istream = std::cin);

    TStatus getStatus();

    void setMode(TMode mode);

    void setStrategies(const std::vector<std::string> &names);

    void setStepsCount(size_t stepsCount);

    bool setScoreMapFromFile(const std::string &fileName);

    bool setConfigsFromFile(const std::string &fileName);

    void setPrintingMode(bool printing);

private:
    TStatus status_ = TStatus::OK;
    bool printing_ = true;
    TMode gameMode_ = TMode::DETAILED;
    std::vector<std::string> strategyNames_;
    std::map<std::string, std::unique_ptr<Strategy>> strategies_;
    std::map<std::string, size_t> gameScores_;
    size_t stepsCount_ = 10;
    std::string configsFileName_;
    TChoicesList history_;
    TScoreMap scoreMap_ = getDefaultScoreMap();

    bool initStrategies();

    bool runTournament(std::ostream &stream);

    bool runDefaultGame(std::ostream &ostream, std::istream &istream = std::cin);

    bool checkStrategiesCount();

    static TScoreMap getDefaultScoreMap();
};

#endif
