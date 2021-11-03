#include "RandomStrategy.h"

#include <random>

#include "Factory.h"
#include "Factory.cpp"

TChoice RandomStrategy::getChoice() {
    int randInt = rand();
    if (randInt % 2 == 0) {
        return COOPERATE;
    }
    return DEFEND;
}
