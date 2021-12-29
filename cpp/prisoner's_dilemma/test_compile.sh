clang++ -Wall -pedantic -fsanitize=address main_tests.cpp PrisonerDilemmaUnitTests.cpp -lgtest MetaStrategy.cpp PredictionStrategy.cpp AlwaysCoopStrategy.cpp AlwaysDefStrategy.cpp Runner.cpp MostFreqStrategy.cpp RandomStrategy.cpp RunnerIO.cpp -o unit_tests_pris_dil


