#ifndef WIREWORLD2D_GAMEQT_H
#define WIREWORLD2D_GAMEQT_H

#include <QColor>

#include "Game.h"

class GameQt : public Game {
public:
    virtual QColor getCellColor(int state) = 0;

    virtual int getCellType(QColor color) = 0;

    virtual std::vector<QColor> getColors() = 0;

};

#endif //WIREWORLD2D_GAMEQT_H
