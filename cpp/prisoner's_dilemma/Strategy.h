#ifndef PRISONER_DILEMMA_STRATEGY_H
#define PRISONER_DILEMMA_STRATEGY_H

#include <map>
#include <string>
#include <vector>

typedef enum TChoice {
    COOPERATE, DEFEND
} TChoice;

constexpr size_t combLen = 3;

typedef std::vector<std::array<TChoice, combLen>> TChoiceMatrix;
typedef std::map<std::array<TChoice, combLen>, std::array<size_t, combLen>> TScoreMap;
typedef std::vector<std::string> TConfigs;

class Strategy {
public:
    Strategy(size_t orderNumber, TChoiceMatrix &history,
             TScoreMap &scoreMap, TConfigs &configs)
            : orderNumber_(orderNumber), history_(history),
              scoreMap_(scoreMap), configs_(configs) {};

    virtual ~Strategy() = default;

    virtual TChoice getChoice() = 0;

    TChoiceMatrix getHistory() const;

    TScoreMap getScoreMap() const;

    TConfigs getConfigs() const;

    size_t getOrderNumber() const;

private:
    size_t orderNumber_ = 0;
    TChoiceMatrix history_;
    TScoreMap scoreMap_;
    TConfigs configs_;
};

#endif //PRISONER_DILEMMA_STRATEGY_H
