#include <QApplication>

#include "WireWorldWindow.h"

int main(int argc, char *argv[])
{
    QApplication app(argc, argv);
    WireWorldWindow window;
    window.show();
    return app.exec();
}
