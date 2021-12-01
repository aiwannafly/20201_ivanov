#ifndef WIREWORLD2D_WIREWORLDWINDOW_H
#define WIREWORLD2D_WIREWORLDWINDOW_H

#include <QWidget>

class FieldArea;
class QComboBox;
class QPushButton;
class QLabel;
class QTimer;

class WireWorldWindow : public QWidget {
Q_OBJECT
public:
    WireWorldWindow();

private slots:
    void handleLoadFieldButton();

    void handleRunButton();

    void mapChanged();

private:
    QTimer *timer_;
    QComboBox *mapComboBox_;
    QLabel *mapLabel_;
    FieldArea *fieldArea_;
    QPushButton *runButton_;
    QPushButton *loadFieldButton_;
};


#endif //WIREWORLD2D_WIREWORLDWINDOW_H
