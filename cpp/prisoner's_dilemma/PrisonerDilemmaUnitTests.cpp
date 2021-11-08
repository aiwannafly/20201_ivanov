#include "PrisonerDilemmaUnitTests.h"

#include <iostream>

#include "Runner.h"

TEST(PrisonerDilemma, Setters) {
    Runner runner;
    runner.disablePrinting();
    runner.setStrategies({"Bob", "Eva"});
    EXPECT_EQ(runner.getStatus(), WRONG_STRATEGY_NAME);
    runner.setStrategies({"random", "coop"});
    EXPECT_EQ(runner.getStatus(), NOT_ENOUGH_STRATS);
    runner.setStrategies({"random", "coop", "def"});
    runner.printErrorMessage(std::cout);
    EXPECT_EQ(runner.getStatus(), OK);
    runner.setMode(TOURNAMENT);
    EXPECT_FALSE(runner.runGame(std::cout));
    EXPECT_EQ(runner.getStatus(), NOT_ENOUGH_STRATS);
    runner.setMode(FAST);
    EXPECT_EQ(runner.getStatus(), OK);
    EXPECT_TRUE(runner.runGame(std::cout));
    runner.setStrategies({"pred", "freq", "coop", "def"});
    EXPECT_EQ(runner.getStatus(), TOO_MANY_STRATS);
    runner.setMode(TOURNAMENT);
    EXPECT_EQ(runner.getStatus(), OK);
    EXPECT_TRUE(runner.runGame(std::cout));
    runner.setConfigs("somethingveryveryweird.com");
    EXPECT_EQ(runner.getStatus(), CONFIGS_FILE_NOT_OPENED);
    runner.setConfigs("configs.txt");
    EXPECT_EQ(runner.getStatus(), OK);
    runner.setMode(FAST);
    runner.setStrategies({"meta", "pred", "freq"});
    runner.setStepsCount(100);
    EXPECT_TRUE(runner.runGame(std::cout));
}

TEST(PrisonerDilemma, LargeTest) {
    Runner runner;
}

int RunTests(int argc, char *argv[]) {
    testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
