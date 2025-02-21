#include "log4cxx/logger.h"
#include <log4cxx/propertyconfigurator.h>
#include <log4cxx/basicconfigurator.h>

#include <stdlib.h>
#include <iostream>

using std::cout;
using std::endl;

int main(int argc, char** argv)
{
    // Initalize logger ...
    //    log4cxx::BasicConfigurator::configure();
                
    cout << "Configuring ..." << endl;
    log4cxx::PropertyConfigurator::configure("logger.properties");
    cout << "Getting logger" << endl;
    log4cxx::Logger* logger = log4cxx::Logger::getLogger("Apa");
    cout << "Calling logger ..." << endl;
    logger->debug("Hej hopp ...");

    return 0;
}
