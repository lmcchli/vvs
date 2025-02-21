/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */

#ifndef NTFMIB_H
#define NTFMIB_H

#include "NtfObject.h"
#include "ConsumedService.h"
#include "ConsumedServiceInstance.h"
#include "CommonAlarm.h"
#include "IPMSMutex.h"
#include "def.h"
#include <sys/time.h>
#include <list>

using namespace std;

class NtfMib {
 public:
    // Destructor
    ~NtfMib();
    
    /**
     * Get the singleton NtfMiB instance.
     *@return the NtfMib instance.
     */
    static NtfMib* instance();
    
    /**
     * Get the main MIB attributes from NTF's MIB.
     * This method is synchronized by locking the MIB.
     *@return a struct with the attribute values.
     */
    NtfObject& getNtfObject();
    
    /**
     * Get the consumed service attributes for the specified index or higher.
     * This method is synchronized by locking the MIB.
     *@param index the first index to look for.
     *@return a consumed service if one was found at or after the specified
     * index and 0 otherwise.
     */
    ConsumedService* getConsumedService(int index);

    /**
     * Get the consumed service instance attributes for the specified indices or
     * higher. The service index is more significant than the instance index.
     * This method is synchronized by locking the MIB.
     *@param serviceIndex the first service index to look for.
     *@param instanceIndex the first instance index to look for.
     *@return a consumed service instance if one was found at or after the
     * specified index and 0 otherwise.
     */
    ConsumedServiceInstance* getConsumedServiceInstance(int serviceIndex, int instanceIndex);

    /**
     * Get the common alarm attributes for the specified index or higher.
     * This method is synchronized by locking the MIB.
     *@param index the first index to look for.
     *@return a common alarm if one was found at or after the specified
     * index and 0 otherwise.
     */
    CommonAlarm* getCommonAlarm(int index);

    /*
     * Get the mutex used to reserve the entire MIB.
     *@return the MIB mutex.
     */
    IPMSMutex& getMibMutex();

    /*
     * Creates a consumed service instance or sets its state to down.
     *@param serviceIndex the index of the service.
     *@param instanceIndex the index of the instance.
     */
    void downConsumedServiceInstance(int serviceIndex, int instanceIndex);

    /*
     * Sets the state of a consumed service instance to up.
     *@param serviceIndex the index of the service.
     *@param instanceIndex the index of the instance.
     */
    void upConsumedServiceInstance(int serviceIndex, int instanceIndex);

    /**
     * Sends a get request if it was long since the MIB information was
     * updated.
     *@return true if update was needed, and false otherwise.
     */
    bool checkUpdate();

    /**
     * Parses content (possibly partial) for the main attributes in NTF's MIB from
     * name-value pairs and puts into the NTF MIB's ntfObjects part.
     * This method is synchronized by locking the MIB. 
     *
     *@param entry - the attributes that should be updated. This is a vector of
     * name-value pairs.
     *@return true if the parse operation succeeded and false otherwise.
     */
    bool parseNtfObject(const AttributeVector& entry);

    /**
     * Parses content (possibly partial) for the main attributes in NTF's MIB from
     * name-value pairs and updates a consumed service.
     * This method is synchronized by locking the MIB. 
     *
     *@param entry - the attributes that should be updated. This is a vector of
     * name-value pairs.
     *@param index the index of the consumed service to update
     *@return true if the parse operation succeeded and false otherwise.
     */
    bool parseConsumedService(int index, const AttributeVector& entry);

    /**
     * Parses content (possibly partial) for the main attributes in NTF's MIB from
     * name-value pairs and updates a consumed service instance.
     * This method is synchronized by locking the MIB.
     *
     *@param entry - the attributes that should be updated. This is a vector of
     * name-value pairs.
     *@param serviceIndex the index of the consumed service of the instance.
     *@param instanceIndex the index of the consumed service instance to update
     *@return true if the parse operation succeeded and false otherwise.
     */
    bool parseConsumedServiceInstance(int serviceIndex, int instanceIndex,
                                      const AttributeVector& entry);

    /**
     * Parses content (possibly partial) for the main attributes in NTF's MIB from
     * name-value pairs and updates a common alarm.
     * This method is synchronized by locking the MIB. 
     *
     *@param entry - the attributes that should be updated. This is a vector of
     * name-value pairs.
     *@param index the index of the common alarm to update
     *@return true if the parse operation succeeded and false otherwise.
     */
    bool parseCommonAlarm(int index, const AttributeVector& entry);

    /**
     * Checks if the NTF MIB has been updated in the last 300 seconds.
     *@return true if the NTF MIB appears to be dead, fals otherwise.
     */
    bool ntfNotAnsweringOnRequest();

 private:
    NtfMib();
    /**
     * Get the consumed service entry with the specififed index, if necessary by
     * creating a new entry. The new entry has the correct value for the index
     * attribute, the other attributes will have default values.
     *@param index the first index to look for.
     *@return a consumed service with the specified index.
     */
    ConsumedService& getOrMakeConsumedService(int index);

    /**
     * Get the consumed service entry with the specififed indices, if necessary
     * by creating a new entry. The new entry has the correct value for the
     * serviceIndex and instanceIndex attributes, the other attributes will have
     * default values.
     *@param serviceIndex the first service index to look for.
     *@param instanceIndex the first instance index to look for.
     *@return a consumed service instance with the specified indices,
     */
    ConsumedServiceInstance& getOrMakeConsumedServiceInstance(int serviceIndex, int instanceIndex);


    /**
     * Get the common alarm entry with the specififed index, if necessary by
     * creating a new entry. The new entry has the correct value for the index
     * attribute, the other attributes will have default values.
     *@param index the first index to look for.
     *@return a common alarm with the specified index.
     */
    CommonAlarm& getOrMakeCommonAlarm(int index);

#ifdef linux
    time_t _nextUpdateTime;
    time_t _notRespondingTime;
#else
    hrtime_t _nextUpdateTime;
    hrtime_t _notRespondingTime; //Time before which NTF must have sent a response
#endif
    
    IPMSMutex _mibMutex;
    static NtfMib* _theMib;
    NtfObject _obj;
    list<ConsumedService> _serv;
    list<ConsumedServiceInstance> _servinst;
    list<CommonAlarm> _commonAlarms;
};

#endif
