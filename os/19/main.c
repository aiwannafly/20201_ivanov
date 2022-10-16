#include <dirent.h>
#include <errno.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define PATTERN_MAX_SIZE (256)

int main() {
    char pattern[PATTERN_MAX_SIZE];
    printf("=== Enter pattern: ");
    fgets(pattern, PATTERN_MAX_SIZE, stdin);
    size_t pattern_length = strlen(pattern);
    pattern[pattern_length] = 0;
    for (int i = 0; i < pattern_length; i++) {
        if (pattern[i] == '/') {
            fprintf(stderr, "=== Symbol / is not supported pattern\n");
            return EXIT_FAILURE;
        }
    }
    DIR *dir = opendir(".");
    if (!dir) {
        perror("=== Error in opendir");
        return EXIT_FAILURE;
    }
    struct dirent *entry;
    size_t file_idx;
    int printed_files_count = 0;
    printf("\n");
    errno = 0;
    while ((entry = readdir(dir)) != NULL) {
        size_t file_length = strlen(entry->d_name);
        int pat_idx = 0;
        bool pattern_completed = false;
        for (file_idx = 0; (file_idx < file_length) && (pat_idx < pattern_length); file_idx++) {
            if (('?' != pattern[pat_idx]) && ('*' != pattern[pat_idx])) {
                if (pattern[pat_idx] != entry->d_name[file_idx]) {
                    break;
                }
                pat_idx++;
            } else if ('?' == pattern[pat_idx]) {
                pat_idx++;
            } else { // *
                while (pat_idx < pattern_length) {
                    pat_idx++;
                    if ('*' != pattern[pat_idx]) {
                        break;
                    }
                }
                if (pattern_length - 1 == pat_idx) {
                    pattern_completed = true;
                    break;
                }
                if (pattern[pat_idx] == '?') {
                    pat_idx++;
                    continue;
                }
                while (file_idx < file_length) {
                    if (pattern[pat_idx] == entry->d_name[file_idx]) {
                        break;
                    }
                    file_idx++;
                }
                pat_idx++;
            }
        }
        if (file_length == file_idx) {
            while (pat_idx < pattern_length) {
                if ('*' != pattern[pat_idx]) {
                    break;
                }
                pat_idx++;
            }
            if (pattern_length - 1 == pat_idx) {
                pattern_completed = true;
            }
        }
        if (pattern_completed || (file_idx == file_length && pat_idx == pattern_length)) {
            printf("--> %s\n", entry->d_name);
            printed_files_count++;
        }
    }
    if (errno != 0) {
        perror("=== Error in readdir()");
        return EXIT_FAILURE;
    }
    if (printed_files_count == 0) {
        printf("=== Pattern: %s\n", pattern);
    }
    closedir(dir);
    return EXIT_SUCCESS;
}
