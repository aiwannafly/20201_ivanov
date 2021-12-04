#ifndef WIREWORLD2D_FIELDAREA_H
#define WIREWORLD2D_FIELDAREA_H

#include <array>

#include <QBrush>
#include <QPen>
#include <QPixmap>
#include <QWidget>

#include "Runner.h"

enum TMOUSE_MODE {
    DRAW, MOVE
};

class FieldArea : public QWidget {
Q_OBJECT
public:
    FieldArea(QWidget *parent = nullptr);

    QSize minimumSizeHint() const override;

    QSize sizeHint() const override;

    bool setField(const std::string &fileName);

    bool proceedTick();

    void run();

    void stop();

    bool isRun();

    size_t getSteps() {
        return runner_.getSteps();
    };

    void setColor(conditions cond) {
        drawCellType_ = cond;
    }

    void setMouseMode(TMOUSE_MODE mode);

protected:
    void paintEvent(QPaintEvent *event) override;

    void wheelEvent(QWheelEvent* event) override;

    void mousePressEvent(QMouseEvent *event) override;

    void mouseMoveEvent(QMouseEvent *event) override;

    void mouseReleaseEvent(QMouseEvent* event) override;

private:
    int coordX_ = 0;
    int coordY_ = 0;
    QPoint oldPos_;
    QPoint moveDelta_;
    TMOUSE_MODE mouseMode_ = DRAW;
    conditions drawCellType_ = ELECTRON_TAIL;
    bool isRunning = false;
    Runner runner_;
    qreal scale_ = 1;
    size_t cellSize_ = 10;
    TField cells_ = {};

    void updateXY(int deltaX, int deltaY);
    void drawCell(QMouseEvent *event);
};

#endif //WIREWORLD2D_FIELDAREA_H
