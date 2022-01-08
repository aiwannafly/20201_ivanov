#include "FieldWidget.h"

#include <QPainter>
#include <QWheelEvent>

#include "Factory.h"

namespace {
    constexpr char kDrawCursorName[] = "icons/cursortarget.png";
    constexpr size_t kMinScale = 1;
    constexpr size_t kMaxScale = 10;
    constexpr int kZoomCoef = 120;
    constexpr size_t kMinWidth = 100;
    constexpr size_t kMinHeight = 100;

    const QColor GRAY = {11, 11, 11};
    const auto kPointCursor = Qt::PointingHandCursor;
    const auto kCaptureCursor = Qt::ClosedHandCursor;
}

FieldWidget::FieldWidget(size_t width, size_t height, size_t cellSizePx, QWidget *parent) :
        fieldWidth_(width), fieldHeight_(height), lengthOfSquarePx_(cellSizePx), QWidget(parent) {
    QPalette palette;
    palette.setColor(QPalette::Window, GRAY);
    setPalette(palette);
    setAutoFillBackground(true);
    setGame("Game Life");
    assert(game_);
    this->setCursor(QCursor(QPixmap(kDrawCursorName)));
}

QSize FieldWidget::minimumSizeHint() const {
    return {kMinWidth, kMinHeight};
}

QSize FieldWidget::sizeHint() const {
    int widthPx = static_cast<int>(fieldWidth_ * lengthOfSquarePx_);
    int heightPx = static_cast<int>(fieldHeight_ * lengthOfSquarePx_);
    return {widthPx, heightPx};
}

void FieldWidget::updateGameField() {
    game_->proceedTick();
}

std::vector<QColor> FieldWidget::getColors() {
    return game_->getColors();
}

void FieldWidget::disableDrawing() {
    drawON_ = false;
}

void FieldWidget::enableDrawing() {
    drawON_ = true;
}

bool FieldWidget::setGame(const std::string &gameName) {
    GameQt *game = Factory<GameQt, std::string, size_t, size_t>::getInstance()
    ->createProduct(gameName, fieldHeight_, fieldWidth_);
    if (!game) {
        return false;
    }
    game_ = game;
    return true;
}

void FieldWidget::updateXY(int deltaX, int deltaY) {
    if (leftTop_.x + deltaX < 0) {
        leftTop_.x = 0;
    } else if (leftTop_.x + deltaX > fieldWidth_ - fieldWidth_ / scale_) {
        leftTop_.x = fieldWidth_ - fieldWidth_ / scale_;
    } else {
        leftTop_.x += deltaX;
    }
    if (leftTop_.y + deltaY < 0) {
        leftTop_.y = 0;
    } else if (leftTop_.y + deltaY > fieldHeight_ - fieldHeight_ / scale_) {
        leftTop_.y = fieldHeight_ - fieldHeight_ / scale_;
    } else {
        leftTop_.y += deltaY;
    }
}

void FieldWidget::drawCell(size_t eventX, size_t eventY) {
    if (!drawON_) {
        return;
    }
    size_t y = static_cast<size_t>(eventY /
            (static_cast<double>(lengthOfSquarePx_) * scale_));
    size_t x = static_cast<size_t>(eventX /
            (static_cast<double>(lengthOfSquarePx_) * scale_));
    if (x + leftTop_.x >= fieldWidth_) {
        return;
    }
    if (y + leftTop_.y >= fieldHeight_) {
        return;
    }
    game_->set(x + leftTop_.y, y + leftTop_.x, game_->getCellType(drawColor_));
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

int sign(int n) {
    if (n >= 0) {
        return 1;
    }
    return -1;
}

void FieldWidget::mouseMoveEvent(QMouseEvent *event) {
    moveDelta_ = event->pos() - oldPos_;
    if (event->buttons() != Qt::LeftButton) {
        int deltaX = -moveDelta_.x() / (lengthOfSquarePx_ * scale_);
        int deltaY = -moveDelta_.y() / (lengthOfSquarePx_ * scale_);
        if (moveCounter_ == scale_) {
            updateXY(sign(deltaY), sign(deltaX));
            update();
            moveCounter_ = 0;
        }
        moveCounter_++;
        return;
    }
    drawCell(event->position().x() + sign(moveDelta_.x() * scale_ * lengthOfSquarePx_),
             event->position().y() + sign(moveDelta_.y() * scale_ * lengthOfSquarePx_));
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
    size_t scaledWidth = qIntCast(fieldWidth_ / scale_);
    size_t scaledHeight = qIntCast(fieldHeight_ / scale_);
    size_t widthPx = lengthOfSquarePx_ * fieldWidth_;
    size_t heightPx = lengthOfSquarePx_ * fieldHeight_;
    for (size_t i = 0; i < scaledHeight; i++) {
        if (i + leftTop_.y >= fieldHeight_) {
            continue;
        }
        painter.drawLine(0, i * lengthOfSquarePx_, widthPx, i * lengthOfSquarePx_);
        painter.drawLine(i * lengthOfSquarePx_, 0, i * lengthOfSquarePx_, heightPx);
        for (size_t j = 0; j < scaledWidth; j++) {
            if (j + leftTop_.x >= fieldWidth_) {
                continue;
            }
            if (game_->getCellColor(game_->get(i + leftTop_.y, j + leftTop_.x)) == Qt::black) {
                continue;
            }
            painter.setBrush(game_->getCellColor(game_->get(i + leftTop_.y, j + leftTop_.x)));
            painter.drawRect(i * lengthOfSquarePx_, j * lengthOfSquarePx_,
                             lengthOfSquarePx_, lengthOfSquarePx_);
        }
    }
}

void FieldWidget::setColor(QColor color) {
    drawColor_ = color;
}

void FieldWidget::wheelEvent(QWheelEvent *event) {
    scale_ += static_cast<double>(event->angleDelta().y()) / kZoomCoef;
    if (scale_ < kMinScale) {
        scale_ = kMinScale;
    } else if (scale_ > kMaxScale) {
        scale_ = kMaxScale;
    }
    this->update();
}

bool FieldWidget::setFieldFromFile(const std::string &fileName) {
    return game_->setFieldFromFile(fileName);
}
