#ifndef PRISONER_DILEMMA_RUNNERIO_H
#define PRISONER_DILEMMA_RUNNERIO_H

#include <array>
#include <fstream>
#include <map>
#include <vector>

#include "RunnerTypesAndConstants.h"

namespace RunnerIO {
    bool parseMatrix(std::ifstream &matrixFile, TScoreMap *scoreMap);

    void printStepResults(std::ostream &stream, std::array<size_t, combLen> results, size_t stepNumber,
                          const std::vector<std::string> &strategyNames,
                          const std::array<TChoice, combLen> &roundChoices,
                          std::map<std::string, size_t> &totalScores, bool printing);

    void printGameResults(std::ostream &stream, size_t stepsCount,
                          const std::vector<std::string> &strategyNames,
                          std::map<std::string, size_t> &totalScores, bool printing);

    void printTotalResults(std::ostream &stream,
                           const std::vector<std::string> &strategyNames,
                           std::map<std::string, size_t> &totalScores, bool printing);

}

#endif //PRISONER_DILEMMA_RUNNERIO_H
