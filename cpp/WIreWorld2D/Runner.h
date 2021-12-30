#ifndef WIREWORLD2D_RUNNER_H
#define WIREWORLD2D_RUNNER_H

#include <string>

#include "Field.h"
#include "WireWorldFieldTypes.h"

class Runner {
public:
    Runner(size_t width, size_t height) : width_(width),
    height_(height){
        field_ = new TField(height, width);
    };

    Runner(size_t width, size_t height, TField *field) : width_(width),
    height_(height), field_(field) {}

    ~Runner() = default;

    bool proceedTick();

    bool setFieldFromFile(const std::string &fileName);

    bool setField(TField *field);

    TField *getField() const;

    size_t getCountOfSteps() const;

    void clearSteps();

private:
    size_t stepsCount_ = 0;
    size_t width_ = 0;
    size_t height_ = 0;
    TField *field_;

    size_t getCountOfHeads(TField &field, int x, int y);
};

#endif //WIREWORLD2D_RUNNER_H
