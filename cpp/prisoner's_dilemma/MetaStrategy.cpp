#include "MetaStrategy.h"

#include <memory>

#include "Factory.h"
#include "Factory.cpp"

namespace {
    Strategy *create(size_t orderNumber, TChoiceMatrix &history,
                     TScoreMap &scoreMap, TConfigs &configs) {
        return new MetaStrategy(orderNumber, history, scoreMap, configs);
    }
}

bool metaB = Factory<Strategy, std::string, size_t, TChoiceMatrix &, TScoreMap &, TConfigs &>
::getInstance()->registerCreator(metaID, create);
#include <iostream>

TChoice MetaStrategy::getChoice() {
    TConfigs configs = getConfigs();
    TChoiceMatrix choiceMatrix = getHistory();
    TScoreMap scoreMap = getScoreMap();
    if (configs.empty()) {
        std::cout << "1" << std::endl;
        int num = rand();
        if (num % 2 == 0) {
            return COOPERATE;
        }
        return DEFEND;
    }
    if (strategiesCounter_ >= configs.size()) {
        strategiesCounter_ = 0;
    }
    std::unique_ptr<Strategy> strategy = std::unique_ptr<Strategy>
            (Factory<Strategy, std::string, size_t,TChoiceMatrix &, TScoreMap &, TConfigs &>
             ::getInstance()->createProduct(configs[strategiesCounter_],
                                            getOrderNumber(),choiceMatrix, scoreMap, configs));
    strategiesCounter_++;
    if (strategy) {
        std::cout << "2" << std::endl;
        return strategy->getChoice();
    }
    std::cout << "3" << std::endl;
    int num = rand();
    if (num % 2 == 0) {
        return COOPERATE;
    }
    return DEFEND;
}
