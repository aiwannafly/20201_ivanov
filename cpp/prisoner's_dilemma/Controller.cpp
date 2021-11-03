#include "Controller.h"

#include <cassert>
#include <iostream>
#include <stdexcept>
#include <string>
#include <vector>
#include <memory>

#include "Factory.h"
#include "Factory.cpp"

constexpr size_t combinationsCount = 8;
constexpr char quitCommand[] = "quit";
constexpr size_t defaultStepsCount = 10;
constexpr char defaultMatrixFileName[] = "default_matrix.txt";

namespace {
    bool startsWith(const std::string &string, const std::string &subString) {
        if (string.size() < subString.size()) {
            return false;
        }
        for (size_t i = 0; i < subString.size(); i++) {
            if (subString[i] != string[i]) {
                return false;
            }
        }
        return true;
    }
}

Controller::Controller(int argc, char *argv[]) {
    status_ = OK;
    mode_ = DETAILED;
    stepsCount_ = defaultStepsCount;
    std::string modeKey = "--mode=";
    std::string stepsKey = "--steps=";
    std::string configsKey = "--configs_=";
    std::string matrixKey = "--matrix=";
    size_t stratCounter = 0;
    for (size_t i = 1; i < argc; i++) {
        std::string current = argv[i];
        if (startsWith(current, modeKey)) {
            if (current == modeKey + "detailed") {
                mode_ = DETAILED;
            } else if (current == modeKey + "fast") {
                mode_ = FAST;
            } else if (current == modeKey + "tournament") {
                mode_ = TOURNAMENT;
            } else {
                status_ = WRONG_MODE;
                return;
            }
        } else if (startsWith(current, stepsKey)) {
            try {
                stepsCount_ = std::stol(current.substr(stepsKey.length()));
            } catch (std::invalid_argument &e) {
                status_ = WRONG_STEPS;
                return;
            }
        } else if (startsWith(current, configsKey)) {
        } else if (startsWith(current, matrixKey)) {
            std::ifstream matrixFile = std::ifstream(current.substr(matrixKey.length()));
            if (!matrixFile.is_open()) {
                status_ = MATRIX_FILE_NOT_OPENED;
                return;
            }
            if (!parseMatrix(matrixFile)) {
                status_ = WRONG_MATRIX;
                return;
            }
        } else {
            Strategy* strategy = Factory<Strategy, std::string, size_t, TChoiceMatrix&,
                            TScoreMap&>::getInstance()->createProduct(
                            current, stratCounter, choiceMatrix_, scoreMap_);
            if (nullptr == strategy) {
                status_ = WRONG_STRATEGY_NAME;
                return;
            }
            strategyNames_.push_back(current);
            strategies_.push_back(std::unique_ptr<Strategy>(strategy));
            stratCounter++;
        }
    }
    if (strategyNames_.size() < 3) {
        status_ = NOT_ENOUGH_STRATS;
    }
    if (mode_ == TOURNAMENT &&
        strategyNames_.size() < 4) {
        status_ = NOT_ENOUGH_STRATS;
    }
    if (scoreMap_.empty()) {
        std::ifstream matrixFile = std::ifstream(defaultMatrixFileName);
        if (!matrixFile.is_open()) {
            status_ = MATRIX_FILE_NOT_OPENED;
            return;
        }
        if (!parseMatrix(matrixFile)) {
            status_ = WRONG_MATRIX;
            return;
        }
    }
}

bool Controller::parseMatrix(std::ifstream &matrixFile) {
    for (size_t i = 0; i < combinationsCount; i++) {
        std::array<TChoice, combLen> combination = {};
        for (size_t j = 0; j < combLen; j++) {
            std::string word;
            matrixFile >> word;
            if (word == "C" || word == "С") {
                combination[j] = COOPERATE;
            } else if (word[0] == 'D') {
                combination[j] = DEFEND;
            } else {
                return false;
            }
        }
        std::array<size_t, combLen> scores = {};
        for (size_t j = 0; j < combLen; j++) {
            std::string word;
            matrixFile >> word;
            try {
                scores[j] = std::stol(word);
            } catch (std::invalid_argument &e) {
                return false;
            }
        }
        scoreMap_[combination] = scores;
    }
    return true;
}

TStatus Controller::getStatus() {
    return status_;
}

