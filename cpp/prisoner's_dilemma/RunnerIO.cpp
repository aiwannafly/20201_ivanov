#include "RunnerIO.h"

#include <array>
#include <vector>

namespace {
    std::map<TStatus, std::string> kErrorMessages{
            {OK,                     "Everything is alright.\n"},
            {WRONG_MODE,             "Such mode does not exist.\n "
                                     "Available mods: default, fast, tournament.\n"},
            {WRONG_STEPS,            "Wrong steps value, it's not a number.\n"},
            {MATRIX_FILE_NOT_OPENED, "File with matrix could not be opened.\n"},
            {WRONG_MATRIX,           "Score matrix has a wrong format.\n"},
            {NOT_ENOUGH_STRATEGIES,  "Too few strategies for the chosen mode.\n"},
            {WRONG_STRATEGY_NAME,    "Strategy with the entered name does not exist.\n"},
            {TOO_MANY_STRATEGIES,    "Too many strategies for the chosen mode.\n"},
            {OUTPUT_STREAM_FAILURE, "Output stream failure.\n"}
    };
}

bool RunnerIO::parseMatrix(std::ifstream &matrixFile, TScoreMap *scoreMap) {
    for (size_t i = 0; i < 2*2*2; i++) {
        std::array<TChoice, combLen> combination = {};
        for (size_t j = 0; j < combLen; j++) {
            std::string word;
            matrixFile >> word;
            if (word == "C" || word == "С") {
                combination[j] = COOP;
            } else if (word == "D") {
                combination[j] = DEF;
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
    choiceMap[COOP] = "C";
    choiceMap[DEF] = "D";
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

void RunnerIO::printErrorMessage(std::ostream &stream, TStatus status) {
    if (status == OK) {
        stream << "Everything is OK" << std::endl;
        return;
    }
    if (kErrorMessages.find(status) == kErrorMessages.end()) {
        stream << "Unknown error" << std::endl;
    } else {
        stream << kErrorMessages[status] << std::endl;
    }
}
