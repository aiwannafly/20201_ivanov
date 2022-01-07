#ifndef WIREWORLD2D_WIREWORLDQT2D_H
#define WIREWORLD2D_WIREWORLDQT2D_H

#include <QColor>
#include <vector>

#include "WireWorld2D.h"

namespace {
    const QColor WHITE = Qt::white;
    const QColor BLUE = Qt::blue;
    const QColor ORANGE = {255, 153, 51};
    const QColor BLACK = Qt::black;

    const std::vector<QColor> COLORS = {WHITE, BLUE, ORANGE, BLACK};
}

class WireWorldQt2D : public WireWorld2D {
public:
    WireWorldQt2D(size_t height, size_t width): WireWorld2D(height, width){};

    QColor getCellColor(TWireWorldCell cond) {
        switch (cond) {
            case TWireWorldCell::ELECTRON_TAIL:
                return WHITE;
            case TWireWorldCell::ELECTRON_HEAD:
                return BLUE;
            case TWireWorldCell::CONDUCTOR:
                return ORANGE;
            default:
                return BLACK;
        }
    }

    TWireWorldCell getCellType(QColor color) {
        if (color == WHITE) {
            return TWireWorldCell::ELECTRON_TAIL;
        } else if (color == BLUE) {
            return TWireWorldCell::ELECTRON_HEAD;
        } else if (color == ORANGE) {
            return TWireWorldCell::CONDUCTOR;
        }
        return TWireWorldCell::EMPTY_CELL;
    }

    std::vector<QColor> getColors() {
        return COLORS;
    }
};

#endif //WIREWORLD2D_WIREWORLDQT2D_H
