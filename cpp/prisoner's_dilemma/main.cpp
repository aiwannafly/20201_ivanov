#include <iostream>

#include "Controller.h"
#include "Controller.cpp"

namespace {
    Strategy *create(size_t orderNumber, TChoiceMatrix &history,
                     TScoreMap &scoreMap) {
        return new RandomStrategy(orderNumber, history, scoreMap);
    }
}

bool b = Factory<Strategy, std::string, Strategy* (*)(size_t, TChoiceMatrix&,
        TScoreMap&), size_t, TChoiceMatrix&, TScoreMap&>::getInstance()->registerCreator(randomID, create);

int main(int argc, char *argv[]) {
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
