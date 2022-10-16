#include <stdio.h>
#include <sys/select.h>

int main() {
    fd_set master_set;
    FD_ZERO(&master_set);
    FD_SET(0, &master_set);
    FD_SET(1, &master_set);
    for (int i = 0; i < 3; i++) {
        printf("%d\n", FD_ISSET(i, &master_set));
    }
    return 0;
}
