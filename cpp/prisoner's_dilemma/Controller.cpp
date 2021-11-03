#include "Controller.h"

#include <iostream>
#include <stdexcept>
#include <string>
#include <vector>
#include <memory>

#include "Factory.h"

constexpr size_t combinationsCount = 8;
constexpr size_t strategiesCount = 4;
constexpr const char* strategiesNames[] = {randomID, mostFreqID, alwaysCoopID, alwaysDefID};

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

    void printStepResults(std::array<TChoice, combLen> choices, std::array<size_t, combLen> results,
                          std::array<size_t, combLen> totalResults, size_t stepNumber,
                          std::vector<std::string> strategyNames) {
        std::map<TChoice, std::string> choiceMap;
        choiceMap[COOPERATE] = "cooperate";
        choiceMap[DEFEND] = "defend";
        std::cout << "ROUND №" << stepNumber << std::endl;
        std::cout << "NAMES: ";
        for (size_t i = 0; i < combLen; i++) {
            std::cout << strategyNames[i] << "\t";
        }
        std::cout << std::endl;
        std::cout << "CHOICES: ";
        for (size_t i = 0; i < combLen; i++) {
            std::cout << choiceMap[choices[i]] << "\t";
        }
        std::cout << std::endl;
        std::cout << "ROUND SCORES: ";
        for (size_t i = 0; i < combLen; i++) {
            std::cout << results[i] << "\t";
        }
        std::cout << std::endl;
        std::cout << "TOTAL SCORES: ";
        for (size_t i = 0; i < combLen; i++) {
            std::cout << totalResults[i] << "\t";
        }
        std::cout << std::endl;
    }
}

Controller::Controller(int argc, char *argv[]) {
    status_ = OK;
    mode_ = DETAILED;
    stepsCount_ = 0;
    std::string modeKey = "--mode=";
    std::string stepsKey = "--steps=";
    std::string configsKey = "--configs_=";
    std::string matrixKey = "--matrix=";
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
//             = std::ifstream(current.substr(configsKey.length()));
//            if(!configs_.configsFile.is_open()) {
//                status_ = WRONG_CONFIGS;
//                return;
//            }
        } else if (startsWith(current, matrixKey)) {
            std::ifstream matrixFile = std::ifstream(current.substr(matrixKey.length()));
            if (!matrixFile.is_open()) {
                status_ = MATRIX_FILE_NOT_OPENED;
                return;
            }
            if (!ParseMatrix(matrixFile)) {
                status_ = WRONG_MATRIX;
                return;
            }
        } else {
            bool inNames = false;
            for (auto strategiesName: strategiesNames) {
                if (current == strategiesName) {
                    inNames = true;
                    break;
                }
            }
            if (inNames) {
                strategyNames_.push_back(current);
            } else {
                status_ = WRONG_STRATEGY_NAME;
                return;
            }
        }
    }
    if (strategyNames_.size() < 3) {
        status_ = NOT_ENOUGH_STRATS;
    }
    if (mode_ == TOURNAMENT &&
        strategyNames_.size() < 4) {
        status_ = NOT_ENOUGH_STRATS;
    }
}

bool Controller::ParseMatrix(const std::ifstream &matrixFile) {
    for (size_t i = 0; i < combinationsCount; i++) {
        std::array<TChoice, combLen> combination = {};
        for (size_t j = 0; j < combLen; j++) {
            std::string word;
            std::cin >> word;
            if (word == "C") {
                combination[j] = COOPERATE;
            } else if (word == "D") {
                combination[j] = DEFEND;
            } else {
                return false;
            }
        }
        std::array<size_t, combLen> scores = {};
        for (size_t j = 0; j < combLen; j++) {
            std::string word;
            std::cin >> word;
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

bool Controller::runGame() {
    std::vector<std::unique_ptr<Strategy>> strategies;
    size_t counter = 0;
    for (auto &strategyName: strategyNames_) {
        strategies.push_back(std::unique_ptr<Strategy>(
                Factory<Strategy, std::string, Strategy *(*)(size_t,
                TChoiceMatrix &, TScoreMap &), size_t, TChoiceMatrix&,
                TScoreMap&>::getInstance()->createProduct(
                strategyName, counter, choiceMatrix_, scoreMap_)));
        counter++;
    }
    std::array<size_t, combLen> totalScores = {0};
    size_t stepsCount = 0;
    while (true) {
        std::string command;
        std::cin >> command;
        if ("quit" == command) {
            break;
        }
        stepsCount++;
        std::array<TChoice, combLen> choices = {};
        choiceMatrix_.push_back(choices);
        std::array<size_t, combLen> scores = scoreMap_[choices];
        for (size_t i = 0; i < combLen; i++) {
            totalScores[i] += scores[i];
        }
        printStepResults(choices, scores, totalScores, stepsCount, strategyNames_);
    }
    return true;
}
