/*
  File:		Config.h
  Description:	
  Originated:	2004-05-11
  Author:	Joakim Nilsson
  Signature:	ermjnil


  Copyright (c) 2004 MOBEON AB

  The copyright to the computer program(s) herein is the property of 
  MOBEON AB, Sweden. The programs may be used and/or copied only 
  with the written permission from MOBEON AB or in accordance with 
  the terms and conditions stipulated in the agreement/contract under 
  which the programs have been supplied.
*/

#ifndef CONFIG_H
#define CONFIG_H

#include <string>
#include <map>

using namespace std;

class Config {
public:
    ~Config();
    static Config * Instance();
    int getTimeOut() const;
    int getValue(const string &var, string &value) const;
    void init(string cmd, string configFile);
    void update(string cmd, string configFile) { init(cmd, configFile); };
    
private:
    map<string, string> _variableList;
    static Config * _pInstance;
    int updateValue(string val, string cmd, string configFile);

    Config();

};
#endif
