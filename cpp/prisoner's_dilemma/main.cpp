#include <iostream>

#include "Runner.h"

namespace {
    constexpr char kModeKeySeq[] = "--mode=";
    constexpr char kStepsKeySeq[] = "--steps=";
    constexpr char kConfigsKeySeq[] = "--configs=";
    constexpr char kMatrixKeySeq[] = "--matrix=";

    using TRunnerData = struct TRunnerData {
        TStatus status = OK;
        TMode mode = DETAILED;
        size_t stepsCount = 10;
        std::string configsFileName;
        std::string scoreMapFileName;
        std::vector<std::string> names = {};
    };

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

TRunnerData parseRunnerArgs(const std::vector<std::string> &params) {
    TRunnerData data;
    std::string modeKey = kModeKeySeq;
    std::string stepsKey = kStepsKeySeq;
    std::string configsKey = kConfigsKeySeq;
    std::string matrixKey = kMatrixKeySeq;
    std::map<std::string, TMode> modeMap{
            {"detailed",   DETAILED},
            {"fast",       FAST},
            {"tournament", TOURNAMENT}
    };
    for (const auto &param: params) {
        if (startsWith(param, modeKey)) {
            std::string modeName = param.substr(modeKey.length());
            if (modeMap.find(modeName) == modeMap.end()) {
                data.status = WRONG_MODE;
                return data;
            }
            data.mode = modeMap[modeName];
        } else if (startsWith(param, stepsKey)) {
            std::string stepsCount = param.substr(stepsKey.length());
            try {
                std::stol(stepsCount);
            } catch (std::invalid_argument &e) {
                data.status = WRONG_STEPS;
                return data;
            }
        } else if (startsWith(param, configsKey)) {
            std::string fileName = param.substr(configsKey.length());
            data.configsFileName = fileName;
        } else if (startsWith(param, matrixKey)) {
            std::string fileName = param.substr(configsKey.length());
            data.scoreMapFileName = fileName;
        } else {
            data.names.push_back(param);
        }
    }
    if (data.names.size() < 3) {
        data.status = NOT_ENOUGH_STRATEGIES;
    }
    return data;
}

int main(int argc, char *argv[]) {
    std::vector<std::string> params;
    for (size_t i = 1; i < argc; i++) {
        params.emplace_back(argv[i]);
    }
    TRunnerData data = parseRunnerArgs(params);
    if (data.status != OK) {
        Runner::printErrorMessage(std::cout, data.status);
        return EXIT_FAILURE;
    }
    Runner runner(data.mode, data.stepsCount, data.configsFileName,
                  data.scoreMapFileName, data.names);
    if (OK == runner.runGame(std::cout)) {
    } else {
        Runner::printErrorMessage(std::cout, runner.getStatus());
    }
    return EXIT_SUCCESS;
}
