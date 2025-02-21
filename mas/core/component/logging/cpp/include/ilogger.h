#ifndef _LOGGER_H
#define _LOGGER_H

#include <string>
#include <stdexcept>

namespace log4cpp {
    class Category;
}

class  ConfigureFailure : public std::runtime_error {
public:
    /**
     * Constructor.
     * @param reason String containing the description of the exception.
     */
    explicit ConfigureFailure(const std::string& reason) : std::runtime_error(reason) {}
};

class ILogger {
public:
    ILogger(const std::string& category);
    void debug(const char * stringFormat, ...) throw();
    void debug(const std::string& logMessage) throw();
    void info(const char * stringFormat, ...) throw();
    void info(const std::string& logMessage) throw();
    void warn(const char * stringFormat, ...) throw();
    void warn(const std::string& logMessage) throw();
    void error(const char * stringFormat, ...) throw();
    void error(const std::string& logMessage) throw();
    void fatal(const char * stringFormat, ...) throw();
    void fatal(const std::string& logMessage) throw();

    static void configureAndWatch(const std::string& filename) throw(ConfigureFailure);
    static void configureAndWatch(const std::string& filename, long delay) throw(ConfigureFailure);

    bool isDebugEnabled() const throw();
    bool isInfoEnabled() const throw();
    bool isWarningEnabled() const throw();
    bool isErrorEnabled() const throw();
    bool isFatalEnabled() const throw();

private:
    log4cpp::Category& cat;
};

#define LOGGER_FATAL(logger, message) { \
        if (logger.isFatalEnabled()) {\
        std::ostringstream oss; \
        oss << message; \
        logger.fatal(oss.str().c_str(), __FILE__, __LINE__); }}

#define LOGGER_ERROR(logger, message) { \
        if (logger.isErrorEnabled()) {\
        std::ostringstream oss; \
        oss << message; \
        logger.error(oss.str().c_str(), __FILE__, __LINE__); }}

#define LOGGER_WARN(logger, message) { \
        if (logger.isWarningEnabled()) {\
        std::ostringstream oss; \
        oss << message; \
        logger.warn(oss.str().c_str(), __FILE__, __LINE__); }}

#define LOGGER_INFO(logger, message) { \
        if (logger.isInfoEnabled()) {\
        std::ostringstream oss; \
        oss << message; \
        logger.info(oss.str().c_str(), __FILE__, __LINE__); }}

#define LOGGER_DEBUG(logger, message) { \
        if (logger.isDebugEnabled()) {\
        std::ostringstream oss; \
        oss << message; \
        logger.debug(oss.str().c_str(), __FILE__, __LINE__); }}

#endif //_LOGGER_H
