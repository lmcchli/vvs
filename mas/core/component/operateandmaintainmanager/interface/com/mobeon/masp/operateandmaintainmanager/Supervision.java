/*
* Copyright (c) 2005 Mobeon AB. All Rights Reserved.
*/

package com.mobeon.masp.operateandmaintainmanager;

/**
 * Interface that register provided services and consumed services to the OMM.
 *
 */
public interface Supervision {

    /**
     * Register a provided service to OMM.
     * @param ServiceName : Name of provided service to be registered.
     * @param Host : Host where service exsists
     * @param Port : Port where the service talk.
     * @param serviceEnablerOperate : ServiceEnablerOperate referense to the serviceenabler where the service is connected.
     * @return  ProvidedServiceEntry : ProvidedService
     */
    ProvidedService createProvidedServiceEntry(String ServiceName,String Host, Integer Port, ServiceEnablerOperate serviceEnablerOperate) throws IlegalServiceParametersException;
    //ProvidedService createProvidedServiceEntry(String ServiceName,String Host, int Port, String serviceEnablerName);
    /**
     * Register a consumed service to OMM.
     * @param ServiceName : Name of consumed service to be registered.
     * @param Host : Host where service exsists
     * @param Port : Port where the service talk.
     * @return ConsumedServiceEntry : ConsumedService
     */
    ConsumedService createConsumedServiceEntry(String ServiceName,String Host, int Port );


    /**
     * Creates a service enabler if id dosent exsist.
     * and returns a referense to the service enabler.
     * @param serviceEnablerRef
     * @return Referense to the service enabler
     */
  //  ServiceEnablerInfo createServiceEnablerStatistics( ServiceEnablerOperate serviceEnablerRef);


    /**
     * returns a referense to the service enabler.
     * @return Referense to the service enabler statistic
     */
    ServiceEnablerInfo getServiceEnablerStatistics(ServiceEnablerOperate serviceEnablerRef) throws Exception;

    /**
     * Retreives session factory from O&M to be able to prvide session information  to O&M.
     */
    public SessionInfoFactory getSessionInfoFactory();


}
