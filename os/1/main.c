#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <ulimit.h>
#include <sys/resource.h>

#define SUCCESS (0)
#define FAIL (-1)
#define BUF_SIZE (100)
#define ARGS_LIST """\
 -i  Печатает реальные и эффективные идентификаторы пользователя и группы.\n\
 -s  Процесс становится лидером группы.\n\
 -p  Печатает идентификаторы процесса, процесса-родителя и группы процессов.\n\
 -u  Печатает значение ulimit\n\
 -Unew_ulimit  Изменяет значение ulimit.\n\
 -c  Печатает размер в байтах core-файла, который может быть создан.\n\
 -Csize  Изменяет размер core-файла\n\
 -d  Печатает текущую рабочую директорию\n\
 -v  Распечатывает переменные среды и их значения\n\
 -Vname=value  Вносит новую переменную в среду или изменяет значение существующей переменной.\n"""

extern char **environ;

static bool extract_int(const char *buf, int *num) {
    if (NULL == buf || num == NULL) {
        return false;
    }
    char *end_ptr = NULL;
    *num = (int) strtol(buf, &end_ptr, 10);
    if (buf + strlen(buf) > end_ptr) {
        return false;
    }
    return true;
}

int main(int argc, char *argv[]) {
    printf("%s\n\n", ARGS_LIST);
    const char options[] = "ispuU:cC:dvV:";
    int current_arg;
    struct rlimit rlp;
    char **current_env_var;
    char buf[BUF_SIZE];
    while ((current_arg = getopt(argc, argv, options)) != FAIL) {
        switch (current_arg) {
            case 'i':
                printf("=== User real id: %d\n", getuid());
                printf("    User effective id: %d\n", geteuid());
                printf("    Group real id: %d\n", getgid());
                printf("    Group effective id: %d\n", getegid());
                break;
            case 's': {
                int return_value = setpgid(0, 0);
                if (return_value == FAIL) {
                    perror("=== Error in setpgid");
                } else {
                    printf("=== Process became a leader of the group\n");
                }
                break;
            }
            case 'p':
                printf("=== Process id: %d\n", getpid());
                printf("    Parent process id: %d\n", getppid());
                printf("    Group process id: %d\n", getpgid(0));
                break;
            case 'u': {
                /* Return the limit on the size of a file, in units of 512 bytes. */
                long return_value = ulimit(UL_GETFSIZE);
                if (return_value == FAIL) {
                    perror("=== Error in ulimit");
                    break;
                }
                printf("=== ulimit = %ld\n", return_value);
                break;
            }
            case 'U': {
                int new_limit;
                bool extracted = extract_int(optarg, &new_limit);
                if (!extracted) {
                    fprintf(stderr, "=== Bad arg for -U, it should be a number\n");
                    break;
                }
                if (ulimit(UL_SETFSIZE, new_limit) == FAIL) {
                    perror("=== Error in ulimit");
                }
                break;
            }
            case 'c':
                if (getrlimit(RLIMIT_CORE, &rlp) == FAIL) {
                    perror("=== Error in getrlimit");
                    break;
                }
                printf("=== core size = %ld\n", rlp.rlim_cur);
                break;
            case 'C': {
                if (getrlimit(RLIMIT_CORE, &rlp) == FAIL) {
                    perror("=== Error in getrlimit");
                    break;
                }
                int new_size;
                bool extracted = extract_int(optarg, &new_size);
                if (!extracted) {
                    fprintf(stderr, "=== Bad value for -C, it should be a number\n");
                    break;
                }
                rlp.rlim_cur = new_size;
                if (setrlimit(RLIMIT_CORE, &rlp) == FAIL)
                    perror("=== Error in setrlimit");
                break;
            }
            case 'd': {
                char *p = getcwd(buf, BUF_SIZE);
                if (NULL == p) {
                    perror("=== Error in getcwd");
                    break;
                }
                printf("=== Current working directory is: %s\n", buf);
                break;
            }
            case 'v':
                printf("=== Environment variables are:\n");
                for (current_env_var = environ; *current_env_var; current_env_var++) {
                    printf("%s\n", *current_env_var);
                }
                break;
            case 'V': {
                int return_value = putenv(optarg);
                if (return_value != SUCCESS) {
                    perror("=== Error in putenv");
                }
                break;
            }
            default:
                fprintf(stderr, "=== Wrong parameter\n");
                break;
        }
    }
    return EXIT_SUCCESS;
}
