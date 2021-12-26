#include "PrisonerDilemmaUnitTests.h"

#include <iostream>

#include "Runner.h"

TEST(PrisonerDilemma, Setters) {
    Runner runner;
    runner.setPrintingMode();
    runner.setStrategies({"Bob", "Eva"});
    EXPECT_EQ(runner.getStatus(), WRONG_STRATEGY_NAME);
    runner.setStrategies({"random", "coop"});
    EXPECT_EQ(runner.getStatus(), NOT_ENOUGH_STRATEGIES);
    runner.setStrategies({"random", "coop", "def"});
    runner.printErrorMessage(std::cout);
    EXPECT_EQ(runner.getStatus(), OK);
    runner.setMode(TOURNAMENT);
    EXPECT_FALSE(runner.runGame(std::cout));
    EXPECT_EQ(runner.getStatus(), NOT_ENOUGH_STRATEGIES);
    runner.setMode(FAST);
    EXPECT_EQ(runner.getStatus(), OK);
    EXPECT_TRUE(runner.runGame(std::cout));
    runner.setStrategies({"pred", "freq", "coop", "def"});
    EXPECT_EQ(runner.getStatus(), TOO_MANY_STRATEGIES);
    runner.setMode(TOURNAMENT);
    EXPECT_EQ(runner.getStatus(), OK);
    EXPECT_TRUE(runner.runGame(std::cout));
    runner.setConfigsFromFile("somethingveryveryweird.com");
    EXPECT_EQ(runner.getStatus(), CONFIGS_FILE_NOT_OPENED);
    runner.setConfigsFromFile("configs.txt");
    EXPECT_EQ(runner.getStatus(), OK);
    runner.setMode(FAST);
    runner.setStrategies({"meta", "pred", "freq"});
    runner.setStepsCount(100);
    // add test for detailed mode
    EXPECT_TRUE(runner.runGame(std::cout));
    runner.setScoreMapFromFile("ASFASksfajksaksjfbaklfas");
    EXPECT_EQ(runner.getStatus(), MATRIX_FILE_NOT_OPENED);
    EXPECT_FALSE(runner.setMode(TOURNAMENT));
    runner.setScoreMapFromFile("default_matrix.txt");
    EXPECT_EQ(runner.getStatus(), OK);
}

int RunTests(int argc, char *argv[]) {
    testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
