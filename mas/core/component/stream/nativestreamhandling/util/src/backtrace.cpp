#include <iostream>
#include <cc++/exception.h>
#include <execinfo.h>
#include <stdlib.h>

#include "backtrace.h"

using namespace std;

#define TIME_BUFFER_SIZE 30
#define BT_BUF_SIZE 100

void BackTrace::dump() {
        void *array[BT_BUF_SIZE];
        char **strings;
        size_t size = backtrace(array, BT_BUF_SIZE);
        strings = backtrace_symbols(array, size);

        time_t currentTime = time(0);
        struct tm * timeinfo = localtime (&currentTime);
        char buffer[TIME_BUFFER_SIZE] = {0};
        strftime(buffer, TIME_BUFFER_SIZE, "%F %T #TZ# ", timeinfo);

        cout << buffer << "Obtained " << size << " stack frames." << endl;
        for (size_t i = 0; i < size; i++) {
                cout << strings[i] << endl;
        }

        cout << endl << flush;
        free(strings);
}
