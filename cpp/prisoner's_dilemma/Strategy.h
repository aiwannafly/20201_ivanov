#ifndef PRISONER_DILEMMA_STRATEGY_H
#define PRISONER_DILEMMA_STRATEGY_H

#include <map>
#include <vector>

typedef enum TChoice {
    COOPERATE, DEFEND
} TChoice;

constexpr size_t combLen = 3;

typedef std::vector<std::array<TChoice, 3>> TChoiceMatrix;
typedef std::map<std::array<TChoice, combLen>, std::array<size_t, combLen>> TScoreMap;

class Strategy {
public:
    Strategy(size_t orderNumber, TChoiceMatrix &history,
                      TScoreMap &scoreMap)
            : orderNumber_(orderNumber), history_(history),
              scoreMap_(scoreMap) {};

    virtual ~Strategy() = default;

    virtual TChoice getChoice() = 0;

    TChoiceMatrix getHistory() const;

    TScoreMap getScoreMap() const;

    size_t getOrderNumber() const;

private:
    size_t orderNumber_ = 0;
    TChoiceMatrix history_;
    TScoreMap scoreMap_;
};

#endif //PRISONER_DILEMMA_STRATEGY_H
