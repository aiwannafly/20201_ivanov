#include <dirent.h>
#include <errno.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <regex.h>

#include "linked_list.h"

#define PATTERN_MAX_SIZE (256)
#define SUCCESS (0)
#define FAIL (-1)
#define PATH_MAX_SIZE (256)

static bool match(const char *string, const char *pattern) {
    regex_t regex;
    if (regcomp(&regex, pattern, REG_EXTENDED) != SUCCESS) {
        return false;
    }
    int status = regexec(&regex, string, 0, NULL, 0);
    regfree(&regex);
    if (status != SUCCESS) {
        return false;
    }
    return true;
}

/*
 * Translates a short regular expression into another
 * one, which can be compiled by <regex.h> API
 */
static void make_lib_pattern(const char old_pattern[PATTERN_MAX_SIZE],
                             char new_pattern[PATTERN_MAX_SIZE]) {
    const char *all_symbols_pat = "[1-9a-zA-Z\\_\\-\\.\\,\\;@#$%^&*!~`/]";
    size_t idx = 0;
    if (old_pattern[0] != '*') {
        new_pattern[idx] = '^';
        idx++;
    }
    for (size_t i = 0; i < strlen(old_pattern); i++) {
        if (old_pattern[i] == '?' || old_pattern[i] == '*') {
            new_pattern[idx] = 0;
            strcat(new_pattern, all_symbols_pat);
            idx += strlen(all_symbols_pat);
        }
        new_pattern[idx] = old_pattern[i];
        idx++;
    }
    if (new_pattern[idx - 1] != '*') {
        new_pattern[idx] = '$';
        idx++;
    }
    new_pattern[idx] = 0;
}

/*
 * Returns a list with names of all files in
 * the current directory. The list includes
 * subdirectories and files in them.
 */
static list_t *get_all_file_names(DIR *dir, char *current_path) {
    if (NULL == dir) {
        return NULL;
    }
    struct dirent *entry;
    errno = 0;
    list_t *file_names = init_list();
    while ((entry = readdir(dir)) != NULL) {
        char *file_name = calloc(PATH_MAX_SIZE, 1);
        memcpy(file_name, entry->d_name, strlen(entry->d_name));
        bool appended = append(file_names, file_name);
        if (!appended) {
            errno = ENOMEM;
            free_list(file_names, free);
            return NULL;
        }
        if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
            continue;
        }
        if (entry->d_type == DT_DIR) {
            char *new_path = calloc(PATH_MAX_SIZE, 1);
            if (current_path != NULL) {
                strcpy(new_path, current_path);
                new_path[strlen(new_path)] = '/';
            }
            strcat(new_path, entry->d_name);
            DIR *subdir = opendir(new_path);
            if (NULL == subdir) {
                free(new_path);
                free_list(file_names, free);
                return NULL;
            }
            list_t *sub_file_names = get_all_file_names(subdir, new_path);
            if (NULL == sub_file_names) {
                free(new_path);
                free_list(file_names, free);
                return NULL;
            }
            size_t sub_paths_count = sub_file_names->len;
            for (size_t i = 0; i < sub_paths_count; i++) {
                char *sub_file = pop(sub_file_names);
                if (strcmp(sub_file, ".") == 0 || strcmp(sub_file, "..") == 0) {
                    free(sub_file);
                    continue;
                }
                char *sub_path = calloc(PATH_MAX_SIZE, 1);
                strcpy(sub_path, entry->d_name);
                sub_path[strlen(sub_path)] = '/';
                strcat(sub_path, sub_file);
                appended = append(file_names, sub_path);
                free(sub_file);
                if (!appended) {
                    free(new_path);
                    int return_value = closedir(subdir);
                    if (return_value == FAIL) {
                        perror("=== Error in closedir()");
                    }
                    free_list(file_names, free);
                    free_list(sub_file_names, free);
                    return NULL;
                }
            }
            free(new_path);
            int return_value = closedir(subdir);
            if (return_value == FAIL) {
                perror("=== Error in closedir()");
            }
            free_list(sub_file_names, free);
        }
    }
    if (errno != 0) {
        free_list(file_names, free);
        return NULL;
    }
    return file_names;
}

int main() {
    char pattern[PATTERN_MAX_SIZE];
    printf("=== Enter pattern: ");
    fgets(pattern, PATTERN_MAX_SIZE, stdin);
    size_t pattern_length = strlen(pattern) - 1; // \n is not needed
    pattern[pattern_length] = 0;
    char re_pattern[PATTERN_MAX_SIZE];
    make_lib_pattern(pattern, re_pattern);
    int printed_files_count = 0;
    printf("\n");
    DIR *dir = opendir(".");
    if (!dir) {
        perror("=== Error in opendir()");
        return EXIT_FAILURE;
    }
    list_t *file_names = get_all_file_names(dir, NULL);
    if (NULL == file_names) {
        perror("=== Could not get file names");
        int return_value = closedir(dir);
        if (return_value == FAIL) {
            perror("=== Error in closedir()");
        }
        return EXIT_FAILURE;
    }
    size_t files_count = file_names->len;
    for (size_t i = 0; i < files_count; i++) {
        char *file_name = pop(file_names);
        if (match(file_name, re_pattern)) {
            printed_files_count++;
            printf("--> %s\n", file_name);
        }
        free(file_name);
    }
    if (printed_files_count == 0) {
        printf("=== Pattern: %s\n", pattern);
    }
    free_list(file_names, free);
    int return_value = closedir(dir);
    if (return_value == FAIL) {
        perror("=== Error in closedir()");
    }
    return EXIT_SUCCESS;
}
