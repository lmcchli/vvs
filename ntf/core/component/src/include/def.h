#ifndef DEF_H
#define DEF_H

#include <string>
#include <sys/time.h>
#include <vector>
#include "MIBAttribute.h"
#include <cctype>

using namespace std;

// Useful functions for strings 
int string2int(const string &str);
long string2long(const string &str);
string int2string(int num);
#ifndef linux
string hrtime2string(hrtime_t num);
#endif
string double2string(double num);
bool stringCompare(const string &str1, const string &str2);
void sendSignal(int signal);


typedef vector<MIBAttribute> AttributeVector;

#define SUCCESS             0
#define FAILURE             1

// Useful define to print variables during development.
#define VAROUT(a) std::cout << #a ": " << a << endl

// Return values from functions
const int SYSTEM_ERROR   = -1;
const int SUCCESSFUL     =  0;
const int DATA_ERROR     =  1;
const int SYNTAX_ERROR   =  2;


////////////////////Protocol definitions///////////////////////
// Event types
#define GET       "Get"
#define SET       "Set"
#define RESPONSE  "Response"
#define START     "Start"
#define STOP      "Stop"
#define ALARM     "Alarm"

// Index constants
const int INDEX_NTF_OBJECT   =  0;
const int INDEX_COMMON_ALARM = 100;
const int INDEX_NO_INSTANCE = -1;

class nocase {
public:
    bool operator() (const char c1, const char c2)
	{ return toupper(c1) == toupper(c2); }
};

class Tokenizer {
    string _str;
    char _separator;
    int _index;
    bool _atEnd;
 public:
    Tokenizer(const string& s, char sep=' ') 
	: _str(s), 
	_separator(sep), 
	_index(0), 
	_atEnd(false) {}
    Tokenizer() 
	: _index(0), 
	_atEnd(false) {}
    
    //
    // Reinitialize the object so that we can reuse the same object.
    //
    void init(const string& s, char sep) { 
	_str = s; _separator = sep; _index = 0; _atEnd = false; 
    }
     void init(const string& s) { 
	_str = s; _index = 0; _atEnd = false; 
    }
     void init() { 
	_index = 0; _atEnd = false; 
    }
    
    //
    // Get the current token and move the pointer forward.
    // Returns SUCCESSFUL if the string is valid, DATA_ERROR if 
    // the end has been reached.
    //
    int get(string& ret) {
	if(_atEnd)
	    return DATA_ERROR;
	int i = _str.find(_separator, _index);
	ret = _str.substr(_index, i - _index);
	if(i == string::npos)
	    _atEnd = true;
	_index = i + 1;
	return SUCCESSFUL;
    }

    //
    // Get the current token without moveing the pointer forward.
    // Returns true if the string is valid, false if 
    // the end has been reached.
    //
    int peek(string& ret) {
	if(_index == string::npos)
	    return DATA_ERROR;
	int i = _str.find(_separator, _index);
	ret = _str.substr(_index, i - _index);
	return SUCCESSFUL;
    }
};
#endif

