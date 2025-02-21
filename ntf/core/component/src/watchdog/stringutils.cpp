/*
  File:		stringutils.cpp
  Originated:	2004-05-04
  Author:	Joakim Nilsson
  Signature:	ermjnil


  Copyright (c) 2004 MOBEON AB

  The copyright to the computer program(s) herein is the property of 
  MOBEON AB, Sweden. The programs may be used and/or copied only 
  with the written permission from MOBEON AB or in accordance with 
  the terms and conditions stipulated in the agreement/contract under 
  which the programs have been supplied.
*/

#include "def.h"
#include "Logger.h"

int string2int(const string &str) {
    if (str.size() <= 0)
	return 0;
    return atoi(str.c_str());
}

long string2long(const string &str) {
    if (str.size() <= 0)
	return 0;
    return atol(str.c_str());
}

string int2string(int num) {
    if(num == 0)
	return string("0");

    string str;
    bool neg = num < 0;

    while(num)
    {
	str += '0' + (neg ? -(num % 10) : num % 10);
	num /= 10;
    }
    if(neg)
	str += '-';
    reverse(str.begin(), str.end());
    return str;
}

string double2string(double num) {
     std::stringstream ss;
     ss << num;
     return ss.str();
}

bool stringCompare(const string& str1, const string& str2) {
    string::const_iterator iter;
    iter = search(str1.begin(), str1.end(), str2.begin(), str2.end(), nocase());
    return (iter != str1.end() && str1.size() == str2.size()) ? true : false;
}

Logger::logtype_e 
string2loglevel(const string& str) {
    int level = atoi(str.c_str());
    switch(level) {
    case 0:
	return Logger::OFF;
    case 1:
	return Logger::ERROR;
    case 2:
	return Logger::WARNING;
    case 3:
	return Logger::INFORMATION;
    default:
	return Logger::ERROR;
    }
}


/*
 * These variables describe the formatting of this file.  If you don't like the 
 * template defaults, feel free to change them here (not in your .emacs file).
 *
 * Local Variables:
 * mode: C++
 * comment-column: 32
 * c-basic-offset: 4
 * fill-column: 79
 * End:
*/
