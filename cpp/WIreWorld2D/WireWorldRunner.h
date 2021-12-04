#ifndef WIREWORLD2D_WIREWORLDRUNNER_H
#define WIREWORLD2D_WIREWORLDRUNNER_H

#include <string>
#include <vector>

#ifndef TCELLTYPE
#define TCELLTYPE
enum TCellType {
    EMPTY_CELL, ELECTRON_TAIL, ELECTRON_HEAD, CONDUCTOR
};
#endif

using TField = std::vector<std::vector<TCellType>>;

class WireWorldRunner {
public:
    WireWorldRunner(size_t width, size_t height);

    ~WireWorldRunner() = default;

    bool proceedTick();

    bool getFieldFromFile(const std::string &fileName);

    bool setField(TField& field);

    TField &getField();

    size_t getCountOfSteps() const {
        return steps_;
    }

private:
    size_t steps_ = 0;
    size_t xOffset_ = 0;
    size_t yOffset_ = 0;
    size_t fwidth_ = 0;
    size_t fheight_ = 0;
    size_t width_ = 0;
    size_t height_ = 0;
    TField field_ = {};

    size_t getCountOfHeads(TField &field, int x, int y);
};

#endif //WIREWORLD2D_WIREWORLDRUNNER_H
