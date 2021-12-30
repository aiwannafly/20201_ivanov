#include "FieldWidget.h"

#include <QPainter>
#include <QWheelEvent>

#include "RLE.h"

constexpr size_t minScale = 1;
constexpr size_t maxScale = 10;
constexpr int zoomCoef = 120;
constexpr char drawCursorName[] = "cursortarget.png";
constexpr size_t minWidth = 100;
constexpr size_t minHeight = 100;

FieldWidget::FieldWidget(size_t width, size_t height, size_t cellSizePx, QWidget *parent) :
        fwidth_(width), fheight_(height), cellSize_(cellSizePx), QWidget(parent) {
    setBackgroundRole(QPalette::AlternateBase);
    setAutoFillBackground(false);
    field_ = new TField(height, width);
    this->setCursor(QCursor(QPixmap(drawCursorName)));
}

FieldWidget::FieldWidget(size_t width, size_t height, size_t cellSizePx, TField *field,
                         QWidget *parent): fwidth_(width), fheight_(height), cellSize_(cellSizePx),
        field_(field), QWidget(parent)  {
    setBackgroundRole(QPalette::AlternateBase);
    setAutoFillBackground(false);
    this->setCursor(QCursor(QPixmap(drawCursorName)));
}

QSize FieldWidget::minimumSizeHint() const {
    return {minWidth, minHeight};
}

QSize FieldWidget::sizeHint() const {
    size_t widthPx = fwidth_ * cellSize_;
    size_t heightPx = fheight_ * cellSize_;
    return {static_cast<int>(widthPx), static_cast<int>(heightPx)};
}

QColor getCellColor(enum TCell cond) {
    switch (cond) {
        case ELECTRON_TAIL:
            return Qt::white;
        case ELECTRON_HEAD:
            return Qt::blue;
        case CONDUCTOR:
            return {255, 153, 51}; //orange
        default:
            return {32, 32, 32}; //gray
    }
}

void FieldWidget::disableDrawing() {
    drawON_ = false;
}

void FieldWidget::enableDrawing() {
    drawON_ = true;
}

void FieldWidget::drawCell(QMouseEvent *event) {
    if (mouseMode_ != DRAW || !drawON_) {
        return;
    }
    size_t y = static_cast<size_t>(event->pos().y() / (static_cast<double>(cellSize_) * scale_));
    size_t x = static_cast<size_t>(event->pos().x() / (static_cast<double>(cellSize_) * scale_));
    if (x + coordX_ >= fwidth_) return;
    if (y + coordY_ >= fheight_) return;
    field_->set(x + coordY_, y + coordX_, drawCellType_);
}

void FieldWidget::setMouseMode(TMOUSE_MODE mode) {
    QCursor cursor;
    if (mode == DRAW) {
        cursor = QCursor(QPixmap(drawCursorName));
    } else {
        cursor = QCursor(Qt::PointingHandCursor);
    }
    this->setCursor(cursor);
    mouseMode_ = mode;
}

void FieldWidget::mousePressEvent(QMouseEvent *event) {
    if (mouseMode_ == DRAW) {
        drawCell(event);
        update();
        return;
    }
    this->setCursor(Qt::ClosedHandCursor);
    if (event->button() == Qt::LeftButton) {
        oldPos_ = event->pos();
    }
}

void FieldWidget::updateXY(int deltaX, int deltaY) {
    if (coordX_ + deltaX < 0) {
        coordX_ = 0;
    } else if (coordX_ + deltaX > fwidth_ - fwidth_ / scale_) {
        coordX_ = fwidth_ - fwidth_ / scale_;
    } else {
        coordX_ += deltaX;
    }
    if (coordY_ + deltaY < 0) {
        coordY_ = 0;
    } else if (coordY_ + deltaY > fheight_ - fheight_ / scale_) {
        coordY_ = fheight_ - fheight_ / scale_;
    } else {
        coordY_ += deltaY;
    }
}

void FieldWidget::mouseMoveEvent(QMouseEvent *event) {
    if (mouseMode_ != DRAW) {
        return;
    }
    drawCell(event);
    update();
}

void FieldWidget::mouseReleaseEvent(QMouseEvent *event) {
    if (mouseMode_ != MOVE) {
        return;
    }
    this->setCursor(Qt::PointingHandCursor);
    moveDelta_ = event->pos() - oldPos_;
    int deltaX = -moveDelta_.x() / (cellSize_ * scale_);
    int deltaY = -moveDelta_.y() / (cellSize_ * scale_);
    updateXY(deltaY, deltaX);
    update();
}

void FieldWidget::paintEvent(QPaintEvent *event) {
    QPainter painter(this);
    painter.scale(scale_, scale_);
    painter.setPen(QPen(lineColor_));
    size_t scaledWidth = qIntCast((fwidth_ / scale_));
    size_t scaledHeight = qIntCast((fheight_ / scale_));
    for (size_t i = 0; i < scaledHeight; i++) {
        if (i + coordY_ >= fheight_) {
            continue;
        }
        for (size_t j = 0; j < scaledWidth; j++) {
            if (j + coordX_ >= fwidth_) {
                continue;
            }
            painter.setBrush(getCellColor(field_->get(i + coordY_, j + coordX_)));
            painter.drawRect(i * cellSize_, j * cellSize_,
                             cellSize_, cellSize_);
        }
    }
}

void FieldWidget::setColor(TCell cond) {
    drawCellType_ = cond;
}

void FieldWidget::setField(TField *field) {
    field_ = field;
};

TField *FieldWidget::getField() {
    return field_;
}

void FieldWidget::wheelEvent(QWheelEvent *event) {
    scale_ += (event->angleDelta().y() / zoomCoef);
    if (scale_ < minScale) {
        scale_ = minScale;
    } else if (scale_ > maxScale) {
        scale_ = maxScale;
    }
    this->update();
}

bool FieldWidget::setFieldFromFile(const std::string &fileName) {
    int width = static_cast<int>(fwidth_);
    int height = static_cast<int>(fheight_);
    bool status = getFieldFromFile(fileName, field_, fwidth_, fheight_,
                                   width, height);
    this->update();
    if (!status) {
        return false;
    }
    return true;
}
