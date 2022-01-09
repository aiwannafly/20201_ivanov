#include "MainWindow.h"

#include <QGridLayout>
#include <QtWidgets>

#include "GamesIDs.h"
#include "FieldWidget.h"

namespace {
    constexpr char kRunIconName[] = "icons/run.png";
    constexpr char kStopIconName[] = "icons/stop.png";
    constexpr char kEraseIconName[] = "icons/eraser.png";
    constexpr char kClearIconName[] = "icons/clear.png";
    constexpr char kLoadIconName[] = "icons/load.png";
    constexpr char kNextIconName[] = "icons/next.png";
    constexpr char kSpeedIconName[] = "icons/speed.png";
    constexpr char kWireWorldIconName[] = "icons/wireworld.png";
    constexpr char kGameLifeIconName[] = "icons/gamelife.png";
    constexpr char kGameIconName[] = "icons/game.png";

    constexpr char kRunButtonText[] = "&Run";
    constexpr char kStopButtonText[] = "&Stop";
    constexpr char kNextButtonText[] = "&Next";
    constexpr char kEraseButtonText[] = "&Erase";

    constexpr char kLoadFieldButtonText[] = "&Load field";
    constexpr char kClearFieldButtonText[] = "&Clear field";

    constexpr size_t kTimeUntilStart = 200;
    constexpr size_t kSquareCellSizePx = 10;
    constexpr size_t kFieldHeight = 120;
    constexpr size_t kFieldWidth = 160;
    constexpr size_t kDefaultInterval = 250;
    constexpr double kIntervals[] = {2, 1, 0.75, 0.5, 0.25};

    constexpr char kMainWindowName[] = "CELLULAR AUTOMATION 2D";
}

void MainWindow::initButtons() {
    runButton_ = QSharedPointer<QPushButton>(new QPushButton(QIcon(kRunIconName), kRunButtonText));
    connect(runButton_.get(), SIGNAL (released()), this, SLOT (handleRunButton()));
    loadFieldButton_ = QSharedPointer<QPushButton>(new QPushButton(QIcon(kLoadIconName), kLoadFieldButtonText));
    connect(loadFieldButton_.get(), SIGNAL (released()), this, SLOT (handleLoadFieldButton()));
    clearFieldButton_ = QSharedPointer<QPushButton>(new QPushButton(QIcon(kClearIconName), kClearFieldButtonText));
    connect(clearFieldButton_.get(), SIGNAL (released()), this, SLOT (handleClearButton()));
    nextButton_ = QSharedPointer<QPushButton>(new QPushButton(QIcon(kNextIconName), kNextButtonText));
    connect(nextButton_.get(), SIGNAL (released()), this, SLOT (handleNextButton()));
    eraseButton_ = QSharedPointer<QPushButton>(new QPushButton(QIcon(kEraseIconName), kEraseButtonText));
    connect(eraseButton_.get(), SIGNAL (released()), this, SLOT (handleEraseButton()));
}

void MainWindow::setColors() {
    if (!colorComboBox_) {
        return;
    }
    colorComboBox_->clear();
    std::vector<QColor> colors = fieldWidget_->getColors();
    auto icons = std::vector<QPixmap>(
            colors.size(),QPixmap(kSquareCellSizePx, kSquareCellSizePx));
    for (size_t i = 0; i < colors.size(); i++) {
        QPainter painter(&icons[i]);
        painter.fillRect(0, 0, kSquareCellSizePx, kSquareCellSizePx, colors[i]);
        std::string label = std::string("Color ") + std::to_string(i + 1);
        colorComboBox_->addItem(QIcon(icons[i]), tr(label.data()));
    }
}

void MainWindow::initColorComboBox() {
    if (!fieldWidget_) {
        return;
    }
    colorComboBox_ = QSharedPointer<QComboBox>(new QComboBox);
    setColors();
    connect(colorComboBox_.get(), SIGNAL(activated(int)), this, SLOT(colorChanged()));
}

void MainWindow::initSpeedComboBox() {
    speedComboBox_ = QSharedPointer<QComboBox>(new QComboBox);
    QIcon speedIcon = QIcon(kSpeedIconName);
    speedComboBox_->addItem(speedIcon, tr("x0.5"));
    speedComboBox_->addItem(speedIcon, tr("x1"));
    speedComboBox_->addItem(speedIcon, tr("x1.5"));
    speedComboBox_->addItem(speedIcon, tr("x2"));
    speedComboBox_->addItem(speedIcon, tr("x8"));
    connect(speedComboBox_.get(), SIGNAL(activated(int)), this, SLOT(speedChanged()));
}

