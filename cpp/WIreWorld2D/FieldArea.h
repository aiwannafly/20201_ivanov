#ifndef WIREWORLD2D_FIELDAREA_H
#define WIREWORLD2D_FIELDAREA_H

#include <array>

#include <QBrush>
#include <QPen>
#include <QPixmap>
#include <QWidget>

#include "Runner.h"

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

protected:
    void paintEvent(QPaintEvent *event) override;

    void wheelEvent(QWheelEvent* event) override;

    void mousePressEvent(QMouseEvent *event) override;

    void mouseMoveEvent(QMouseEvent *event) override;

private:
    bool isRunning = false;
    Runner runner_;
    QPoint oldPos_ = {0, 0};
    std::string fieldFile_;
    qreal scale_ = 1;
    size_t cellSize_ = 10;
    TField cells_ = {};
};

#endif //WIREWORLD2D_FIELDAREA_H
