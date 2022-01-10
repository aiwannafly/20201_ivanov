#include "FieldWidget.h"

#include <QPainter>
#include <QWheelEvent>

#include "GamesIDs.h"
#include "Factory.h"

namespace {
    constexpr char kDrawCursorName[] = "icons/cursortarget.png";
    constexpr size_t kMinScale = 1;
    constexpr size_t kMaxScale = 10;
    constexpr int kZoomCoef = 180;
    constexpr size_t kMinWidth = 100;
    constexpr size_t kMinHeight = 100;
    constexpr size_t kLightLineFreq = 10;

    const QColor LIGHT_GRAY = {66, 66, 66};
    const QColor GRAY = {11, 11, 11};
    const QColor EMPTY_COLOR = Qt::black;
    const auto kPointCursor = Qt::PointingHandCursor;
    const auto kCaptureCursor = Qt::ClosedHandCursor;

    int min(int a, int b) {
        if (a <= b) {
            return a;
        }
        return b;
    }

    int sign(int n) {
        if (n >= 0) {
            return 1;
        }
        return -1;
    }
}

FieldWidget::FieldWidget(size_t width, size_t height, size_t cellSizePx, QWidget *parent) :
        width_(width), height_(height), cellSizePx_(cellSizePx), QWidget(parent) {
    QPalette palette;
    palette.setColor(QPalette::Window, GRAY);
    setPalette(palette);
    setAutoFillBackground(true);
    setGame(GamesIDs::GAMES_NAMES[0]);
    assert(game_);
    drawColor_ = game_->getColors()[0];
    this->setCursor(QCursor(QPixmap(kDrawCursorName)));
}

QSize FieldWidget::minimumSizeHint() const {
    return {kMinWidth, kMinHeight};
}

QSize FieldWidget::sizeHint() const {
    int widthPx = width_ * cellSizePx_;
    int heightPx = height_ * cellSizePx_;
    return {widthPx, heightPx};
}

void FieldWidget::clear() {
    for (size_t row = 0; row < height_; row++) {
        for (size_t col = 0; col < width_; col++) {
            int emptyCell = game_->getCellType(EMPTY_COLOR);
            game_->set(row, col, emptyCell);
        }
    }
}

void FieldWidget::updateGameField() {
    game_->proceedTick();
}

std::vector<QColor> FieldWidget::getColors() {
    return game_->getColors();
}

void FieldWidget::setColor(QColor color) {
    if (mode_ == drawMode::ERASE) {
        return;
    }
    drawColor_ = color;
}

void FieldWidget::setDrawMode(drawMode mode) {
    mode_ = mode;
    if (mode_ == drawMode::ERASE) {
        drawColor_ = EMPTY_COLOR;
    } else {
        drawColor_ = game_->getColors()[0];
    }
}

bool FieldWidget::setGame(const std::string &gameName) {
    auto *game = Factory<GameQt, std::string, size_t, size_t>::getInstance()
                                            ->createProduct(gameName, height_, width_);
    if (!game) {
        return false;
    }
    game_ = std::unique_ptr<GameQt>(game);
    drawColor_ = game_->getColors()[0];
    return true;
}

FieldWidget::drawMode FieldWidget::getDrawMode() const {
    return mode_;
}

bool FieldWidget::checkFieldCoords(size_t row, size_t col) const {
    if (row >= 0 & row <= height_ &
        col >= 0 & col <= width_) {
        return true;
    }
    return false;
}

void FieldWidget::updateLeftTop(int deltaCol, int deltaRow) {
    size_t scaledWidth = qIntCast(width_ / scale_);
    size_t scaledHeight = qIntCast(height_ / scale_);
    coords rightBottom = {};
    rightBottom.col = min(leftTop_.col + scaledWidth, width_);
    rightBottom.row = min(leftTop_.row + scaledHeight, height_);
    if (leftTop_.col + deltaCol >= 0 & rightBottom.col + deltaCol <= width_) {
        leftTop_.col += deltaCol;
    }
    if (leftTop_.row + deltaRow >= 0 & rightBottom.row + deltaRow <= height_) {
        leftTop_.row += deltaRow;
    }
}

void FieldWidget::drawCell(double eventX, double eventY) {
    if (mode_ == drawMode::NO_DRAW) {
        return;
    }
    size_t row = leftTop_.row + (eventY / (cellSizePx_ * scale_));
    size_t col = leftTop_.col + (eventX / (cellSizePx_ * scale_));
    if (checkFieldCoords(row, col)) {
        game_->set(row, col, game_->getCellType(drawColor_));
    }
}

