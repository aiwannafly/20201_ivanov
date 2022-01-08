#include <QApplication>

#include "MainWindow.h"

int main(int argc, char *argv[]) {
    /*
     * TODO: make common rle parser
     * TODO: make moving zoom
     * TODO: make good movement on a field
     * TODO: make good field loading
     */
    QApplication app(argc, argv);
    MainWindow mainWindow;
    mainWindow.show();
    return QApplication::exec();
}
