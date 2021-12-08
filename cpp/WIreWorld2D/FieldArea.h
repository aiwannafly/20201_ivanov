#ifndef WIREWORLD2D_FIELDAREA_H
#define WIREWORLD2D_FIELDAREA_H

#include <array>

#include <QBrush>
#include <QPen>
#include <QPixmap>
#include <QWidget>

#ifndef TCELLTYPE
#define TCELLTYPE
enum TCellType {
    EMPTY_CELL, ELECTRON_TAIL, ELECTRON_HEAD, CONDUCTOR
};
#endif

using TField = std::vector<std::vector<TCellType>>;

enum TMOUSE_MODE {
    DRAW, MOVE
};

class FieldArea : public QWidget {
Q_OBJECT
public:
    FieldArea(size_t width, size_t height, size_t cellSize, QWidget *parent = nullptr);

    QSize minimumSizeHint() const override;

    QSize sizeHint() const override;

    void setColor(TCellType cond) {
        drawCellType_ = cond;
    }

    void setField(TField &field) {
        cells_ = field;
    };

    TField &getField() {
        return cells_;
    }

    void setMouseMode(TMOUSE_MODE mode);

    bool setFieldFromFile(const std::string &fileName);

protected:
    void paintEvent(QPaintEvent *event) override;

    void wheelEvent(QWheelEvent* event) override;

    void mousePressEvent(QMouseEvent *event) override;

    void mouseMoveEvent(QMouseEvent *event) override;

    void mouseReleaseEvent(QMouseEvent* event) override;

private:
    size_t fwidth_ = 0;
    size_t fheight_ = 0;
    int coordX_ = 0;
    int coordY_ = 0;
    QPoint oldPos_;
    QPoint moveDelta_;
    TMOUSE_MODE mouseMode_ = DRAW;
    TCellType drawCellType_ = ELECTRON_TAIL;
    qreal scale_ = 1;
    size_t cellSize_ = 10;
    TField cells_ = {};

    void updateXY(int deltaX, int deltaY);
    void drawCell(QMouseEvent *event);
};

#endif //WIREWORLD2D_FIELDAREA_H
