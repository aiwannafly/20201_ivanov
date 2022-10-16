#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#define YEAR_SHIFT -100
#define MONTH_SHIFT 1

/*
 * This program is supposed to show the current time in
 * California
 */
int main() {
    errno = 0; // clear errno from previous usages
    time_t now = time(NULL);   // syscall, we ask OS to give us the count
                                    // of elapsed seconds since 1970
    if (now == -1) {
        perror("===  Error in time()");
        return EXIT_FAILURE;
    }
    int status = putenv("TZ=America/Los_Angeles"); // Los Angeles is placed in California
    if (0 != status) {
        char *errorBuffer = strerror(errno);
        fprintf(stderr, "Error message : %s\n", errorBuffer);
        return EXIT_FAILURE;
    }
    char *datetimeString = ctime(&now);
    printf("%s", datetimeString);
    struct tm *localTime = localtime(&now);
    printf("%d/%d/%02d %d:%02d %s\n",
           localTime->tm_mon + MONTH_SHIFT, localTime->tm_mday, // months since January
           localTime->tm_year + YEAR_SHIFT, localTime->tm_hour, // years since 1900
           localTime->tm_min, tzname[localTime->tm_isdst]);
    // есть таблица обработчиков прерываний
    // как происходит прерывание
    // при пререыания происходит смена флага CS
    // Системные вызовы - интерфейс ОС
    return EXIT_SUCCESS;
}
