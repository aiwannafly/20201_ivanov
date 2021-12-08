#ifndef WIREWORLD2D_MAINWINDOW_H
#define WIREWORLD2D_MAINWINDOW_H

#include <QWidget>

class FieldArea;
class WireWorldFieldManager;
class QLabel;
class QPushButton;
class QTimer;
class QComboBox;
class Runner;

class MainWindow : public QWidget {
Q_OBJECT
public:
    MainWindow();

private slots:

    void handleLoadFieldButton();

    void handleRunButton();

    void handleClearButton();

    void handleNextButton();

    void handleDrawButton();

    void handleMoveButton();

    void colorChanged();

    void speedChanged();

private:
    bool running_ = false;
    Runner *runner_;
    FieldArea *fieldArea_;
    QTimer *runGameTimer_;
    QPushButton *runButton_;
    QPushButton *loadFieldButton_;
    QPushButton *clearFieldButton_;
    QPushButton *nextButton_;
    QPushButton *paintButton_;
    QPushButton *moveButton_;
    QLabel *stepsLabel_;
    QComboBox *colorComboBox_;
    QLabel *colorLabel_;
    QComboBox *speedComboBox_;
    QLabel *speedLabel_;

    void getNext();
};

#endif //WIREWORLD2D_MAINWINDOW_H
