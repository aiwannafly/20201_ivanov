#include "Runner.h"

#include <iostream>
#include <string>
#include <vector>
#include <memory>

#include "Factory.h"
#include "RunnerIO.h"

namespace {
    constexpr char kQuitCommand[] = "quit";
    const TScoreMap kDefaultScoreMap = {
            {{TChoice::COOP, TChoice::COOP, TChoice::COOP}, {7, 7, 7}},
            {{TChoice::COOP, TChoice::COOP, TChoice::DEF},  {3, 3, 9}},
            {{TChoice::COOP, TChoice::DEF,  TChoice::COOP}, {3, 9, 3}},
            {{TChoice::DEF,  TChoice::COOP, TChoice::COOP}, {9, 3, 3}},
            {{TChoice::COOP, TChoice::DEF,  TChoice::DEF},  {0, 5, 5}},
            {{TChoice::DEF,  TChoice::COOP, TChoice::DEF},  {5, 0, 5}},
            {{TChoice::DEF,  TChoice::DEF,  TChoice::COOP}, {5, 5, 0}},
            {{TChoice::DEF,  TChoice::DEF,  TChoice::DEF},  {1, 1, 1}}
    };

    Strategy *getStrategy(const std::string &name) {
        return Factory<Strategy, std::string>::getInstance()
        ->createProduct(name);
    }
}

Runner::Runner(TMode mode, size_t stepsCount, const std::string &configsFileName,
               const std::string &scoreMapFileName, std::vector<std::string> &names) :
        gameMode_(mode), strategyNames_(names), stepsCount_(stepsCount) {
    if (!configsFileName.empty()) {
        configsFileName_ = configsFileName;
    }
    if (!scoreMapFileName.empty()) {
        if (!setScoreMapFromFile(scoreMapFileName)) {
            return;
        }
    }
}

TScoreMap Runner::getDefaultScoreMap() {
    return kDefaultScoreMap;
}

TStatus Runner::getStatus() {
    return status_;
}

void Runner::setMode(TMode mode) {
    status_ = TStatus::OK;
    gameMode_ = mode;
}

void Runner::setStrategies(const std::vector<std::string> &names) {
    status_ = TStatus::OK;
    strategyNames_ = names;
}

void Runner::setStepsCount(size_t stepsCount) {
    status_ = TStatus::OK;
    stepsCount_ = stepsCount;
}

void Runner::setPrintingMode(bool printing) {
    printing_ = printing;
}

bool Runner::setScoreMapFromFile(const std::string &fileName) {
    std::ifstream matrixFile;
    matrixFile.exceptions(std::ifstream::failbit | std::ifstream::badbit);
    try {
        matrixFile.open(fileName);
    } catch (std::ifstream::failure &error) {
        status_ = TStatus::MATRIX_FILE_NOT_OPENED;
        return false;
    }
    try {
        if (!RunnerIO::parseMatrix(matrixFile, &scoreMap_)) {
            status_ = TStatus::WRONG_MATRIX;
            return false;
        }
    } catch (std::ifstream::failure &error) {
        status_ = TStatus::WRONG_MATRIX;
        return false;
    }
    status_ = TStatus::OK;
    return true;
}

bool Runner::setConfigsFromFile(const std::string &fileName) {
    configsFileName_ = fileName;
    status_ = TStatus::OK;
    return true;
}

bool Runner::checkStrategiesCount() {
    if (gameMode_ == TMode::TOURNAMENT) {
        if (strategyNames_.size() < 4) {
            status_ = TStatus::NOT_ENOUGH_STRATEGIES;
            return false;
        }
    } else {
        if (strategyNames_.size() > 3) {
            status_ = TStatus::TOO_MANY_STRATEGIES;
            return false;
        } else if (strategyNames_.size() < 3) {
            status_ = TStatus::NOT_ENOUGH_STRATEGIES;
            return false;
        }
    }
    return true;
}

bool Runner::initStrategies() {
    if (!strategies_.empty()) {
        return false;
    }
    size_t counter = 0;
    for (const auto &name: strategyNames_) {
        strategies_[name] = std::unique_ptr<Strategy>(getStrategy(name));
        if (!strategies_[name]) {
            status_ = TStatus::WRONG_STRATEGY_NAME;
            return false;
        }
        counter++;
    }
    return true;
}

