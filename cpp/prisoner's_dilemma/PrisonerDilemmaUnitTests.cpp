#include "PrisonerDilemmaUnitTests.h"

#include <iostream>

#include "Runner.h"

TEST(PrisonerDilemma, BadInput) {
    int argc1 = 4;
    char *argv1[] = {(char*) "fast", (char*) "detailed", (char*) "zebra"};
    Runner controller1(argc1, argv1);
    EXPECT_EQ(controller1.getStatus(), WRONG_STRATEGY_NAME);
    EXPECT_FALSE(controller1.runGame(std::cout));
    int argc2 = 5;
    char *argv2[] = {(char*) "random", (char*) "def",
                     (char*) "freq", (char*) "--mode=tournament"};
    Runner controller2(argc2, argv2);
    EXPECT_EQ(controller2.getStatus(), NOT_ENOUGH_STRATS);
    EXPECT_FALSE(controller2.runGame(std::cout));
    int argc3 = 5;
    char *argv3[] = {(char*) "coop", (char*) "random",
                     (char*) "def", (char*) "--mode=elephant"};
    Runner controller3(argc3, argv3);
    EXPECT_EQ(controller3.getStatus(), WRONG_MODE);
    EXPECT_FALSE(controller2.runGame(std::cout));
    char *argv4[] = {(char*) "coop", (char*) "random",
                     (char*) "def", (char*) "--steps=50*50"};
    Runner controller4(5, argv4);
    EXPECT_EQ(controller4.getStatus(), WRONG_STEPS);
    EXPECT_FALSE(controller4.runGame(std::cout));
    char *argv5[] = {(char*) "coop", (char*) "def",
                     (char*) "random", (char*) "--matrix=main.cpp"};
    Runner controller5(5, argv5);
    EXPECT_TRUE(controller5.getStatus() == WRONG_MATRIX ||
                controller5.getStatus() == MATRIX_FILE_NOT_OPENED);
    EXPECT_FALSE(controller5.runGame(std::cout));
}

TEST(PrisonerDilemma, GoodInput) {
    char *argv1[] = {(char*) "coop", (char*) "def",
                     (char*) "random", (char*) "--mode=fast"};
    Runner controller1(5, argv1);
    EXPECT_EQ(controller1.getStatus(), OK);
    char *argv2[] = {(char*) "coop", (char*) "random", (char*) "def",
                     (char*) "freq", (char*) "--mode=tournament"};
    Runner controller2(6, argv2);
    EXPECT_EQ(controller2.getStatus(), OK);
    char *argv3[] = {(char*) "coop", (char*) "def", (char*) "random"};
    Runner controller3(4, argv3);
    EXPECT_EQ(controller3.getStatus(), OK);
}

int RunTests(int argc, char *argv[]) {
    testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
