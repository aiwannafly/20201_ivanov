#include "PrisonerDilemmaUnitTests.h"

#include <iostream>

#include "Runner.h"

TEST(PrisonerDilemma, Setters) {
    Runner runner;
    runner.setStrategies({"Bob", "Eva"});
    EXPECT_EQ(runner.getStatus(), WRONG_STRATEGY_NAME);
    runner.setStrategies({"random", "coop"});
    EXPECT_EQ(runner.getStatus(), NOT_ENOUGH_STRATS);
    runner.setStrategies({"random", "coop", "def"});
    EXPECT_EQ(runner.getStatus(), OK);
    runner.disablePrinting();
    runner.setMode(TOURNAMENT);
    EXPECT_FALSE(runner.runGame(std::cout));
    EXPECT_EQ(runner.getStatus(), NOT_ENOUGH_STRATS);
}

int RunTests(int argc, char *argv[]) {
    testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
