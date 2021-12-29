#include "PrisonerDilemmaUnitTests.h"

#include <iostream>

#include "Runner.h"

namespace {
    constexpr char kInputFileName[] = "5steps_input";

    bool runFromFile(Runner &runner) {
        std::ifstream input(kInputFileName);
        return runner.runGame(std::cout, input);
    }
}

TEST(PrisonerDilemma, Setters) {
    Runner runner;
    runner.setPrintingMode(false);
    runner.setStrategies({"Bob", "Eva"});
    EXPECT_FALSE(runFromFile(runner));
    EXPECT_EQ(runner.getStatus(), WRONG_STRATEGY_NAME);
    runner.setStrategies({"random", "coop"});
    EXPECT_FALSE(runFromFile(runner));
    EXPECT_EQ(runner.getStatus(), NOT_ENOUGH_STRATEGIES);
    runner.setStrategies({"random", "coop", "def"});
    runner.setMode(DETAILED);
    EXPECT_TRUE(runFromFile(runner));
    EXPECT_EQ(OK, runner.getStatus());
    runner.setMode(TOURNAMENT);
    EXPECT_FALSE(runFromFile(runner));
    EXPECT_EQ(runner.getStatus(), NOT_ENOUGH_STRATEGIES);
    runner.setMode(FAST);
    EXPECT_EQ(runner.getStatus(), OK);
    EXPECT_TRUE(runFromFile(runner));
    runner.setStrategies({"pred", "freq", "coop", "def"});
    EXPECT_FALSE(runFromFile(runner));
    EXPECT_EQ(runner.getStatus(), TOO_MANY_STRATEGIES);
    runner.setMode(TOURNAMENT);
    EXPECT_TRUE(runFromFile(runner));
    runner.setConfigsFromFile("configs.txt");
    EXPECT_EQ(runner.getStatus(), OK);
    runner.setMode(FAST);
    runner.setStrategies({"meta", "pred", "freq"});
    runner.setStepsCount(1000);
    EXPECT_TRUE(runFromFile(runner));
    runner.setScoreMapFromFile("ASFASksfajksaksjfbaklfas");
    EXPECT_EQ(runner.getStatus(), MATRIX_FILE_NOT_OPENED);
    runner.setScoreMapFromFile("default_matrix.txt");
    EXPECT_EQ(runner.getStatus(), OK);
}

int RunTests(int argc, char *argv[]) {
    testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