void FieldWidget::mousePressEvent(QMouseEvent *event) {
    oldPos_ = event->pos();
    if (event->buttons() == Qt::LeftButton) {
        this->setCursor(QCursor(QPixmap(kDrawCursorName)));
        drawCell(event->position().x(), event->position().y());
        update();
        return;
    }
    this->setCursor(kCaptureCursor);
}

void FieldWidget::mouseMoveEvent(QMouseEvent *event) {
    moveDelta_ = event->pos() - oldPos_;
    if (event->buttons() != Qt::LeftButton) {
        int deltaCol = -moveDelta_.x() / (cellSizePx_ * scale_ * 2);
        int deltaRow = -moveDelta_.y() / (cellSizePx_ * scale_ * 2);
        updateLeftTop(deltaCol, deltaRow);
        update();
        return;
    }
    drawCell(event->position().x() + sign(moveDelta_.x() * scale_ * cellSizePx_),
             event->position().y() + sign(moveDelta_.y() * scale_ * cellSizePx_));
    drawCell(event->position().x(), event->position().y());
    update();
}

void FieldWidget::mouseReleaseEvent(QMouseEvent *event) {
    if (event->buttons() == Qt::LeftButton) {
        return;
    }
    this->setCursor(kPointCursor);
}

void FieldWidget::paintEvent(QPaintEvent *event) {
    QPainter painter(this);
    painter.scale(scale_, scale_);
    painter.setPen(QPen(lineColor_));
    size_t scaledWidth = qIntCast(this->size().width() / (scale_ * cellSizePx_));
    size_t scaledHeight = qIntCast(this->size().height() / (scale_ * cellSizePx_));
    size_t widthPx = cellSizePx_ * scaledWidth;
    size_t heightPx = cellSizePx_ * scaledHeight;
    for (size_t row = 0; row < scaledHeight; row++) {
        if (row + leftTop_.row >= height_) {
            continue;
        }
        if ((row + leftTop_.row) % kLightLineFreq == 0) {
            painter.setPen(QPen(LIGHT_GRAY));
        }
        painter.drawLine(0, row * cellSizePx_, widthPx, row * cellSizePx_);
        painter.setPen(QPen(lineColor_));
        for (size_t col = 0; col < scaledWidth; col++) {
            if (row == 0) {
                if ((col + leftTop_.col) % kLightLineFreq == 0) {
                    painter.setPen(QPen(LIGHT_GRAY));
                }
                painter.drawLine(col * cellSizePx_, 0, col * cellSizePx_, heightPx);
                painter.setPen(QPen(lineColor_));
            }
            if (col + leftTop_.col >= width_) {
                continue;
            }
            if (game_->getCellColor(game_->get(row + leftTop_.row, col + leftTop_.col)) == EMPTY_COLOR) {
                continue;
            }
            painter.setBrush(game_->getCellColor(game_->get(row + leftTop_.row, col + leftTop_.col)));
            painter.drawRect(col * cellSizePx_, row * cellSizePx_,
                             cellSizePx_, cellSizePx_);
        }
    }
}

void FieldWidget::wheelEvent(QWheelEvent *event) {
    coords fieldPlace = {};
    double oldX = event->position().x();
    double oldY = event->position().y();
    fieldPlace.col = leftTop_.col + oldX / (scale_ * cellSizePx_);
    fieldPlace.row = leftTop_.row + oldY / (scale_ * cellSizePx_);
    scale_ += static_cast<double>(event->angleDelta().y()) / kZoomCoef;
    if (scale_ < kMinScale) {
        scale_ = kMinScale;
    } else if (scale_ > kMaxScale) {
        scale_ = kMaxScale;
    }
    size_t xOffset = oldX / (scale_ * cellSizePx_);
    size_t yOffset = oldY / (scale_ * cellSizePx_);
    if (fieldPlace.col >= xOffset) {
        leftTop_.col = fieldPlace.col - xOffset;
    }
    if (fieldPlace.row >= yOffset) {
        leftTop_.row = fieldPlace.row - yOffset;
    }
    size_t scaledWidth = qIntCast(this->size().width() / (scale_ * cellSizePx_));
    size_t scaledHeight = qIntCast(this->size().height() / (scale_ * cellSizePx_));
    if (leftTop_.col + scaledWidth >= width_) {
        leftTop_.col = width_ - scaledWidth;
    }
    if (leftTop_.row + scaledHeight >= height_) {
        leftTop_.row = height_ - scaledHeight;
    }
    this->update();
}

bool FieldWidget::setFieldFromFile(const std::string &fileName) {
    return game_->setFieldFromFile(fileName);
}
