#include <iostream>

#include "Controller.h"
//#include "Controller.cpp"

int main(int argc, char *argv[]) {
//    TChoiceMatrix cm;
//    TScoreMap sm;
//    auto *rs = new RandomStrategy(1, cm, sm);
//    std::cout << rs->getChoice() << std::endl;
//    int *a = new int(5);
//    std::unique_ptr<int> ptr = std::unique_ptr<int>(a);
//    std::cout << *ptr << std::endl;
//    return EXIT_SUCCESS;
    Controller controller(argc, argv);
    if (OK == controller.getStatus()) {
        controller.runGame();
    } else {
        std::cout << controller.getStatus() << std::endl;
    }
    return EXIT_SUCCESS;
}
