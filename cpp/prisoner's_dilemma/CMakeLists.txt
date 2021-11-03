cmake_minimum_required(VERSION 3.20)
project(prisoner_dilemma)

set(CMAKE_CXX_STANDARD 14)

add_executable(prisoner_dilemma main.cpp Factory.cpp Factory.h Strategy.cpp Strategy.h AlwaysCoopStrategy.cpp AlwaysCoopStrategy.h AlwaysDefStrategy.cpp AlwaysDefStrategy.h Controller.cpp Controller.h MostFreqStrategy.cpp MostFreqStrategy.h RandomStrategy.cpp RandomStrategy.h)
