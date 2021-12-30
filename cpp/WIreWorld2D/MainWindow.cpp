#include "MainWindow.h"

#include <QGridLayout>
#include <QtWidgets>

#include "FieldWidget.h"
#include "Runner.h"

namespace {
    constexpr char kRunIconName[] = "run.png";
    constexpr char kStopIconName[] = "stop.png";
    constexpr char kMoveIconName[] = "move.jpg";
    constexpr char kPaintIconName[] = "paint.png";
    constexpr char kRunButtonText[] = "&Run";
    constexpr char kStopButtonText[] = "&Stop";
    constexpr char kPaintButtonText[] = "&Paint";
    constexpr char kMoveButtonText[] = "&Move";
    constexpr char kNextButtonText[] = "&Next";
    constexpr char kLoadFieldButtonText[] = "&Load field";
    constexpr char kClearFieldButtonText[] = "&Clear field";
    constexpr char kEmptyFieldName[] = "field_empty.rle";
    constexpr size_t kTimeUntilStart = 200;
    constexpr size_t kSquareCellSizePx = 10;
    constexpr size_t kFieldHeight = 80; // squares
    constexpr size_t kFieldWidth = 80;  // squares
    constexpr size_t kDefaultInterval = 250;
    constexpr char kMainWindowName[] = "WIREWORLD 2D";
}

MainWindow::MainWindow() {
    field_ = new TField(kFieldHeight, kFieldWidth);
    fieldWidget_ = new FieldWidget(kFieldWidth, kFieldHeight, kSquareCellSizePx,
                                   field_);
    runner_ = new Runner(kFieldWidth, kFieldHeight, field_);
    runButton_ = new QPushButton(QIcon(kRunIconName), kRunButtonText);
    loadFieldButton_ = new QPushButton(kLoadFieldButtonText);
    clearFieldButton_ = new QPushButton(kClearFieldButtonText);
    nextButton_ = new QPushButton(kNextButtonText);
    paintButton_ = new QPushButton(QIcon(kPaintIconName), kPaintButtonText);
    moveButton_ = new QPushButton(QIcon(kMoveIconName), kMoveButtonText);
    stepsLabel_ = new QLabel((std::string("Step №: ") +
                              std::to_string(runner_->getCountOfSteps())).data());
    colorComboBox_ = new QComboBox;
    colorComboBox_->addItem(tr("Empty"));
    colorComboBox_->addItem(tr("Electron tail"));
    colorComboBox_->addItem(tr("Electron head"));
    colorComboBox_->addItem(tr("Conductor"));
    colorLabel_ = new QLabel(tr("&Set cell to draw:"));
    colorLabel_->setBuddy(colorComboBox_);
    speedComboBox_ = new QComboBox;
    speedComboBox_->addItem(tr("x1"));
    speedComboBox_->addItem(tr("x0.5"));
    speedComboBox_->addItem(tr("x1.5"));
    speedComboBox_->addItem(tr("x2"));
    speedComboBox_->addItem(tr("x8"));
    speedLabel_ = new QLabel(tr("&Set speed"));
    speedLabel_->setBuddy(speedComboBox_);
    auto *mainLayout = new QGridLayout;
    runGameTimer_ = new QTimer(this);
    connect(runGameTimer_, &QTimer::timeout, this, QOverload<>::of(&MainWindow::getNext));
    runGameTimer_->setInterval(500);
    mainLayout->setColumnStretch(0, 1);
    mainLayout->setColumnStretch(2, 1);
    mainLayout->addWidget(fieldWidget_, 0, 0, 1, 5);
    mainLayout->addWidget(runButton_, 1, 0, Qt::AlignLeft);
    mainLayout->addWidget(nextButton_, 1, 1, Qt::AlignLeft);
    mainLayout->addWidget(loadFieldButton_, 1, 2, Qt::AlignLeft);
    mainLayout->addWidget(clearFieldButton_, 1, 3, Qt::AlignLeft);
    mainLayout->addWidget(stepsLabel_, 1, 4, Qt::AlignLeft);
    mainLayout->addWidget(colorLabel_, 2, 0, Qt::AlignLeft);
    mainLayout->addWidget(colorComboBox_, 2, 0);
    mainLayout->addWidget(paintButton_, 2, 1, Qt::AlignLeft);
    mainLayout->addWidget(moveButton_, 2, 2, Qt::AlignLeft);
    mainLayout->addWidget(speedLabel_, 2, 3, Qt::AlignLeft);
    mainLayout->addWidget(speedComboBox_, 2, 3);
    connect(colorComboBox_, SIGNAL(activated(int)), this, SLOT(colorChanged()));
    connect(speedComboBox_, SIGNAL(activated(int)), this, SLOT(speedChanged()));
    connect(loadFieldButton_, SIGNAL (released()), this, SLOT (handleLoadFieldButton()));
    connect(runButton_, SIGNAL (released()), this, SLOT (handleRunButton()));
    connect(nextButton_, SIGNAL (released()), this, SLOT (handleNextButton()));
    connect(clearFieldButton_, SIGNAL (released()), this, SLOT (handleClearButton()));
    connect(moveButton_, SIGNAL (released()), this, SLOT (handleMoveButton()));
    connect(paintButton_, SIGNAL (released()), this, SLOT (handleDrawButton()));
    setLayout(mainLayout);
    setWindowTitle(tr(kMainWindowName));
}

void MainWindow::handleLoadFieldButton() {
    QString qfileName = QFileDialog::getOpenFileName(this, tr("Open field"),
                                                     "/", tr("Field Files (*.rle)"));
    std::string fileName = qfileName.toStdString();
    if (!fieldWidget_->setFieldFromFile(fileName)) {
        QMessageBox::warning(this, "Error occurred", "Could not load field from the chosen file. Check if it can"
                                                     "not be opened or it does not have rle format.");
    }
    if (running_) {
        stopRunning();
    }
    runner_->clearSteps();
}

void MainWindow::speedChanged() {
    int speedId = speedComboBox_->currentIndex();
    double interval = kDefaultInterval;
    if (0 == speedId) {
        interval *= 1;
    } else if (1 == speedId) {
        interval *= 2;
    } else if (2 == speedId) {
        interval *= 0.75;
    } else if (3 == speedId) {
        interval /= 2;
    } else {
        interval /= 4;
    }
    runGameTimer_->setInterval(static_cast<int>(interval));
}

void MainWindow::colorChanged() {
    int colorId = colorComboBox_->currentIndex();
    fieldWidget_->setColor(static_cast<TCell>(colorId));
}

void MainWindow::getNext() {
    runner_->proceedTick();
    stepsLabel_->setText((std::string("Step №: ") +
                          std::to_string(runner_->getCountOfSteps())).data());
    fieldWidget_->update();
}

void MainWindow::handleNextButton() {
    getNext();
}

void MainWindow::stopRunning() {
    fieldWidget_->enableDrawing();
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
    fieldWidget_->disableDrawing();
    runGameTimer_->start(kTimeUntilStart);
    runButton_->setText(kStopButtonText);
    runButton_->setIcon(QIcon(kStopIconName));
}

void MainWindow::handleClearButton() {
    fieldWidget_->setFieldFromFile(kEmptyFieldName);
    runner_->clearSteps();
    stopRunning();
    stepsLabel_->setText("Step №: 0");
}

void MainWindow::handleDrawButton() {
    fieldWidget_->setMouseMode(DRAW);
}

void MainWindow::handleMoveButton() {
    fieldWidget_->setMouseMode(MOVE);
}
