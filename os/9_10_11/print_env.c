#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[], char *envp[]) {
    int idx = 0;
    while(true) {
        if (envp[idx] == NULL) {
            break;
        }
        printf("%s\n", envp[idx]);
        idx++;
    }
    return EXIT_SUCCESS;
}
