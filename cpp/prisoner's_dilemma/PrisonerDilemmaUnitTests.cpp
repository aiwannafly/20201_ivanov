#include "PrisonerDilemmaUnitTests.h"

#include <iostream>

#include "Runner.h"

TEST(PrisonerDilemma, Setters) {
    Runner runner;
    std::vector<std::string> badNames = {"Bob", "Eva"};
    runner.setStrategies(badNames);
    EXPECT_EQ(runner.getStatus(), WRONG_STRATEGY_NAME);
    std::vector<std::string> fewNames = {"random", "coop"};
    runner.setStrategies(fewNames);
    EXPECT_EQ(runner.getStatus(), NOT_ENOUGH_STRATS);

}

int RunTests(int argc, char *argv[]) {
    testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
