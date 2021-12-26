cmake_minimum_required(VERSION 3.20)
project(prisoner_dilemma)

set(CMAKE_CXX_STANDARD 14)

add_executable(prisoner_dilemma main.cpp Factory.cpp Factory.h Strategy.cpp Strategy.h AlwaysCoopStrategy.cpp AlwaysCoopStrategy.h AlwaysDefStrategy.cpp AlwaysDefStrategy.h Runner.cpp Runner.h MostFreqStrategy.cpp MostFreqStrategy.h RandomStrategy.cpp RandomStrategy.h PrisonerDilemmaUnitTests.cpp PrisonerDilemmaUnitTests.h PredictionStrategy.cpp PredictionStrategy.h MetaStrategy.cpp MetaStrategy.h RunnerIO.cpp RunnerIO.h StrategyTypesAndConstants.h RunnerTypesAndConstants.h)
