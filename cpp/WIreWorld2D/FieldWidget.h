#ifndef WIREWORLD2D_FIELDWIDGET_H
#define WIREWORLD2D_FIELDWIDGET_H

#include <QBrush>
#include <QPen>
#include <QPixmap>
#include <QWidget>

#include "WireWorldFieldTypes.h"

enum TMOUSE_MODE {
    DRAW, MOVE
};

class FieldWidget : public QWidget {
Q_OBJECT
public:
    FieldWidget(size_t width, size_t height, size_t cellSizePx, QWidget *parent = nullptr);

    FieldWidget(size_t width, size_t height, size_t cellSizePx, TField *field,
                QWidget *parent = nullptr);

    QSize minimumSizeHint() const override;

    QSize sizeHint() const override;

    void setColor(TCell cond);

    void setField(TField *field);

    TField *getField();

    void setMouseMode(TMOUSE_MODE mode);

    bool setFieldFromFile(const std::string &fileName);

    void disableDrawing();

    void enableDrawing();

protected:
    void paintEvent(QPaintEvent *event) override;

    void wheelEvent(QWheelEvent* event) override;

    void mousePressEvent(QMouseEvent *event) override;

    void mouseMoveEvent(QMouseEvent *event) override;

    void mouseReleaseEvent(QMouseEvent* event) override;

private:
    bool drawON_ = true;
    size_t fwidth_ = 0;
    size_t fheight_ = 0;
    int coordX_ = 0;
    int coordY_ = 0;
    QPoint oldPos_;
    QPoint moveDelta_;
    TMOUSE_MODE mouseMode_ = DRAW;
    TCell drawCellType_ = ELECTRON_TAIL;
    qreal scale_ = 1;
    size_t cellSize_ = 10;
    TField *field_;
    QColor lineColor_ = Qt::black;

    void updateXY(int deltaX, int deltaY);
    void drawCell(QMouseEvent *event);
};

#endif //WIREWORLD2D_FIELDWIDGET_H
