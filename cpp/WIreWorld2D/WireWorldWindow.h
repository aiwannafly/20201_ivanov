#ifndef WIREWORLD2D_WIREWORLDWINDOW_H
#define WIREWORLD2D_WIREWORLDWINDOW_H

#include <QWidget>

class FieldArea;

class QLabel;

class QPushButton;

class QTimer;

class QComboBox;

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
    QTimer *timer_;
    FieldArea *fieldArea_;
    QPushButton *runButton_;
    QPushButton *loadFieldButton_;
    QPushButton *clearFieldButton_;
    QPushButton *nextButton_;
    QPushButton *drawButton_;
    QPushButton *moveButton_;
    QLabel *stepsLabel_;
    QComboBox *colorComboBox_;
    QLabel *colorLabel_;
};

#endif //WIREWORLD2D_WIREWORLDWINDOW_H
