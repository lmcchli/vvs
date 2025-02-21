/*
  File:		MIBAttribute.cpp
  Originated:	2004-04-26
  Author:	Joakim Nilsson
  Signature:	ermjnil


  Copyright (c) 2004 MOBEON AB

  The copyright to the computer program(s) herein is the property of 
  MOBEON AB, Sweden. The programs may be used and/or copied only 
  with the written permission from MOBEON AB or in accordance with 
  the terms and conditions stipulated in the agreement/contract under 
  which the programs have been supplied.
*/

#include "MIBAttribute.h"


MIBAttribute::MIBAttribute(const string &name, const string &value) {
    _name = name;
    _value = value;
}
