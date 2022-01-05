#include "RunnerIO.h"

#include <array>
#include <vector>

bool RunnerIO::parseMatrix(std::ifstream &matrixFile, TScoreMap *scoreMap) {
    for (size_t i = 0; i < 2*2*2; i++) {
        std::array<TChoice, combLen> combination = {};
        for (size_t j = 0; j < combLen; j++) {
            std::string word;
            matrixFile >> word;
            if (word == "C" || word == "С") {
                combination[j] = TChoice::COOP;
            } else if (word == "D") {
                combination[j] = TChoice::DEF;
            } else {
                return false;
            }
        }
        std::array<size_t, combLen> scores = {};
        for (size_t j = 0; j < combLen; j++) {
            matrixFile >> scores[j];
        }
        scoreMap->at(combination) = scores;
    }
    return true;
}

void RunnerIO::printStepResults(std::ostream &stream, std::array<size_t, combLen> results, size_t stepNumber,
                      const std::vector<std::string> &strategyNames,
                      const std::array<TChoice, combLen> &roundChoices,
                      std::map<std::string, size_t> &totalScores, bool printing) {
    if (!printing) {
        return;
    }
    std::map<TChoice, std::string> choiceMap;
    choiceMap[TChoice::COOP] = "C";
    choiceMap[TChoice::DEF] = "D";
    stream << "=================== ROUND №" <<
           stepNumber << " ==============" << std::endl;
    stream << "    NAMES    |";
    for (const auto &name: strategyNames) {
        stream << "\t" << name;
    }
    stream << std::endl;
    stream << "   CHOICES   |";
    for (size_t i = 0; i < combLen; i++) {
        stream << "\t" << choiceMap[roundChoices[i]];
    }
    stream << std::endl;
    stream << "ROUND SCORES |";
    for (size_t i = 0; i < combLen; i++) {
        stream << "\t" << results[i];
    }
    stream << std::endl;
    stream << "TOTAL SCORES |";
    for (const auto &name: strategyNames) {
        stream << "\t" << totalScores[name];
    }
    stream << std::endl;
    stream << "===========================================" << std::endl;
}

void RunnerIO::printGameResults(std::ostream &stream, size_t stepsCount,
                      const std::vector<std::string> &strategyNames,
                      std::map<std::string, size_t> &totalScores, bool printing) {
    if (!printing) {
        return;
    }
    stream << "=================== GAME RESULTS ==============" << std::endl;
    stream << "STEPS COUNT" << "\t|\t" << stepsCount << std::endl;
    stream << "-----------------------------------------------" << std::endl;
    for (const auto &name: strategyNames) {
        if (totalScores.find(name) == totalScores.end()) {
            continue;
        }
        stream << name << "\t|\t" << totalScores[name] << std::endl;
    }
    stream << "===============================================" << std::endl;
}

void RunnerIO::printTotalResults(std::ostream &stream,
                       const std::vector<std::string> &strategyNames,
                       std::map<std::string, size_t> &totalScores, bool printing) {
    if (!printing) {
        return;
    }
    stream << "================== TOTAL RESULTS ==============" << std::endl;
    size_t maxScore = 0;
    for (const auto &name: strategyNames) {
        if (totalScores[name] > maxScore) {
            maxScore = totalScores[name];
        }
    }
    for (const auto &name: strategyNames) {
        if (totalScores[name] == maxScore) {
            stream << name << "\t|\t" << totalScores[name]
                   << "\t\t<--- WINNER" << std::endl;
        } else {
            stream << name << "\t|\t" << totalScores[name] << std::endl;
        }
    }
    stream << "==============================================" << std::endl;
}
