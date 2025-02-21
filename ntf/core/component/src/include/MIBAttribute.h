/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */
#ifndef MIBATTRIBUTE_H
#define MIBATTRIBUTE_H

#include <string>

using namespace std;

struct MIBAttribute {
    MIBAttribute(const string &name, const string &value);

    string _name;
    string _value;
};
#endif
