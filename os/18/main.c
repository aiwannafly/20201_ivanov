#include <dirent.h>
#include <grp.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pwd.h>
#include <time.h>
#include <sys/stat.h>

#define BUF_SIZE (30)
#define FAIL (-1)

/*
 * Prints main file statistics, like in ls -la output
 */
static void print_file_info(const char *name, const struct stat *file_info) {
    if (NULL == name || NULL == file_info) {
        return;
    }
    char rights[10 + 1];
    if (S_IFREG == (file_info->st_mode & S_IFMT)) {
        rights[0] = '-';
    } else if (S_IFDIR == (file_info->st_mode & S_IFMT)) {
        rights[0] = 'd';
    } else {
        rights[0] = '?';
    }
    rights[1] = (file_info->st_mode & S_IRUSR) ? 'r' : '-';
    rights[2] = (file_info->st_mode & S_IWUSR) ? 'w' : '-';
    rights[3] = (file_info->st_mode & S_IXUSR) ? 'x' : '-';

    rights[4] = (file_info->st_mode & S_IRGRP) ? 'r' : '-';
    rights[5] = (file_info->st_mode & S_IWGRP) ? 'w' : '-';
    rights[6] = (file_info->st_mode & S_IXGRP) ? 'x' : '-';

    rights[7] = (file_info->st_mode & S_IROTH) ? 'r' : '-';
    rights[8] = (file_info->st_mode & S_IWOTH) ? 'w' : '-';
    rights[9] = (file_info->st_mode & S_IXOTH) ? 'x' : '-';
    rights[10] = '\0';
    printf("%s", rights);
    printf(" %lu", file_info->st_nlink);
    struct passwd *user = getpwuid(file_info->st_uid);
    struct group *group = getgrgid(file_info->st_gid);
    if (user == NULL) {
        printf(" %d", file_info->st_uid);
    } else {
        printf(" %s", user->pw_name);
    }
    if (group == NULL) {
        printf(" %d", file_info->st_gid);
    } else {
        printf(" %s", group->gr_name);
    }
    time_t t = file_info->st_mtim.tv_sec;
    const char *s = ctime(&t);
    char last_update_time[BUF_SIZE];
    memset(last_update_time, 0, BUF_SIZE);
    strncpy(last_update_time, s, strlen(s) - 1);
    printf("\t%lu\t%s %s\n", file_info->st_size, last_update_time, name);
}

int main() {
    DIR *dir = opendir(".");
    if (dir == NULL) {
        perror("=== Error in opendir()");
        return EXIT_FAILURE;
    }
    struct dirent *entry;
    errno = 0;
    while ((entry = readdir(dir)) != NULL) {
        struct stat file_info;
        int return_value = lstat(entry->d_name, &file_info);
        if (return_value == FAIL) {
            perror("=== Error in lstat()");
            continue;
        }
        print_file_info(entry->d_name, &file_info);
    }
    if (errno != 0) {
        perror("=== Error in readdir()");
        return EXIT_FAILURE;
    }
    int return_value = closedir(dir);
    if (return_value == FAIL) {
        perror("=== Error in closedir()");
    }
    return EXIT_SUCCESS;
}
