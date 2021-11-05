#include <iostream>

#include "Runner.h"

int main(int argc, char *argv[]) {
    /*
     * TODO: add two more not trivial strategies,
     * TODO: add module tests
     * TODO: add std::ios::exceptions
     */
    std::vector<std::string> params;
    for (size_t i = 1; i < argc; i++) {
        params.emplace_back(argv[i]);
    }
    Runner runner(params);
    if (OK == runner.getStatus()) {
        runner.runGame(std::cout);
    } else {
        runner.printErrorMessage(std::cout);
    }
    return EXIT_SUCCESS;
}
