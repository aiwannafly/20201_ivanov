#ifndef WIREWORLD2D_FIELDWIDGET_H
#define WIREWORLD2D_FIELDWIDGET_H

#include <QBrush>
#include <QPen>
#include <QPixmap>
#include <QWidget>

#include "WireWorldQt2D.h"

class FieldWidget : public QWidget {
Q_OBJECT
public:
    FieldWidget(size_t width, size_t height, size_t cellSizePx, QWidget *parent = nullptr);

    [[nodiscard]] QSize minimumSizeHint() const override;

    [[nodiscard]] QSize sizeHint() const override;

    void setColor(QColor color);

    bool setFieldFromFile(const std::string &fileName);

    void disableDrawing();

    void enableDrawing();

    void updateGameField();

    std::vector<QColor> getColors();

protected:
    void paintEvent(QPaintEvent *event) override;

    void wheelEvent(QWheelEvent* event) override;

    void mousePressEvent(QMouseEvent *event) override;

    void mouseMoveEvent(QMouseEvent *event) override;

    void mouseReleaseEvent(QMouseEvent* event) override;

private:
    struct coords {
        int x;
        int y;
    } leftTop_ = {0, 0};
    WireWorldQt2D game_;
    bool drawON_ = true;
    size_t fieldWidth_ = 0;
    size_t fieldHeight_ = 0;
    QPoint oldPos_;
    QPoint moveDelta_;
    qreal scale_ = 1;
    size_t lengthOfSquarePx_ = 10;
    QColor drawColor_ = Qt::white;
    QColor lineColor_ = {33, 33, 33};
    size_t moveCounter_ = 0;

    void updateXY(int deltaX, int deltaY);
    void drawCell(size_t eventX, size_t eventY);
};

#endif //WIREWORLD2D_FIELDWIDGET_H
