#include <errno.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>

#define OWNERS_FILE_NAME "owners_file"
#define FILE_NOT_OPENED_MSG "File owners_file was not opened"

void print_user_identities() {
    uid_t user_real_id = getuid();
    uid_t user_effective_id = geteuid();
    printf("User real ID: %u\n"
           "User effective ID: %u\n", user_real_id, user_effective_id);
}

bool open_file() {
    FILE *owners_file = fopen(OWNERS_FILE_NAME, "r+");
    if (NULL == owners_file) {
        return false;
    }
    fclose(owners_file);
    return true;
}

int main() {
    errno = 0;
    print_user_identities();
    bool status = open_file();
    if (!status) {
        perror(FILE_NOT_OPENED_MSG);
        return EXIT_FAILURE;
    }
    printf("File %s was successfully opened.\n", OWNERS_FILE_NAME);
    if (getuid() == geteuid()) {
        return EXIT_SUCCESS;
    }
    printf("*Setting effective ID to a real ID and repeating*\n");
    int status_code = seteuid(getuid());
    if (-1 == status_code) {
        perror("Could not change effective ID");
        return EXIT_FAILURE;
    }
    print_user_identities();
    status = open_file();
    if (!status) {
        perror(FILE_NOT_OPENED_MSG);
        return EXIT_FAILURE;
    }
    printf("File %s was successfully opened.\n", OWNERS_FILE_NAME);
    return EXIT_SUCCESS;
}
