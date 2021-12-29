#ifndef PRISONER_DILEMMA_STRATEGYTYPESANDCONSTANTS_H
#define PRISONER_DILEMMA_STRATEGYTYPESANDCONSTANTS_H

constexpr size_t combLen = 3;
constexpr char kConfigsLineEnd[] = "end";

using TChoice =  enum TChoice {
    COOP, DEF
};
using TChoicesList = std::vector<std::array<TChoice, combLen>>;
using TScoreMap = std::map<std::array<TChoice, combLen>, std::array<size_t, combLen>>;
using TConfigs = std::string;

#endif //PRISONER_DILEMMA_STRATEGYTYPESANDCONSTANTS_H