bool Controller::runTournament() {
    assert(mode_ == TOURNAMENT);
    size_t countOfStrategies = strategyNames_.size();
    std::vector<size_t> totalScores;
    for (size_t i = 0; i < countOfStrategies; i++) {
        totalScores.push_back(0);
    }
    for (size_t i = 0; i < countOfStrategies; i++) {
        for (size_t j = i + 1; j < countOfStrategies; j++) {
            for (size_t k = j + 1; k < countOfStrategies; k++) {
                std::array<size_t, combLen> gameScores = {0};
                for (size_t step = 0; step < stepsCount_; step++) {
                    std::array<TChoice, combLen> choices = {};
                    choices[0] = strategies_[i]->getChoice();
                    choices[1] = strategies_[j]->getChoice();
                    choices[2] = strategies_[k]->getChoice();
                    choiceMatrix_.push_back(choices);
                    std::array<size_t, combLen> scores = scoreMap_[choices];
                    for (size_t ind = 0; ind < combLen; ind++) {
                        gameScores[ind] += scores[ind];
                    }
                }
                printGameResults(gameScores, strategyNames_[i], strategyNames_[j],
                                 strategyNames_[k], stepsCount_);
                totalScores[i] += gameScores[0];
                totalScores[j] += gameScores[1];
                totalScores[k] += gameScores[2];
                choiceMatrix_.clear();
            }
        }
    }
    printTotalResults(totalScores, strategyNames_);
    return true;
}

bool Controller::runGame() {
    if (OK != getStatus()) {
        return false;
    }
    if (mode_ == TOURNAMENT) {
        return runTournament();
    }
    std::array<size_t, combLen> totalScores = {0};
    size_t stepsCount = 0;
    while (true) {
        if (mode_ == DETAILED) {
            std::string command;
            std::cin >> command;
            if (quitCommand == command) {
                break;
            }
        }
        stepsCount++;
        std::array<TChoice, combLen> choices = {};
        for (size_t i = 0; i < combLen; i++)  {
            choices[i] = strategies_[i]->getChoice();
        }
        choiceMatrix_.push_back(choices);
        std::array<size_t, combLen> scores = scoreMap_[choices];
        for (size_t i = 0; i < combLen; i++) {
            totalScores[i] += scores[i];
        }
        if (mode_ == DETAILED) {
            printStepResults(choices, scores, totalScores, stepsCount);
        }
        if (mode_ == FAST && stepsCount == stepsCount_) {
            printGameResults(totalScores, strategyNames_[0],
                             strategyNames_[1], strategyNames_[2], stepsCount);
            break;
        }
    }
    choiceMatrix_.clear();
    return true;
}

void Controller::printStepResults(std::array<TChoice, combLen> choices, std::array<size_t, combLen> results,
                                  std::array<size_t, combLen> totalResults, size_t stepNumber) {
    std::map<TChoice, std::string> choiceMap;
    choiceMap[COOPERATE] = "C";
    choiceMap[DEFEND] = "D";
    std::cout << "=================== ROUND №" <<
              stepNumber << " ==============" << std::endl;
    std::cout << "    NAMES    |";
    for (size_t i = 0; i < combLen; i++) {
        std::cout << "\t" << strategyNames_[i];
    }
    std::cout << std::endl;
    std::cout << "   CHOICES   |";
    for (size_t i = 0; i < combLen; i++) {
        std::cout << "\t" << choiceMap[choices[i]];
    }
    std::cout << std::endl;
    std::cout << "ROUND SCORES |";
    for (size_t i = 0; i < combLen; i++) {
        std::cout << "\t" << results[i];
    }
    std::cout << std::endl;
    std::cout << "TOTAL SCORES |";
    for (size_t i = 0; i < combLen; i++) {
        std::cout << "\t" << totalResults[i];
    }
    std::cout << std::endl;
    std::cout << "===========================================" << std::endl;
}

void Controller::printGameResults(std::array<size_t, combLen> gameResults,
                      const std::string &firstStrName, const std::string &secStrName,
                      const std::string &thirdStrName, size_t stepsCount) {
    std::cout << "=================== GAME RESULTS ==============" << std::endl;
    std::cout << "STEPS COUNT" << "\t|\t" << stepsCount << std::endl;
    std::cout << "-----------------------------------------------" << std::endl;
    std::cout << firstStrName << "\t|\t" << gameResults[0] << std::endl;
    std::cout << secStrName << "\t|\t" << gameResults[1] << std::endl;
    std::cout << thirdStrName << "\t|\t" << gameResults[2] << std::endl;
    std::cout << "===========================================" << std::endl;
}

void Controller::printTotalResults(std::vector<size_t> totalScores, std::vector<std::string> strategyNames) {
    assert(totalScores.size() == strategyNames.size());
    std::cout << "================== TOTAL RESULTS ==============" << std::endl;
    size_t indexOfWinner = 0;
    for (size_t i = 1; i < totalScores.size(); i++) {
        if (totalScores[i] > totalScores[indexOfWinner]) {
            indexOfWinner = i;
        }
    }
    std::cout << strategyNames[indexOfWinner] << "\t|\t" << totalScores[indexOfWinner]
              << "\t\t<--- WINNER" << std::endl;
    for (size_t i = 0; i < totalScores.size(); i++) {
        if (i != indexOfWinner) {
            std::cout << strategyNames[i] << "\t|\t" << totalScores[i] << std::endl;
        }
    }
    std::cout << "===========================================" << std::endl;
}
