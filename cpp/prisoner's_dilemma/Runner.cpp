#include "Runner.h"

#include <iostream>
#include <string>
#include <vector>
#include <memory>

#include "Factory.h"
#include "Factory.cpp"
#include "RunnerIO.h"

namespace {
    constexpr char kQuitCommand[] = "quit";
    const TScoreMap kDefaultScoreMap = {
            {{COOP, COOP, COOP}, {7, 7, 7}},
            {{COOP, COOP, DEF},  {3, 3, 9}},
            {{COOP, DEF,  COOP}, {3, 9, 3}},
            {{DEF,  COOP, COOP}, {9, 3, 3}},
            {{COOP, DEF,  DEF},  {0, 5, 5}},
            {{DEF,  COOP, DEF},  {5, 0, 5}},
            {{DEF,  DEF,  COOP}, {5, 5, 0}},
            {{DEF,  DEF,  DEF},  {1, 1, 1}}
    };

    Strategy *getStrategy(const std::string &name, size_t counter, TChoiceMatrix &history,
                          TScoreMap &scoreMap, TConfigs &configs) {
        return Factory<Strategy, std::string, size_t, TChoiceMatrix &, TScoreMap &, TConfigs &>
        ::getInstance()->createProduct(name, counter, history, scoreMap, configs);
    }
}

Runner::Runner(TMode mode, size_t stepsCount, const std::string &configsFileName,
               const std::string &scoreMapFileName, std::vector<std::string> &names) :
        mode_(mode), strategyNames_(names), stepsCount_(stepsCount) {
    if (!configsFileName.empty()) {
        if (!setConfigsFromFile(configsFileName)) {
            return;
        }
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

void Runner::setMode(TMode mode) {
    mode_ = mode;
}

void Runner::setStrategies(const std::vector<std::string> &names) {
    strategyNames_ = names;
}

void Runner::setStepsCount(size_t stepsCount) {
    stepsCount_ = stepsCount;
}

void Runner::setPrintingMode(bool printing) {
    printing_ = printing;
}

TStatus Runner::getStatus() {
    return status_;
}

void Runner::printErrorMessage(std::ostream &stream, TStatus status) {
    RunnerIO::printErrorMessage(stream, status);
}

bool Runner::setScoreMapFromFile(const std::string &fileName) {
    std::ifstream matrixFile;
    matrixFile.exceptions(std::ifstream::failbit | std::ifstream::badbit);
    try {
        matrixFile.open(fileName);
    } catch (std::ifstream::failure &error) {
        status_ = MATRIX_FILE_NOT_OPENED;
        return false;
    }
    try {
        if (!RunnerIO::parseMatrix(matrixFile, &scoreMap_)) {
            status_ = WRONG_MATRIX;
            return false;
        }
    } catch (std::ifstream::failure &error) {
        status_ = WRONG_MATRIX;
        return false;
    }
    return true;
}

bool Runner::setConfigsFromFile(const std::string &fileName) {
    std::ifstream configsFile;
    configsFile.exceptions(std::ifstream::failbit | std::ifstream::badbit);
    try {
        configsFile.open(fileName);
    } catch (std::ifstream::failure &error) {
        status_ = CONFIGS_FILE_NOT_OPENED;
        return false;
    }
    while (true) {
        try {
            std::string word;
            configsFile >> word;
            configs_.push_back(word);
        } catch (std::ifstream::failure &e) {
            break;
        }
    }
    return true;
}

bool Runner::checkStrategiesCount() {
    if (mode_ == TOURNAMENT) {
        if (strategyNames_.size() < 4) {
            status_ = NOT_ENOUGH_STRATEGIES;
            return false;
        }
    } else {
        if (strategyNames_.size() > 3) {
            status_ = TOO_MANY_STRATEGIES;
            return false;
        } else if (strategyNames_.size() < 3) {
            status_ = NOT_ENOUGH_STRATEGIES;
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
        strategies_[name] = std::unique_ptr<Strategy>(getStrategy
                (name, counter, history_, scoreMap_, configs_));
        if (!strategies_[name]) {
            status_ = WRONG_STRATEGY_NAME;
            return false;
        }
        counter++;
    }
    return true;
}

bool Runner::runTournament(std::ostream &stream) {
    if (mode_ != TOURNAMENT) {
        return false;
    }
    std::map<std::string, size_t> results;
    for (size_t i = 0; i < strategiesCount_; i++) {
        for (size_t j = i + 1; j < strategiesCount_; j++) {
            for (size_t k = j + 1; k < strategiesCount_; k++) {
                gameScores_ = {};
                for (size_t step = 0; step < stepsCount_; step++) {
                    std::array<TChoice, combLen> choices = {};
                    choices[0] = strategies_[strategyNames_[i]]->getChoice();
                    choices[1] = strategies_[strategyNames_[j]]->getChoice();
                    choices[2] = strategies_[strategyNames_[k]]->getChoice();
                    history_.push_back(choices);
                    std::array<size_t, combLen> scores = scoreMap_[choices];
                    gameScores_[strategyNames_[i]] += scores[0];
                    gameScores_[strategyNames_[j]] += scores[1];
                    gameScores_[strategyNames_[k]] += scores[2];
                }
                RunnerIO::printGameResults(stream, stepsCount_, strategyNames_, gameScores_,
                                           printing_);
                results[strategyNames_[i]] += gameScores_[strategyNames_[i]];
                results[strategyNames_[j]] += gameScores_[strategyNames_[j]];
                results[strategyNames_[k]] += gameScores_[strategyNames_[k]];
                history_.clear();
                gameScores_.clear();
            }
        }
    }
    RunnerIO::printTotalResults(stream, strategyNames_, results, printing_);
    return true;
}

bool Runner::runDefaultGame(std::ostream &stream) {
    size_t stepsCount = 0;
    while (true) {
        if (mode_ == DETAILED) {
            std::string command;
            std::cin >> command;
            if (kQuitCommand == command) {
                break;
            }
        }
        stepsCount++;
        std::array<TChoice, combLen> choices = {};
        size_t ind = 0;
        for (const auto &name: strategyNames_) {
            choices[ind] = strategies_[name]->getChoice();
            ind++;
        }
        history_.push_back(choices);
        std::array<size_t, combLen> scores = scoreMap_[choices];
        gameScores_[strategyNames_[0]] += scores[0];
        gameScores_[strategyNames_[1]] += scores[1];
        gameScores_[strategyNames_[2]] += scores[2];
        if (mode_ == DETAILED) {
            RunnerIO::printStepResults(stream, scores, stepsCount,
                                       strategyNames_, choices, gameScores_,
                                       printing_);
        }
        if (mode_ == FAST && stepsCount == stepsCount_) {
            RunnerIO::printGameResults(stream, stepsCount_, strategyNames_, gameScores_,
                                       printing_);
            break;
        }
    }
    history_.clear();
    return true;
}

bool Runner::runGame(std::ostream &ostream) {
    if (OK != getStatus()) {
        return false;
    }
    if (ostream.fail()) {
        status_ = OUTPUT_STREAM_FAILURE;
        return false;
    }
    bool status = checkStrategiesCount();
    if (!status) {
        return false;
    }
    if (strategies_.empty()) {
        status = initStrategies();
        if (!status) {
            return false;
        }
    }
    if (mode_ == TOURNAMENT) {
        return runTournament(ostream);
    } else {
        return runDefaultGame(ostream);
    }
}
