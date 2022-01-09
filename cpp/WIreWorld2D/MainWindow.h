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
    void handleEraseButton();
    void colorChanged();
    void speedChanged();
    void gameChanged();

private:
    bool running_ = false;
    QSharedPointer<FieldWidget> fieldWidget_;
    QSharedPointer<QTimer> runGameTimer_;
    QSharedPointer<QPushButton> runButton_;
    QSharedPointer<QPushButton> loadFieldButton_;
    QSharedPointer<QPushButton> clearFieldButton_;
    QSharedPointer<QPushButton> nextButton_;
    QSharedPointer<QPushButton> eraseButton_;
    QSharedPointer<QComboBox> colorComboBox_;
    QSharedPointer<QComboBox> speedComboBox_;
    QSharedPointer<QComboBox> gameComboBox_;

    void getNext();
    void stopRunning();
    void initButtons();
    void initColorComboBox();
    void initSpeedComboBox();
    void initGameComboBox();

    void setColors();
};

#endif //WIREWORLD2D_MAINWINDOW_H
