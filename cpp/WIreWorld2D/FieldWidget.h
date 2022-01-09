#ifndef WIREWORLD2D_FIELDWIDGET_H
#define WIREWORLD2D_FIELDWIDGET_H

#include <memory>
#include <QBrush>
#include <QPen>
#include <QPixmap>
#include <QWidget>

#include "GameLifeQt.h"
#include "WireWorldQt.h"

class FieldWidget : public QWidget {
Q_OBJECT
public:
    enum class drawMode {
        DRAW, ERASE, NO_DRAW
    };

    FieldWidget(size_t width, size_t height, size_t cellSizePx, QWidget *parent = nullptr);

    [[nodiscard]] QSize minimumSizeHint() const override;

    [[nodiscard]] QSize sizeHint() const override;

    void setColor(QColor color);

    bool setGame(const std::string &gameName);

    bool setFieldFromFile(const std::string &fileName);

    void setDrawMode(drawMode mode);

    drawMode getDrawMode() const;

    void clear();

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
        int col;
        int row;
    } leftTop_ = {0, 0};
    std::unique_ptr<GameQt> game_ = nullptr;
    drawMode mode_;
    size_t width_ = 0;
    size_t height_ = 0;
    QPoint oldPos_;
    QPoint moveDelta_;
    qreal scale_ = 1;
    size_t cellSizePx_ = 10;
    QColor drawColor_;
    QColor lineColor_ = {33, 33, 33};

    void updateLeftTop(int deltaCol, int deltaRow);
    void drawCell(double eventX, double eventY);
    bool checkFieldCoords(size_t row, size_t col) const;
};

#endif //WIREWORLD2D_FIELDWIDGET_H
