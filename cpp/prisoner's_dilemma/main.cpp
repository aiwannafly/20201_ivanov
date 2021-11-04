#include <iostream>

#include "Controller.h"

int main(int argc, char *argv[]) {
    Controller controller(argc, argv);
    if (OK == controller.getStatus()) {
        controller.runGame(std::cout);
    } else {
        controller.printErrorMessage(std::cout);
    }
    return EXIT_SUCCESS;
}
