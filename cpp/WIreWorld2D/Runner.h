#ifndef WIREWORLD2D_RUNNER_H
#define WIREWORLD2D_RUNNER_H

#include <string>
#include <vector>

#ifndef TCELLTYPE
#define TCELLTYPE
enum TCellType {
    EMPTY_CELL, ELECTRON_TAIL, ELECTRON_HEAD, CONDUCTOR
};
#endif

using TField = std::vector<std::vector<TCellType>>;

class Runner {
public:
    Runner(size_t width, size_t height);

    ~Runner() = default;

    bool proceedTick();

    bool setFieldFromFile(const std::string &fileName);

    bool setField(TField& field);

    TField &getField();

    size_t getCountOfSteps() const;

    void clearSteps();

private:
    size_t stepsCount_ = 0;
    size_t width_ = 0;
    size_t height_ = 0;
    TField field_ = {};

    size_t getCountOfHeads(TField &field, int x, int y);
};

#endif //WIREWORLD2D_RUNNER_H
