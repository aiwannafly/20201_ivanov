#ifndef PRISONER_DILEMMA_STRATEGYTYPESANDCONSTANTS_H
#define PRISONER_DILEMMA_STRATEGYTYPESANDCONSTANTS_H

constexpr size_t combLen = 3;

using TChoice =  enum TChoice {
    COOP, DEF
};
using TChoiceMatrix = std::vector<std::array<TChoice, combLen>>; //using
using TScoreMap = std::map<std::array<TChoice, combLen>, std::array<size_t, combLen>>;
using TConfigs = std::vector<std::string>;

#endif //PRISONER_DILEMMA_STRATEGYTYPESANDCONSTANTS_H
