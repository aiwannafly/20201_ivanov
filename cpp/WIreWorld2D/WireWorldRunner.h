#ifndef WIREWORLD2D_WIREWORLDRUNNER_H
#define WIREWORLD2D_WIREWORLDRUNNER_H

#include <array>
#include <string>

enum conditions {
    EMPTY_CELL, ELECTRON_TAIL, ELECTRON_HEAD, CONDUCTOR
};

constexpr std::size_t fwidth = 80;
constexpr std::size_t fheight = 80;
using TField = std::array<std::array<conditions, fwidth>, fheight>;

class WireWorldRunner {
public:
    WireWorldRunner() = default;

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
    size_t width_ = fwidth;
    size_t height_ = fheight;
    TField field_ = {};

    static size_t getCountOfHeads(TField &field, int x, int y);
};

#endif //WIREWORLD2D_WIREWORLDRUNNER_H
