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

void WireWorldWindow::handleRunButton() {
    if (fieldArea_->isRun()) {
        runButton_->setText("&Run");
        fieldArea_->stop();
        return;
    }
    fieldArea_->run();
    runButton_->setText("&Stop");
}

void WireWorldWindow::mapChanged() {
    int idx = mapComboBox_->itemData(
            mapComboBox_->currentIndex(), Qt::UserRole).toInt();
    
}

WireWorldWindow::WireWorldWindow() {
    fieldArea_ = new FieldArea;
    runButton_ = new QPushButton("&Run");
    loadFieldButton_ = new QPushButton("&Load field");
    mapComboBox_ = new QComboBox;
    mapComboBox_->addItem(tr("Empty field"));
    mapComboBox_->addItem(tr("Field 1"));
    mapComboBox_->addItem(tr("Field 2"));
    mapComboBox_->addItem(tr("Field 3"));

    mapLabel_ = new QLabel(tr("&Basic fields"));
    mapLabel_->setBuddy(mapComboBox_);
    connect(mapComboBox_, SIGNAL(activated(int)),
            this, SLOT(mapChanged()));

    QGridLayout *mainLayout = new QGridLayout;
    timer_ = new QTimer(this);
    connect(timer_, &QTimer::timeout, this, QOverload<>::of(&FieldArea::update));
    timer_->start(1000);
    mainLayout->setColumnStretch(0, 1);
    mainLayout->setColumnStretch(2, 1);
    mainLayout->addWidget(fieldArea_, 0, 0, 1, 4);
    mainLayout->addWidget(runButton_, 1, 0, Qt::AlignRight);
    mainLayout->addWidget(loadFieldButton_, 1, 1, Qt::AlignRight);
    connect(loadFieldButton_, SIGNAL (released()), this, SLOT (handleLoadFieldButton()));
    connect(runButton_, SIGNAL (released()), this, SLOT (handleRunButton()));
    setLayout(mainLayout);
    setWindowTitle(tr("WIREWORLD 2D"));
}
