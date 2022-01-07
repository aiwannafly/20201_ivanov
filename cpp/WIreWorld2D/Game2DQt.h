#ifndef WIREWORLD2D_GAME2DQT_H
#define WIREWORLD2D_GAME2DQT_H

#include <QColor>

#include "Game2D.h"

template<class CellState>
class Game2DQt : public Game2D<CellState> {
public:
    virtual QColor getCellColor(CellState cond) = 0;

    virtual CellState getCellType(QColor color) = 0;

    virtual std::vector<QColor> getColors() = 0;
};

#endif //WIREWORLD2D_GAME2DQT_H
