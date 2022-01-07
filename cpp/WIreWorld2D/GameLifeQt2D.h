#ifndef WIREWORLD2D_GAMELIFEQT2D_H
#define WIREWORLD2D_GAMELIFEQT2D_H

#include "Game2DQt.h"
#include "GameLife2D.h"

namespace {
    const std::vector<QColor> COLORS = {Qt::green};
}

class GameLifeQt2D : public GameLife2D, public Game2DQt<TGameLifeCell> {
public:
    GameLifeQt2D(size_t height, size_t width): GameLife2D(height, width) {};

    QColor getCellColor(TGameLifeCell value) override {
        if (value == TGameLifeCell::ALIVE) {
            return Qt::green;
        }
        return Qt::black;
    }

    TGameLifeCell getCellType(QColor color) override {
        if (color == Qt::green) {
            return TGameLifeCell::ALIVE;
        }
        return TGameLifeCell::DEAD;
    }

    std::vector<QColor> getColors() override {
        return COLORS;
    }
};

#endif //WIREWORLD2D_GAMELIFEQT2D_H
