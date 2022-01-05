#include <iostream>

#include "Runner.h"

namespace {
    constexpr char kModeKeySeq[] = "--mode=";
    constexpr char kStepsKeySeq[] = "--steps=";
    constexpr char kConfigsKeySeq[] = "--configs=";
    constexpr char kMatrixKeySeq[] = "--matrix=";
    const std::map<TStatus, std::string> kErrorMessages{
            {TStatus::OK,                     "Everything is alright.\n"},
            {TStatus::WRONG_MODE,             "Such mode does not exist.\n"
                                              "Available modes: default, fast, tournament.\n"},
            {TStatus::WRONG_STEPS,            "Wrong steps value, it's not a number.\n"},
            {TStatus::MATRIX_FILE_NOT_OPENED, "File with matrix could not be opened.\n"},
            {TStatus::WRONG_MATRIX,           "Score matrix has a wrong format.\n"},
            {TStatus::NOT_ENOUGH_STRATEGIES,  "Too few strategies for the chosen mode.\n"},
            {TStatus::WRONG_STRATEGY_NAME,    "Strategy with the entered name does not exist.\n"},
            {TStatus::TOO_MANY_STRATEGIES,    "Too many strategies for the chosen mode.\n"},
            {TStatus::OUTPUT_STREAM_FAILURE, "Output stream failure.\n"}
    };

    struct TRunnerData {
        TStatus status = TStatus::OK;
        TMode mode = TMode::DETAILED;
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

    void printErrorMessage(std::ostream &stream, TStatus status) {
        if (status == TStatus::OK) {
            stream << "Everything is OK" << std::endl;
            return;
        }
        if (kErrorMessages.find(status) == kErrorMessages.end()) {
            stream << "Unknown error" << std::endl;
        } else {
            stream << kErrorMessages.at(status) << std::endl;
        }
    }
}

TRunnerData parseRunnerArgs(const std::vector<std::string> &params) {
    TRunnerData data;
    std::string modeKey = kModeKeySeq;
    std::string stepsKey = kStepsKeySeq;
    std::string configsKey = kConfigsKeySeq;
    std::string matrixKey = kMatrixKeySeq;
    std::map<std::string, TMode> modeMap{
            {"detailed",   TMode::DETAILED},
            {"fast",       TMode::FAST},
            {"tournament", TMode::TOURNAMENT}
    };
    for (const auto &param: params) {
        if (startsWith(param, modeKey)) {
            std::string modeName = param.substr(modeKey.length());
            if (modeMap.find(modeName) == modeMap.end()) {
                data.status = TStatus::WRONG_MODE;
                return data;
            }
            data.mode = modeMap[modeName];
        } else if (startsWith(param, stepsKey)) {
            std::string stepsCount = param.substr(stepsKey.length());
            try {
                data.stepsCount = std::stol(stepsCount);
            } catch (std::invalid_argument &e) {
                data.status = TStatus::WRONG_STEPS;
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
        data.status = TStatus::NOT_ENOUGH_STRATEGIES;
    }
    return data;
}

int main(int argc, char *argv[]) {
    std::vector<std::string> params;
    for (size_t i = 1; i < argc; i++) {
        params.emplace_back(argv[i]);
    }
    TRunnerData data = parseRunnerArgs(params);
    if (data.status != TStatus::OK) {
        printErrorMessage(std::cout, data.status);
        return EXIT_FAILURE;
    }
    Runner runner(data.mode, data.stepsCount, data.configsFileName,
                  data.scoreMapFileName, data.names);
    if (runner.runGame(std::cout)) {
    } else {
        printErrorMessage(std::cout, runner.getStatus());
    }
    return EXIT_SUCCESS;
}
