/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "platform.h"

bool
Platform::isBigEndian() {
    int b;
    char *p;

    b = 1;
    p = (char *) &b;
    if (*p == 0) {
        return true;
    } else {
        return false;
    }
}

bool
Platform::isLittleEndian() {
    return !Platform::isBigEndian();
}

