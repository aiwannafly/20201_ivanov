#include <stdio.h>
#include <stdlib.h>
#include <termios.h>
#include <unistd.h>

int main() {
    printf("y or n ?\n");
    struct termios term;
    if (!tcgetattr(STDIN_FILENO, &term)) {
        term.c_lflag &= ~ICANON;
        int omin = term.c_cc[VMIN];
        term.c_cc[VMIN] = 1;
        if (!tcsetattr(STDIN_FILENO, TCSANOW, &term)) {
            char c;
            scanf("%c", &c);
            if (c != 'y' && c != 'n') {
                fprintf(stderr, "\nBad answer\n");
            } else {
                printf("\nAnswer: %c\n", c);
            }
            term.c_lflag |= ICANON;
            term.c_cc[VMIN] = omin;
            if (tcsetattr(STDIN_FILENO, TCSANOW, &term)) {
                return EXIT_FAILURE;
            }
        }
    }
    return EXIT_SUCCESS;
}
