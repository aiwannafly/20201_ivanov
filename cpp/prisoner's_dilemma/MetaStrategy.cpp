#include "MetaStrategy.h"

#include <experimental/random>
#include <memory>

#include "Factory.h"

namespace {
    Strategy *create() {
        return new MetaStrategy();
    }
}

bool metaB = Factory<Strategy, std::string>::getInstance()->
registerCreator(metaID, create);

void MetaStrategy::setConfigsFileName(const std::string &configsFileName) {
    std::ifstream configsFile;
    configsFile.exceptions(std::ifstream::failbit | std::ifstream::badbit);
    try {
        configsFile.open(configsFileName);
    } catch (std::ifstream::failure &error) {
        return; // got no params
    }
    std::string currentWord;
    while (currentWord != metaID) {
        configsFile >> currentWord;
    }
    while (true) {
        configsFile >> currentWord;
        if (currentWord == kConfigsLineEnd) {
            break;
        }
        strategiesNames_.push_back(currentWord);
    }
}

TChoice MetaStrategy::getChoice() {
    if (strategiesNames_.empty()) {
        // then we use the "random" strategy
        int num = std::experimental::randint(0, 1);
        if (num == 0) {
            return TChoice::COOP;
        }
        return TChoice::DEF;
    }
    if (strategiesCounter_ >= strategiesNames_.size()) {
        strategiesCounter_ = 0;
    }
    auto strategy = std::unique_ptr<Strategy>(Factory<Strategy, std::string, size_t,
            TChoicesList &, TScoreMap &, TConfigs &>::getInstance()->createProduct(
            strategiesNames_[strategiesCounter_], orderNumber_, history_,
            scoreMap_, configsFileName_));
    strategiesCounter_++;
    if (strategy) {
        return strategy->getChoice();
    }
    int num = std::experimental::randint(0, 1);
    if (num == 0) {
        return TChoice::COOP;
    }
    return TChoice::DEF;
}
