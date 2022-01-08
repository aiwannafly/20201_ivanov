#ifndef WIREWORLD2D_MAINWINDOW_H
#define WIREWORLD2D_MAINWINDOW_H

#include <QWidget>

class FieldWidget;
class QLabel;
class QPushButton;
class QTimer;
class QComboBox;
class WireWorld;

class MainWindow : public QWidget {
    Q_OBJECT
public:
    MainWindow();

private
slots:
    void handleLoadFieldButton();
    void handleRunButton();
    void handleClearButton();
    void handleNextButton();
    void colorChanged();
    void speedChanged();
    void gameChanged();

private:
    bool running_ = false;
    FieldWidget *fieldWidget_;
    QTimer *runGameTimer_;
    QPushButton *runButton_;
    QPushButton *loadFieldButton_;
    QPushButton *clearFieldButton_;
    QPushButton *nextButton_;
    QComboBox *colorComboBox_;
    QLabel *colorLabel_;
    QComboBox *speedComboBox_;
    QLabel *speedLabel_;
    QComboBox *gameComboBox_;
    QLabel *gameLabel_;

    void getNext();
    void stopRunning();
    void initButtons();
    void initColorComboBox();
    void initSpeedComboBox();
    void initGameComboBox();

    void setColors();
};

#endif //WIREWORLD2D_MAINWINDOW_H
