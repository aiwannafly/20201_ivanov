#ifndef WIREWORLD2D_RUNNERWIREWORLD_H
#define WIREWORLD2D_RUNNERWIREWORLD_H

#include <string>

#include "WireWorldFieldTypes.h"

class RunnerWireWorld {
public:
    RunnerWireWorld(size_t height, size_t width) : width_(width),
                                                   height_(height){
        field_ = new TField(height, width);
    };

    RunnerWireWorld(size_t height, size_t width, TField *field) : width_(width),
                                                                  height_(height), field_(field) {}

    RunnerWireWorld(const RunnerWireWorld &) = delete;

    void operator=(const RunnerWireWorld &) = delete;

    ~RunnerWireWorld() = default;

    bool proceedTick();

    bool setFieldFromFile(const std::string &fileName);

    bool setField(TField *field);

    TField *getField() const;

private:
    size_t width_ = 0;
    size_t height_ = 0;
    TField *field_;

    size_t getCountOfHeads(TField &field, int x, int y);
};

#endif //WIREWORLD2D_RUNNERWIREWORLD_H
