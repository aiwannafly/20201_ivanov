#ifndef WIREWORLD2D_WIREWORLDWINDOW_H
#define WIREWORLD2D_WIREWORLDWINDOW_H

#include <QWidget>

class FieldArea;
class WireWorldFieldManager;
class QLabel;
class QPushButton;
class QTimer;
class QComboBox;
class WireWorldRunner;

class WireWorldWindow : public QWidget {
Q_OBJECT
public:
    WireWorldWindow();

private slots:

    void handleLoadFieldButton();

    void handleRunButton();

    void handleClearButton();

    void handleNextButton();

    void handleDrawButton();

    void handleMoveButton();

    void colorChanged();

private:
    bool running_ = false;
    WireWorldRunner *runner_;
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
};

#endif //WIREWORLD2D_WIREWORLDWINDOW_H
