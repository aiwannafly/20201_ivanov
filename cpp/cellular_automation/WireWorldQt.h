#ifndef WIREWORLD2D_WIREWORLDQT_H
#define WIREWORLD2D_WIREWORLDQT_H

#include <QColor>
#include <vector>

#include "GameQt.h"
#include "WireWorld.h"

namespace {
    const QColor WHITE = Qt::white;
    const QColor BLUE = Qt::blue;
    const QColor ORANGE = {255, 153, 51};
    const QColor BLACK = Qt::black;

    const std::vector<QColor> WIREWORLD_COLORS = {WHITE, BLUE, ORANGE};
}

class WireWorldQt : public GameQt, public WireWorld {
public:
    WireWorldQt(size_t width, size_t height): WireWorld(width, height) {};

    ~WireWorldQt() override = default;

    void set(size_t i, size_t j, int state) override {
        WireWorld::set(i, j, state);
    }

    int get(size_t i, size_t j) override {
        return WireWorld::get(i, j);
    }

    bool proceedTick() override {
        return WireWorld::proceedTick();
    }

    bool setFieldFromFile(const std::string &fileName) override {
        return WireWorld::setFieldFromFile(fileName);
    }

    QColor getCellColor(int state) override {
        switch (state) {
            case WireWorld::ELECTRON_TAIL:
                return WHITE;
            case WireWorld::ELECTRON_HEAD:
                return BLUE;
            case WireWorld::CONDUCTOR:
                return ORANGE;
            default:
                return BLACK;
        }
    }

    int getCellType(QColor color) override {
        if (color == WHITE) {
            return WireWorld::ELECTRON_TAIL;
        } else if (color == BLUE) {
            return WireWorld::ELECTRON_HEAD;
        } else if (color == ORANGE) {
            return WireWorld::CONDUCTOR;
        }
        return WireWorld::EMPTY_CELL;
    }

    std::vector<QColor> getColors() override {
        return WIREWORLD_COLORS;
    }

};

#endif //WIREWORLD2D_WIREWORLDQT_H
