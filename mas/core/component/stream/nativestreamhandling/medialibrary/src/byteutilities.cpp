/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <string.h>
#include "byteutilities.h" 
#include <iostream>
/* Swap word */
void 
ByteUtilities::swapW(uint16_t &uw)
{
    uw = ((uw >> 8) & 0x00ff) | ((uw << 8) & 0xff00);
}

/* Swap double word */
void
ByteUtilities::swapDW(uint32_t &udw) 
{
    udw = ((udw >> 24) & 0x000000ffL) | 
          ((udw >> 8)  & 0x0000ff00L) | 
          ((udw << 8)  & 0x00ff0000L) | 
          ((udw << 24) & 0xff000000L);
}
/* Read double word */
void 
ByteUtilities::readDW(const char *ptr, uint32_t &udw, bool swap) 
{
//    std::cout << "ByteUtilities:readDW ptr:" << ptr 
//                << " udw:" << udw << std::endl;

    memcpy(&udw, ptr, sizeof(uint32_t));
    if (swap) {
        ByteUtilities::swapDW(udw);    
    }

}
/* Read word. */
void 
ByteUtilities::readW(const char *ptr, uint16_t &uw, bool swap) 
{
//    std::cout << "ByteUtilities:readW ptr:" << ptr 
//                << " uw:" << uw << std::endl;
    
    memcpy(&uw, ptr, sizeof(uint16_t));
    if (swap) {
        ByteUtilities::swapW(uw);    
    }
}
void 
ByteUtilities::alignW(uint16_t &uw) {
    if (uw % 2 > 0) {
        uw++;
    }
}
void 
ByteUtilities::alignDW(uint32_t &udw) {
    if (udw % 2 > 0) {
        udw++;
    }
}
