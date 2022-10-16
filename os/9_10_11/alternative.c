
#include <fcntl.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <wait.h>
#include <unistd.h>
#include "linked_list.h"

#define PATH_PREFIX "PATH="
#define DEFAULT_PATH "/usr/bin"
#define PATH_MAX_LEN (1000)
#define SINGLE_PATH_MAX_LEN (100)
#define FAIL (-1)
#define CHILD_FORK_PID (0)
#define SPLITTER ':'

extern char **environ;

static bool starts_with(const char *string, const char *prefix) {
    if (NULL == string || NULL == prefix) {
        return false;
    }
    size_t string_len = strlen(string);
    size_t prefix_len = strlen(prefix);
    if (prefix_len > string_len) {
        return false;
    }
    for (size_t i = 0; i < prefix_len; i++) {
        if (string[i] != prefix[i]) {
            return false;
        }
    }
    return true;
}

static list_t *examine_all_paths(char PATH[PATH_MAX_LEN]) {
    list_t *paths_list = init_list();
    size_t PATH_LEN = strlen(PATH);
    size_t begin_idx = 0;
    size_t end_idx = 0;
    for (size_t i = 0; i < PATH_LEN; i++) {
        if (PATH[i] == SPLITTER) {
            size_t len = end_idx - begin_idx + 1;
            char *new_path = malloc(SINGLE_PATH_MAX_LEN);
            if (NULL == new_path) {
                free_list(paths_list, free);
                return NULL;
            }
            for (size_t j = begin_idx; j < end_idx; j++) {
                new_path[j - begin_idx] = PATH[j];
            }
            new_path[len - 1] = '/';
            new_path[len] = '\0';
            bool appended = append(paths_list, new_path);
            if (!appended) {
                free_list(paths_list, free);
                return NULL;
            }
            begin_idx = end_idx + 1;
        }
        end_idx++;
    }
    return paths_list;
}

static void get_path_from_env(char path[PATH_MAX_LEN]) {
    bool found_path = false;
    size_t idx = 0;
    while (true) {
        char *arg = environ[idx];
        if (NULL == arg) {
            break;
        }
        if (starts_with(arg, PATH_PREFIX)) {
            for (size_t i = strlen(PATH_PREFIX); i < strlen(arg); i++) {
                path[i - strlen(PATH_PREFIX)] = arg[i];
            }
            path[strlen(arg) - strlen(PATH_PREFIX)] = '\0';
            found_path = true;
            break;
        }
        idx++;
    }
    if (!found_path) {
        for (size_t i = 0; i < strlen(DEFAULT_PATH); i++) {
            path[i] = DEFAULT_PATH[i];
        }
        path[strlen(DEFAULT_PATH)] = '\0';
    }
}

int execvpe(const char *file, char *const argv[], char *const envp[]) {
    if (NULL == file || NULL == argv || NULL == envp) {
        return FAIL;
    }
    char path[PATH_MAX_LEN];
    get_path_from_env(path);
    list_t *paths_list = examine_all_paths(path);
    if (NULL == paths_list) {
        return FAIL;
    }
    list_node_t *current = paths_list->head;
    for (size_t i = 0; i < paths_list->len; i++) {
        char *full_path = strcat(current->value, file);
        // printf("%s\n", full_path);
        if (FAIL == open(full_path, O_RDONLY)) {
            current = current->next;
        } else {
            return execve(full_path, argv, envp);
        }
    }
    free_list(paths_list, free);
    return FAIL;
}
