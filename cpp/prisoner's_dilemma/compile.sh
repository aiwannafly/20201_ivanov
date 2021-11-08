clang++ -Wall -pedantic -fsanitize=address main.cpp PrisonerDilemmaUnitTests.cpp -lgtest Factory.cpp Strategy.cpp MetaStrategy.cpp PredictionStrategy.cpp AlwaysCoopStrategy.cpp AlwaysDefStrategy.cpp Runner.cpp MostFreqStrategy.cpp RandomStrategy.cpp -o pris_dil


