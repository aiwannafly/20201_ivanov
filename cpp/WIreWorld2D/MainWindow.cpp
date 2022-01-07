#include "MainWindow.h"

#include <QGridLayout>
#include <QtWidgets>

#include "FieldWidget.h"

namespace {
    constexpr char kRunIconName[] = "icons/run.png";
    constexpr char kStopIconName[] = "icons/stop.png";

    constexpr char kRunButtonText[] = "&Run";
    constexpr char kStopButtonText[] = "&Stop";
    constexpr char kNextButtonText[] = "&Next";

    constexpr char kLoadFieldButtonText[] = "&Load field";
    constexpr char kClearFieldButtonText[] = "&Clear field";

    constexpr char kEmptyFieldName[] = "fields/field_empty.rle";

    constexpr size_t kTimeUntilStart = 200;
    constexpr size_t kSquareCellSizePx = 10;
    constexpr size_t kFieldHeight = 400; // squares
    constexpr size_t kFieldWidth = 400;  // squares
    constexpr size_t kDefaultInterval = 250;

    constexpr char kMainWindowName[] = "CELLULAR AUTOMATION 2D";
}

void MainWindow::initButtons() {
    runButton_ = new QPushButton(QIcon(kRunIconName), kRunButtonText);
    connect(runButton_, SIGNAL (released()), this, SLOT (handleRunButton()));
    loadFieldButton_ = new QPushButton(kLoadFieldButtonText);
    connect(loadFieldButton_, SIGNAL (released()), this, SLOT (handleLoadFieldButton()));
    clearFieldButton_ = new QPushButton(kClearFieldButtonText);
    connect(clearFieldButton_, SIGNAL (released()), this, SLOT (handleClearButton()));
    nextButton_ = new QPushButton(kNextButtonText);
    connect(nextButton_, SIGNAL (released()), this, SLOT (handleNextButton()));
}

void MainWindow::initColorComboBox() {
    if (!fieldWidget_) {
        return;
    }
    std::vector<QColor> colors = fieldWidget_->getColors();
    auto icons = std::vector<QPixmap>(
            colors.size(),QPixmap(kSquareCellSizePx, kSquareCellSizePx));
    colorComboBox_ = new QComboBox;
    for (size_t i = 0; i < colors.size(); i++) {
        QPainter painter(&icons[i]);
        painter.fillRect(0, 0, kSquareCellSizePx, kSquareCellSizePx, colors[i]);
        std::string label = std::string("Color ") + std::to_string(i + 1);
        colorComboBox_->addItem(QIcon(icons[i]), tr(label.data()));
    }
    colorLabel_ = new QLabel(tr("&Set cell to draw:"));
    colorLabel_->setBuddy(colorComboBox_);
    connect(colorComboBox_, SIGNAL(activated(int)), this, SLOT(colorChanged()));
}

void MainWindow::initSpeedComboBox() {
    speedComboBox_ = new QComboBox;
    speedComboBox_->addItem(tr("x1"));
    speedComboBox_->addItem(tr("x0.5"));
    speedComboBox_->addItem(tr("x1.5"));
    speedComboBox_->addItem(tr("x2"));
    speedComboBox_->addItem(tr("x8"));
    speedLabel_ = new QLabel(tr("&Set speed"));
    speedLabel_->setBuddy(speedComboBox_);
    connect(speedComboBox_, SIGNAL(activated(int)), this, SLOT(speedChanged()));
}

MainWindow::MainWindow() {
    fieldWidget_ = new FieldWidget(kFieldWidth, kFieldHeight, kSquareCellSizePx);
    initButtons();
    initColorComboBox();
    initSpeedComboBox();
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
    mainLayout->addWidget(colorLabel_, 2, 0, Qt::AlignLeft);
    mainLayout->addWidget(colorComboBox_, 2, 0);
    mainLayout->addWidget(speedLabel_, 2, 3, Qt::AlignLeft);
    mainLayout->addWidget(speedComboBox_, 2, 3);
    setLayout(mainLayout);
    setWindowTitle(tr(kMainWindowName));
}

void MainWindow::handleLoadFieldButton() {
    QString qfileName = QFileDialog::getOpenFileName(this, tr("Open field"),
                                                     "/", tr("VectorField Files (*.rle)"));
    std::string fileName = qfileName.toStdString();
    if (!fieldWidget_->setFieldFromFile(fileName)) {
        QMessageBox::warning(this, "Error occurred", "Could not load field from the chosen file. Check if it can"
                                                     "not be opened or it does not have rle format.");
    }
    if (running_) {
        stopRunning();
    }
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
    auto colors = fieldWidget_->getColors();
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
    stopRunning();
}
