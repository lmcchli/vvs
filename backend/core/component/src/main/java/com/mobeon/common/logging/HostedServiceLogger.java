/**
 * COPYRIGHT (c) Abcxyz Canada Inc., 2007.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Abcxyz Canada Inc.  The program(s) may be used and/or copied only with the
 * written permission from Abcxyz Canada Inc. or in accordance with the terms
 * and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *
 */
package com.mobeon.common.logging;

/**
 * This class decorates {@link ILogger} with methods specialized to log hosted service circumstances.
 * <br>
 * You should use the {@link HostedServiceLogger} to prevent repeated logging of connection failures.
 * <br>
 * <i>Example:</i>*
 * <pre>
 * String protocol = "imap";
 * String host = "localhost";
 * int port = 143;
 * MyService service = new MyService(protocol, host, 143);
 * <p/>
 * ILogger logger = ...
 * HostedServiceLogger hslogger = new HostedServiceLogger(logger);
 * <p/>
 * int retry = 3;
 * <p/>
 * while(retry > 0) {
 *  try {
 *     service.connect();
 *     hslogger.available(protocol, host, port);
 *     continue;
 *  } catch(Exception e) {
 *     hslogger.notAvailable(protocol, host, port,"Exception while connecting");
 *     if(--retry <= 0) throw e;
 *  }
 * }
 * </pre>
 *
 * @author Håkan Stolt
 */
public class HostedServiceLogger implements ILogger {

    /**
     * The decorated logger.
     */
    private final ILogger logger;

    /**
     * Constructs with a logger, null not allowed.
     *
     * @param logger the logger to decorate.
     */
    public HostedServiceLogger(ILogger logger) {
        this.logger = logger;
        if (logger == null) throw new IllegalArgumentException("logger cannot be null!");
    }

    /**
     * Logs availability using {@link LogJustOnceMessage} to enable non-repeated logging.
     * Builds a {@link LogJustOnceMessage} and logs it on the error level.
     *
     * @param protocol
     * @param host
     * @param port
     * @param message  optional message.
     */
    public void notResponding(String protocol, String host, int port, String message) {
        String hostedServiceUrl = composeHostedServiceUrl(protocol, host, port);
        logger.warn(new LogJustOnceMessage(
                new HostedServiceAvailabilityLogContext(hostedServiceUrl, "response"), hostedServiceUrl + " is not responding: " + message, false));
    }

    /**
     * Similar to {@link #notResponding(String, String, int, String)}
     * without the optional message.
     *
     * @param protocol
     * @param host
     * @param port
     */
    public void notResponding(String protocol, String host, int port) {
        notResponding(protocol, host, port, "");
    }


    /**
     * Logs availability using {@link LogJustOnceMessage} to enable non-repeated logging.
     * Builds a {@link LogJustOnceMessage} and logs it on the error level.
     *
     * @param protocol
     * @param host
     * @param port
     * @param message  optional message.
     */
    public void notAvailable(String protocol, String host, int port, String message) {
        String hostedServiceUrl = composeHostedServiceUrl(protocol, host, port);
        logger.error(new LogJustOnceMessage(
                new HostedServiceAvailabilityLogContext(hostedServiceUrl), hostedServiceUrl + " is not available: " + message, false));
    }

    /**
     * Similar to {@link #notAvailable(String, String, int, String)}
     * without the optional message.
     *
     * @param protocol
     * @param host
     * @param port
     */
    public void notAvailable(String protocol, String host, int port) {
        notAvailable(protocol, host, port, "");
    }


    /**
     * Logs availability using {@link LogJustOnceMessage} with a
     * resetting {@link LogJustOnceMessage} and logs it on the info level.
     *
     * @param protocol
     * @param host
     * @param port
     */
    public void available(String protocol, String host, int port) {
        String hostedServiceUrl = composeHostedServiceUrl(protocol, host, port);
        if (logger.isInfoEnabled()) logger.info(new LogJustOnceMessage(
                new HostedServiceAvailabilityLogContext(hostedServiceUrl), hostedServiceUrl + " is available.", true));
    }

    public void trace(Object message) {
        logger.trace(message);
    }

    public void trace(Object message, Throwable t) {
        logger.trace(message, t);
    }

    public void debug(Object message) {
        logger.debug(message);
    }

    public void debug(Object message, Throwable t) {
        logger.debug(message, t);
    }


    public void info(Object message) {
        logger.info(message);
    }

    public void info(Object message, Throwable t) {
        logger.info(message, t);
    }

    public void warn(Object message) {
        logger.warn(message);
    }

    public void warn(Object message, Throwable t) {
        logger.warn(message, t);
    }

    public void error(Object message) {
        logger.error(message);
    }

    public void error(Object message, Throwable t) {
        logger.error(message, t);
    }

    public void fatal(Object message) {
        logger.fatal(message);
    }

    public void fatal(Object message, Throwable t) {
        logger.fatal(message, t);
    }

    public void registerSessionInfo(String name, Object sessionInfo) {
        logger.registerSessionInfo(name, sessionInfo);
    }

    public void clearSessionInfo() {
        logger.clearSessionInfo();
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    /**
     * Constructs an instance with protocol, host and port.
     * Null values not allowed.
     *
     * @param protocol
     * @param host
     * @param port
     */
    private static String composeHostedServiceUrl(String protocol, String host, int port) {
        if (protocol == null || !protocol.matches("\\w[\\w-+.]*"))
            throw new IllegalArgumentException("protocol cannot be null and must match \"\\\\w[\\\\w-+.]+\"!");
        if (host == null || !host.matches("(\\w[\\w-]*)+(\\.\\w[\\w-]*)*"))
            throw new IllegalArgumentException("host cannot be null and must match \"(\\\\w[\\\\w-]*)+(\\\\.\\\\w[\\\\w-]*)*\"!");
        if (port < 0)
            throw new IllegalArgumentException("port number must be a positive number!");
        StringBuffer url = new StringBuffer();
        url.append(protocol.toLowerCase());
        url.append("://");
        url.append(host.toLowerCase());
        url.append(":");
        url.append(port);
        return url.toString();
    }

    static class HostedServiceAvailabilityLogContext extends BasicLogContext {


        HostedServiceAvailabilityLogContext(String hostedServiceUrl) {
            this(hostedServiceUrl, null);
        }

        protected HostedServiceAvailabilityLogContext(String hostedServiceUrl, String subname) {
            super("[HostedServiceAvailability;" + hostedServiceUrl + "]." + (subname != null && subname.length() > 0 ? subname : "*"));
        }

    }
}
