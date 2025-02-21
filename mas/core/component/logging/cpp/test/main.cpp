#include <log4cxx/logger.h>
#include <log4cxx/propertyconfigurator.h>

#include <stdlib.h>

int main(int argc, char** argv)
{
    // Initalize logger ...
                
    log4cxx::PropertyConfigurator::configure("logger.properties");
    log4cxx::Logger* logger = log4cxx::Logger::getLogger("Apa");

    logger->debug("Hej hopp ...");

    return 0;
}
