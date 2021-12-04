#include "WireWorldWindow.h"

#include <QGridLayout>
#include <QtWidgets>

#include "FieldArea.h"

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

#include <iostream>

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
        runButton_->setText("&Run");
        fieldArea_->stop();
        stepsLabel_->setText((std::string("Step №: ") +
                             std::to_string(fieldArea_->getSteps())).data());
        return;
    }
    fieldArea_->run();
    runButton_->setText("&Stop");
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

WireWorldWindow::WireWorldWindow() {
    fieldArea_ = new FieldArea;
    runButton_ = new QPushButton("&Run");
    loadFieldButton_ = new QPushButton("&Load field");
    clearFieldButton_ = new QPushButton("&Clear field");
    nextButton_ = new QPushButton("&Next");
    drawButton_ = new QPushButton("&Paint");
    moveButton_ = new QPushButton("&Move");
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
    timer_->start(200);
    mainLayout->setColumnStretch(0, 1);
    mainLayout->setColumnStretch(2, 1);
    mainLayout->addWidget(fieldArea_, 0, 0, 1, 4);
    mainLayout->addWidget(runButton_, 1, 0, Qt::AlignLeft);
    mainLayout->addWidget(nextButton_, 1, 1, Qt::AlignLeft);
    mainLayout->addWidget(loadFieldButton_, 1, 2, Qt::AlignLeft);
    mainLayout->addWidget(clearFieldButton_, 1, 3, Qt::AlignLeft);
    mainLayout->addWidget(stepsLabel_, 1, 4, Qt::AlignLeft);
    mainLayout->addWidget(colorLabel_, 1, 5, Qt::AlignLeft);
    mainLayout->addWidget(colorComboBox_, 1, 5);
    mainLayout->addWidget(drawButton_, 1, 6, Qt::AlignLeft);
    mainLayout->addWidget(moveButton_, 1, 7, Qt::AlignLeft);
    connect(colorComboBox_, SIGNAL(activated(int)),
            this, SLOT(colorChanged()));

    connect(loadFieldButton_, SIGNAL (released()), this, SLOT (handleLoadFieldButton()));
    connect(runButton_, SIGNAL (released()), this, SLOT (handleRunButton()));
    connect(nextButton_, SIGNAL (released()), this, SLOT (handleNextButton()));
    connect(clearFieldButton_, SIGNAL (released()), this, SLOT (handleClearButton()));
    connect(moveButton_, SIGNAL (released()), this, SLOT (handleMoveButton()));
    connect(drawButton_, SIGNAL (released()), this, SLOT (handleDrawButton()));
    setLayout(mainLayout);
    setWindowTitle(tr("WIREWORLD 2D"));
}