void MainWindow::initGameComboBox() {
    gameComboBox_ = QSharedPointer<QComboBox>(new QComboBox);
    for (const std::string &name: GamesIDs::GAMES_NAMES) {
        auto icon = QIcon(kGameIconName);
        if (name == GamesIDs::WIREWORLD_ID) {
            icon = QIcon(kWireWorldIconName);
        } else if (name == GamesIDs::GAME_LIFE_ID) {
            icon = QIcon(kGameLifeIconName);
        }
        gameComboBox_->addItem(icon, tr(name.data()));
    }
    connect(gameComboBox_.get(), SIGNAL(activated(int)), this, SLOT(gameChanged()));
}

MainWindow::MainWindow() {
    fieldWidget_ = QSharedPointer<FieldWidget>(new FieldWidget(kFieldWidth, kFieldHeight, kSquareCellSizePx));
    initButtons();
    initColorComboBox();
    initSpeedComboBox();
    initGameComboBox();
    auto *mainLayout = new QGridLayout;
    runGameTimer_ = QSharedPointer<QTimer>(new QTimer(this));
    connect(runGameTimer_.get(), &QTimer::timeout, this, QOverload<>::of(&MainWindow::getNext));
    runGameTimer_->setInterval(kIntervals[1]);
    mainLayout->addWidget(fieldWidget_.get(), 0, 0, 1, 5);
    mainLayout->addWidget(runButton_.get(), 1, 0);
    mainLayout->addWidget(nextButton_.get(), 1, 1);
    mainLayout->addWidget(loadFieldButton_.get(), 1, 2);
    mainLayout->addWidget(clearFieldButton_.get(), 1, 3);
    mainLayout->addWidget(colorComboBox_.get(), 2, 0);
    mainLayout->addWidget(eraseButton_.get(), 2, 1);
    mainLayout->addWidget(speedComboBox_.get(), 2, 2);
    mainLayout->addWidget(gameComboBox_.get(), 2, 3);
    setLayout(mainLayout);
    setWindowTitle(tr(kMainWindowName));
}

void MainWindow::handleLoadFieldButton() {
    QString qfileName = QFileDialog::getOpenFileName(this, tr("Open field"),
                                                     "/", tr("VectorField Files (*.rle)"));
    std::string fileName = qfileName.toStdString();
    if (!fieldWidget_->setFieldFromFile(fileName)) {
        QMessageBox::warning(this, "Error occurred", "Could not load field from the chosen file_. Check if it can"
                                                     "not be opened or it does not have rle format.");
    }
    fieldWidget_->update();
    if (running_) {
        stopRunning();
    }
}

void MainWindow::gameChanged() {
    if (running_) {
        stopRunning();
    }
    fieldWidget_->clear();
    fieldWidget_->update();
    int gameId = gameComboBox_->currentIndex();
    bool status = fieldWidget_->setGame(GamesIDs::GAMES_NAMES[gameId]);
    if (!status) {
        QMessageBox::warning(this, "Error occurred", "Could not run the chosen game");
    }
    setColors();
}

void MainWindow::speedChanged() {
    int speedId = speedComboBox_->currentIndex();
    double interval = kDefaultInterval * kIntervals[speedId];
    runGameTimer_->setInterval(static_cast<int>(interval));
}

void MainWindow::colorChanged() {
    int colorId = colorComboBox_->currentIndex();
    auto colors = fieldWidget_->getColors();
    auto mode = fieldWidget_->getDrawMode();
    if (mode == FieldWidget::drawMode::NO_DRAW) {
        return;
    }
    fieldWidget_->setDrawMode(FieldWidget::drawMode::DRAW);
    fieldWidget_->setColor(colors[colorId]);
}

void MainWindow::getNext() {
    fieldWidget_->updateGameField();
    fieldWidget_->update();
}

void MainWindow::handleNextButton() {
    getNext();
}

void MainWindow::stopRunning() {
    fieldWidget_->setDrawMode(FieldWidget::drawMode::DRAW);
    runGameTimer_->stop();
    runButton_->setText(kRunButtonText);
    runButton_->setIcon(QIcon(kRunIconName));
    running_ = false;
}

void MainWindow::handleRunButton() {
    if (running_) {
        stopRunning();
        return;
    }
    running_ = true;
    fieldWidget_->setDrawMode(FieldWidget::drawMode::NO_DRAW);
    runGameTimer_->start(kTimeUntilStart);
    runButton_->setText(kStopButtonText);
    runButton_->setIcon(QIcon(kStopIconName));
}

void MainWindow::handleEraseButton() {
    auto mode = fieldWidget_->getDrawMode();
    if (mode == FieldWidget::drawMode::NO_DRAW) {
        return;
    }
    fieldWidget_->setDrawMode(FieldWidget::drawMode::ERASE);
}

void MainWindow::handleClearButton() {
    fieldWidget_->clear();
    fieldWidget_->update();
    stopRunning();
}
