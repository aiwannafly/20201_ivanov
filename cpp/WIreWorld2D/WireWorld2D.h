#ifndef WIREWORLD2D_WIREWORLD2D_H
#define WIREWORLD2D_WIREWORLD2D_H

#include "Game2D.h"
#include "RunnerWireWorld.h"

class WireWorld2D: public Game2D<TWireWorldCell> {
public:
    WireWorld2D(size_t height, size_t width) : runner_(height, width){};

    TWireWorldCell get(size_t i, size_t j) override {
        return runner_.getField()->get(i, j);
    }

    void set(size_t i, size_t j, TWireWorldCell value) override {
        runner_.getField()->set(i, j, value);
    }

    bool setFieldFromFile(const std::string &fileName) override {
        return runner_.setFieldFromFile(fileName);
    }

    bool proceedTick() override {
        return runner_.proceedTick();
    }

private:
    RunnerWireWorld runner_;
};

#endif //WIREWORLD2D_WIREWORLD2D_H
