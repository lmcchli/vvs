/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.util.component;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * An factory creating objects based on a componentSpecification and possibly a
 * class type. The implementation serve as a facade to the Spring framework.
 * The configuration is specified in the file(s) ComponentConfig.xml, which shall be located in the
 * classpath. The configuration may be split in multiple files, all with the same name, but located in
 * different places in the classpath
 *
 * @author David Looberger
 */
public class SpringComponentManager implements IComponentManager {
    private static IComponentManager instance = null;
    private static ApplicationContext ctx = null;
    private ILogger logger = ILoggerFactory.getILogger(SpringComponentManager.class);

    public static void initialApplicationContext(ApplicationContext appCtx) {
        if (ctx == null)
            ctx = appCtx;
        else {
            final ILogger iLogger = ILoggerFactory.getILogger(SpringComponentManager.class);
            if(iLogger.isDebugEnabled())
                iLogger.debug("initialApplicationContext has already been declared once with: "+ctx);
        }
    }

    private ApplicationContext ctx() {
        if(ctx == null)
        	try {
        		ctx = new ClassPathXmlApplicationContext("file:/opt/moip/config/mas/ComponentConfig.xml");
        	} catch (Throwable t) {
        		logger.error("Exception while initialising ClassPathXmlApplicationContext ", t);
        	}
        return ctx;
    }
    /**
     * Create and return an object (bean) corresponding to componentSpecifaction.
     *
     * @param componentSpecification The name identifying the bean to create.
     * Depending on the configuration read by the component manager,
     * the returned object will be a singleton or a template.
     * @return The Object
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException - if there's no such bean definition
     * @throws org.springframework.beans.BeansException - if the bean could not be created
     */
    public Object create(String componentSpecification) {
        return ctx().getBean(componentSpecification);
    }

     /**
     * Create and return an object (bean) corresponding to componentSpecifaction and a specified type.
     *
     * @param componentSpecification The name identifying the bean to create
     * @return The Object
     * @throws org.springframework.beans.factory.BeanNotOfRequiredTypeException - if the bean is not of the required type
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException - if there's no such bean definition
     * @throws org.springframework.beans.BeansException - if the bean could not be created
     */
    @SuppressWarnings("unchecked")
	public Object create(String componentSpecification, Class clazz) {
        return ctx().getBean(componentSpecification, clazz);
    }

    public static synchronized IComponentManager getInstance() {
        if (instance == null) {
            instance = new SpringComponentManager();
        }
        return instance;
    }
}
