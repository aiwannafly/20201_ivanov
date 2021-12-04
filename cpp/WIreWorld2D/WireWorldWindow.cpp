#include "WireWorldWindow.h"

#include <QGridLayout>
#include <QtWidgets>

#include "FieldArea.h"

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
constexpr size_t timeUntilStart = 200;

WireWorldWindow::WireWorldWindow() {
    fieldArea_ = new FieldArea;
    runButton_ = new QPushButton(QIcon(runIconName), runButtonText);
    loadFieldButton_ = new QPushButton(loadFieldButtonText);
    clearFieldButton_ = new QPushButton(clearFieldButtonText);
    nextButton_ = new QPushButton(nextButtonText);
    paintButton_ = new QPushButton(QIcon(paintIconName), paintButtonText);
    moveButton_ = new QPushButton(QIcon(moveIconName), moveButtonText);
    stepsLabel_ = new QLabel((std::string("Step №: ") +
                              std::to_string(fieldArea_->getSteps())).data());
    colorComboBox_ = new QComboBox;
    colorComboBox_->addItem(tr("Empty"));
    colorComboBox_->addItem(tr("Electron tail"));
    colorComboBox_->addItem(tr("Electron head"));
    colorComboBox_->addItem(tr("Conductor"));
    colorLabel_ = new QLabel(tr("&Set cell to draw:"));
    colorLabel_->setBuddy(colorComboBox_);

    auto *mainLayout = new QGridLayout;
    timer_ = new QTimer(this);
    connect(timer_, &QTimer::timeout, this, QOverload<>::of(&FieldArea::update));
    timer_->start(timeUntilStart);
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
    connect(colorComboBox_, SIGNAL(activated(int)),
            this, SLOT(colorChanged()));

    connect(loadFieldButton_, SIGNAL (released()), this, SLOT (handleLoadFieldButton()));
    connect(runButton_, SIGNAL (released()), this, SLOT (handleRunButton()));
    connect(nextButton_, SIGNAL (released()), this, SLOT (handleNextButton()));
    connect(clearFieldButton_, SIGNAL (released()), this, SLOT (handleClearButton()));
    connect(moveButton_, SIGNAL (released()), this, SLOT (handleMoveButton()));
    connect(paintButton_, SIGNAL (released()), this, SLOT (handleDrawButton()));
    setLayout(mainLayout);
    setWindowTitle(tr("WIREWORLD 2D"));
}

void WireWorldWindow::handleLoadFieldButton() {
    QString qfileName = QFileDialog::getOpenFileName(this, tr("Open field"),
                                                     "/home/zander/desktop/prog_labs/first_semester/WireWorld",
                                                     tr("Field Files (*.rle)"));
    std::string fileName = qfileName.toStdString();
    if (!fieldArea_->setField(fileName)) {
        QMessageBox::warning(this, "Error occurred", "Could not load field from the chosen file. Check if it can"
                                                   "not be opened or it does not have rle format.");
    }
}

void WireWorldWindow::colorChanged() {
    int colorId = colorComboBox_->currentIndex();
    fieldArea_->setColor(static_cast<conditions>(colorId));
}

void WireWorldWindow::handleNextButton() {
    fieldArea_->proceedTick();
    stepsLabel_->setText((std::string("Step №: ") +
                         std::to_string(fieldArea_->getSteps())).data());
}

void WireWorldWindow::handleRunButton() {
    if (fieldArea_->isRun()) {
        runButton_->setText(runButtonText);
        runButton_->setIcon(QIcon(runIconName));
        fieldArea_->stop();
        stepsLabel_->setText((std::string("Step №: ") +
                             std::to_string(fieldArea_->getSteps())).data());
        return;
    }
    fieldArea_->run();
    runButton_->setText(stopButtonText);
    runButton_->setIcon(QIcon(stopIconName));
}

void WireWorldWindow::handleClearButton() {
    fieldArea_->setField("field_empty.rle");
    stepsLabel_->setText("Step №: 0");
}

void WireWorldWindow::handleDrawButton() {
    fieldArea_->setMouseMode(DRAW);
}

void WireWorldWindow::handleMoveButton() {
    fieldArea_->setMouseMode(MOVE);
}
