#include "FieldArea.h"

#include <QPainter>
#include <QWheelEvent>
#include <iostream>

FieldArea::FieldArea(QWidget *parent) : QWidget(parent) {
    setBackgroundRole(QPalette::AlternateBase);
    setAutoFillBackground(false);
}

bool FieldArea::isRun() {
    return isRunning;
}

QSize FieldArea::minimumSizeHint() const {
    return QSize(100, 100);
}

QSize FieldArea::sizeHint() const {
    return QSize(fwidth * cellSize_, fheight * cellSize_);
}

Qt::GlobalColor getCellColor(enum conditions cond) {
    // turns enum type condition into char type
    switch (cond) {
        case ELECTRON_TAIL:
            return Qt::white;
        case ELECTRON_HEAD:
            return Qt::blue;
        case CONDUCTOR:
            return Qt::yellow;
        default:
            return Qt::darkGray;
    }
}

void FieldArea::mousePressEvent(QMouseEvent *event) {
//    if (event->button() == Qt::LeftButton) {
//        oldPos_ = event->pos();
//    }
}

void FieldArea::mouseMoveEvent(QMouseEvent *event) {
//    QPoint delta = event->pos() - oldPos_;
//    move(pos() + delta);
//    std::cout << "MOVE" << std::endl;
}

void FieldArea::run() {
    isRunning = true;
}

void FieldArea::stop() {
    isRunning = false;
}

bool FieldArea::proceedTick() {
    runner_.setField(cells_);
    bool changed = runner_.proceedTick();
    cells_ = runner_.getField();
    this->update();
    return changed;
}

bool FieldArea::setField(const std::string &fileName) {
    bool status = runner_.setField(fileName);
    if (status) {
        cells_ = runner_.getField();
        this->update();
        return true;
    }
    return false;
}

void FieldArea::paintEvent(QPaintEvent *event) {
    if (isRunning) {
        proceedTick();
    }
    QPainter painter(this);
    painter.scale(scale_, scale_);
    painter.setPen(QPen(Qt::black));
    for (size_t i = 0; i < fheight; i++) {
        for (size_t j = 0; j < fwidth; j++) {
            painter.setBrush(getCellColor(cells_[i][j]));
            painter.drawRect(i * cellSize_, j * cellSize_,
                             cellSize_, cellSize_);
        }
    }
}

void FieldArea::wheelEvent(QWheelEvent *event) {
    scale_ += (event->angleDelta().y() / 120);
    if (scale_ < 1) {
        scale_ = 1;
    } else if (scale_ > 10) {
        scale_ = 10;
    }
    this->update();
}
