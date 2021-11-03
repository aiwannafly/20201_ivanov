#include <iostream>

#include "Controller.h"

int main(int argc, char *argv[]) {
    Controller controller(argc, argv);
    if (OK == controller.getStatus()) {
        controller.runGame();
    } else {
        std::cout << controller.getStatus() << std::endl;
    }
    return EXIT_SUCCESS;
}
