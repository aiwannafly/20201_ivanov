#include <QApplication>

#include "WireWorldWindow.h"

int main(int argc, char *argv[]) {
    //TODO: add interface between runner and window
    //TODO: add good zoomer
    //TODO: add ability to move on the field
    QApplication app(argc, argv);
    WireWorldWindow window;
    window.show();
    return app.exec();
}
