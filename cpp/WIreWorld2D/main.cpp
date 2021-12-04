#include <QApplication>

#include "WireWorldWindow.h"

int main(int argc, char *argv[]) {
    //TODO: add interface between runner and mainWindow
    QApplication app(argc, argv);
    WireWorldWindow mainWindow;
    mainWindow.show();
    return QApplication::exec();
}