bool Runner::runTournament(std::ostream &stream) {
    if (gameMode_ != TMode::TOURNAMENT) {
        return false;
    }
    std::map<std::string, size_t> results;
    size_t countOfStrats = strategies_.size();
    for (size_t i = 0; i < countOfStrats; i++) {
        for (size_t j = i + 1; j < countOfStrats; j++) {
            for (size_t k = j + 1; k < countOfStrats; k++) {
                gameScores_.clear();
                history_.clear();
                std::string names[] = {strategyNames_[i], strategyNames_[j],
                                       strategyNames_[k]};
                for (size_t step = 0; step < stepsCount_; step++) {
                    size_t orderNum = 0;
                    for (const auto &name: names) {
                        strategies_[name]->setOrderNumber(orderNum);
                        orderNum++;
                        strategies_[name]->setHistory(history_);
                        strategies_[name]->setConfigsFileName(configsFileName_);
                        strategies_[name]->setScoreMap(scoreMap_);
                    }
                    std::array<TChoice, combLen> choices = {};
                    for (size_t idx = 0; idx < 3; idx++) {
                        choices[idx] = strategies_[names[idx]]->getChoice();
                    }
                    history_.push_back(choices);
                    std::array<size_t, combLen> scores = scoreMap_[choices];
                    for (size_t idx = 0; idx < 3; idx++) {
                        gameScores_[names[idx]] += scores[idx];
                    }
                }
                RunnerIO::printGameResults(stream, stepsCount_, strategyNames_, gameScores_,
                                           printing_);
                for (const auto &name : names) {
                    results[name] += gameScores_[name];
                }
            }
        }
    }
    RunnerIO::printTotalResults(stream, strategyNames_, results, printing_);
    return true;
}

bool Runner::runDefaultGame(std::ostream &ostream, std::istream &istream) {
    size_t stepsCount = 0;
    while (true) {
        if (gameMode_ == TMode::DETAILED) {
            std::string command;
            istream >> command;
            if (kQuitCommand == command) {
                break;
            }
        }
        // here ew should set settings for the strategies:
        size_t idx = 0;
        for (const auto &name: strategyNames_) {
            strategies_[name]->setScoreMap(scoreMap_);
            strategies_[name]->setHistory(history_);
            strategies_[name]->setConfigsFileName(configsFileName_);
            strategies_[name]->setOrderNumber(idx);
            idx++;
        }
        stepsCount++;
        std::array<TChoice, combLen> choices = {};
        idx = 0;
        for (const auto &name: strategyNames_) {
            choices[idx] = strategies_[name]->getChoice();
            idx++;
        }
        history_.push_back(choices);
        std::array<size_t, combLen> scores = scoreMap_[choices];
        gameScores_[strategyNames_[0]] += scores[0];
        gameScores_[strategyNames_[1]] += scores[1];
        gameScores_[strategyNames_[2]] += scores[2];
        if (gameMode_ == TMode::DETAILED) {
            RunnerIO::printStepResults(ostream, scores, stepsCount,
                                       strategyNames_, choices, gameScores_,
                                       printing_);
        }
        if (gameMode_ == TMode::FAST && stepsCount == stepsCount_) {
            RunnerIO::printGameResults(ostream, stepsCount_, strategyNames_, gameScores_,
                                       printing_);
            break;
        }
    }
    history_.clear();
    return true;
}

bool Runner::runGame(std::ostream &ostream, std::istream &istream) {
    if (TStatus::OK != getStatus()) {
        return false;
    }
    if (ostream.fail()) {
        status_ = TStatus::OUTPUT_STREAM_FAILURE;
        return false;
    }
    strategies_.clear();
    bool status = initStrategies();
    if (!status) {
        return false;
    }
    status = checkStrategiesCount();
    if (!status) {
        return false;
    }
    if (gameMode_ == TMode::TOURNAMENT) {
        return runTournament(ostream);
    } else {
        return runDefaultGame(ostream, istream);
    }
}
