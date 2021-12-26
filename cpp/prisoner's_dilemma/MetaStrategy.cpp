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

TChoice MetaStrategy::getChoice() {
    TConfigs configs = getConfigs();
    TChoiceMatrix choiceMatrix = getHistory();
    TScoreMap scoreMap = getScoreMap();
    if (configs.empty()) {
        int num = rand();
        if (num % 2 == 0) {
            return COOP;
        }
        return DEF;
    }
    if (strategiesCounter_ >= configs.size()) {
        strategiesCounter_ = 0;
    }
    // std::make_unique + auto
    std::unique_ptr<Strategy> strategy = std::unique_ptr<Strategy>
            (Factory<Strategy, std::string, size_t,TChoiceMatrix &, TScoreMap &, TConfigs &>
             ::getInstance()->createProduct(configs[strategiesCounter_],
                                            getOrderNumber(),choiceMatrix, scoreMap, configs));
    strategiesCounter_++;
    if (strategy) {
        return strategy->getChoice();
    }
    int num = rand(); // generators classes
    if (num % 2 == 0) {
        return COOP;
    }
    return DEF;
}
