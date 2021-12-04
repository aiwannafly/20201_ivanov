#include <QApplication>

#include "WireWorldWindow.h"
#include <array>

int main(int argc, char *argv[]) {
    //TODO: add interface between runner and mainWindow
    //fieldArea <-> WWInterface <-> Runner?
    QApplication app(argc, argv);
    WireWorldWindow mainWindow;
    mainWindow.show();
    return QApplication::exec();
}
