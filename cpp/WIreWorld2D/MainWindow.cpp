#include "MainWindow.h"

#include <QGridLayout>
#include <QtWidgets>

#include "FieldArea.h"
#include "Runner.h"

constexpr char runIconName[] = "run.png";
constexpr char stopIconName[] = "stop.png";
constexpr char moveIconName[] = "move.jpg";
constexpr char paintIconName[] = "paint.png";
constexpr char runButtonText[] = "&Run";
constexpr char stopButtonText[] = "&Stop";
constexpr char paintButtonText[] = "&Paint";
constexpr char moveButtonText[] = "&Move";
constexpr char nextButtonText[] = "&Next";
constexpr char loadFieldButtonText[] = "&Load field";
constexpr char clearFieldButtonText[] = "&Clear field";
constexpr char emptyFieldName[] = "field_empty.rle";
constexpr size_t timeUntilStart = 200;
constexpr size_t cellSize = 10; //px
constexpr size_t fieldHeight = 80;
constexpr size_t fieldWidth = 80;

MainWindow::MainWindow() {
    fieldArea_ = new FieldArea(fieldWidth, fieldHeight, cellSize);
    runner_ = new Runner(fieldWidth, fieldHeight);
    runButton_ = new QPushButton(QIcon(runIconName), runButtonText);
    loadFieldButton_ = new QPushButton(loadFieldButtonText);
    clearFieldButton_ = new QPushButton(clearFieldButtonText);
    nextButton_ = new QPushButton(nextButtonText);
    paintButton_ = new QPushButton(QIcon(paintIconName), paintButtonText);
    moveButton_ = new QPushButton(QIcon(moveIconName), moveButtonText);
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
    mainLayout->addWidget(fieldArea_, 0, 0, 1, 5);
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
    setWindowTitle(tr("WireWorld 2D"));
}

void MainWindow::handleLoadFieldButton() {
    QString qfileName = QFileDialog::getOpenFileName(this, tr("Open field"),
                                                     "/home/zander/desktop/prog_labs/first_semester/WireWorld",
                                                     tr("Field Files (*.rle)"));
    std::string fileName = qfileName.toStdString();
    if (!fieldArea_->setFieldFromFile(fileName)) {
        QMessageBox::warning(this, "Error occurred", "Could not load field from the chosen file. Check if it can"
                                                     "not be opened or it does not have rle format.");
    }
    runner_->clearSteps();
}

void MainWindow::speedChanged() {
    int speedId = speedComboBox_->currentIndex();
    if (0 == speedId) {
        runGameTimer_->setInterval(1000);
    } else if (1 == speedId) {
        runGameTimer_->setInterval(2000);
    } else if (2 == speedId) {
        runGameTimer_->setInterval(750);
    } else if (3 == speedId) {
        runGameTimer_->setInterval(500);
    } else {
        runGameTimer_->setInterval(125);
    }
}

void MainWindow::colorChanged() {
    int colorId = colorComboBox_->currentIndex();
    fieldArea_->setColor(static_cast<TCellType>(colorId));
}

void MainWindow::getNext() {
    runner_->setField(fieldArea_->getField());
    runner_->proceedTick();
    stepsLabel_->setText((std::string("Step №: ") +
                          std::to_string(runner_->getCountOfSteps())).data());
    fieldArea_->setField(runner_->getField());
    fieldArea_->update();
}

void MainWindow::handleNextButton() {
    getNext();
}

void MainWindow::handleRunButton() {
    if (running_) {
        runGameTimer_->stop();
        runButton_->setText(runButtonText);
        runButton_->setIcon(QIcon(runIconName));
        running_ = false;
        stepsLabel_->setText((std::string("Step №: ") +
                              std::to_string(runner_->getCountOfSteps())).data());
        return;
    }
    running_ = true;
    runGameTimer_->start(timeUntilStart);
    runButton_->setText(stopButtonText);
    runButton_->setIcon(QIcon(stopIconName));
}

void MainWindow::handleClearButton() {
    fieldArea_->setFieldFromFile(emptyFieldName);
    runner_->clearSteps();
    stepsLabel_->setText("Step №: 0");
}

void MainWindow::handleDrawButton() {
    fieldArea_->setMouseMode(DRAW);
}

void MainWindow::handleMoveButton() {
    fieldArea_->setMouseMode(MOVE);
}
