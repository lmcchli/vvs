var SUBSCRIBER_LOOKUP_ERROR = "Subscrber lookup error";

/**
 * #DisplayName PhoneNumber Get Analyzed Number
 * #Description This function will return the analyzed phone number.
 * #param rule rule The rule to use for the analysis.
 * #param phoneNumber phoneNumber The phone number to analyze.
 * #param callerPhoneNumber callerPhoneNumber The callers phone number.
 */
function PhoneNumber_GetAnalyzedNumber(rule, phoneNumber, callerPhoneNumber) {
    if (phoneNumber == undefined)
        return "";
    if (callerPhoneNumber == '') {
        if (phoneNumber == "") {
            return "";
        }
        else {
            return mas.systemAnalyzeNumber(rule, phoneNumber, null);
        }
    }
    else
       return mas.systemAnalyzeNumber(rule, phoneNumber, callerPhoneNumber); 
}

/**
 * #DisplayName PhoneNumber Is Retrieval DNIS
 * #Description Returns true if the given phone number matches that of the Retrieval DNIS; otherwise, returns false.
 * #param phoneNumber phoneNumber The phone number to analyze.
 */
function PhoneNumber_IsRetrievalDNIS(phoneNumber) {
	return phoneNumber == System_GetConfig("vmpa.incomingcall", "retrievaldnis");
}

/**
 * #DisplayName PhoneNumber Remove Short Code
 * #Description Returns phone number after removing short code prefix. If no short code prefix, returns the original phone number.
 * #param phoneNumber phoneNumber The phone number to analyze.
 */
function PhoneNumber_RemoveShortCode(phoneNumber) {
	var number = phoneNumber;
	var shortCodeLength = System_GetConfig("vmpa.incomingcall", "shortcodelength");
	if((PhoneNumber_StartsWithShortCode(phoneNumber)) && (phoneNumber.length > shortCodeLength)){
		number = phoneNumber.substring(shortCodeLength);
	}	
	return number;
}

/**
 * #DisplayName PhoneNumber Starts With Short Code
 * #Description Returns true if the given phone number starts with a short code; otherwise, returns false.
 * #param phoneNumber phoneNumber The phone number to analyze.
 */
function PhoneNumber_StartsWithShortCode(phoneNumber) {
	var startsWithShortCode = false;
	var shortCodePrefix = System_GetConfig("vmpa.incomingcall", "shortcodeprefix");
	if((shortCodePrefix != '') && (phoneNumber.indexOf(shortCodePrefix) == 0)){
		startsWithShortCode = true;
	}	
	return startsWithShortCode;
}

/**
 * #DisplayName System Get Opco Name
 * #Description Returns the OPCO to which the given phone number belongs.
 * #param phoneNumber The phone number to search with.
 */
function Subscriber_GetOpcoName(phoneNumber) {
	var name = undefined;
	try{
		name = mas.subscriberGetOperatorName(phoneNumber);
	}
	catch(err){
		if (new String(err).indexOf('error.com.mobeon.platform.system') > -1){
			name = SUBSCRIBER_LOOKUP_ERROR;
		}
	}
	return name;
}

/**
 * #Displayname System Get Config
 * #Description Returns the value of the specified parameter.
 * #param section section The section for the configuration parameter.
 * #param parameterName parameterName The name of the configuration parameter.
 */
function System_GetConfig(section, parameterName) {
	return mas.systemGetConfigurationParameter(section, parameterName);
}

/**
 * #DisplayName System Get Proxy Server Address
 * #Description Returns the IP address for the given serverName.
 * #param serverName The serverName for which the address is requested.
 */
function System_GetProxyServerAddress(serverName) {
	return mas.systemGetConfigurationTableParameter("vmpa", "ProxyServers.Table", serverName, "serverAddress");
}

/**
 * #DisplayName System Get Proxy Server Port
 * #Description Returns the port for the given serverName.
 * #param serverName The serverName for which the port is requested.
 */
function System_GetProxyServerPort(serverName) {
	return mas.systemGetConfigurationTableParameter("vmpa", "ProxyServers.Table", serverName, "serverPort");
}

/**
 * #DisplayName System Is Migration Enabled
 * #Description Returns true if migration mode is enabled in configuration.
 */
function System_IsMigrationEnabled(){
	return System_GetConfig("vmpa.proxy", "migrationenabled") == "yes";
}

/**
 * #Displayname System Set Partition Restriction
 * #Description Set a restriction for searches in User Directory to be only in the local sub/domain for example when using iMux.
 * #param restrict restrict A boolean that defines if searches shall be restricted or not.
 */
function System_SetPartitionRestriction(restrict) {
    mas.systemSetPartitionRestriction(restrict);
}

//********** MasInterface stubs *********************

var mas = new PlatformAccess();

function PlatformAccess() {
	this.systemAnalyzeNumber = systemAnalyzeNumber;
	this.systemGetConfigurationParameter = systemGetConfigurationParameter;
	this.systemGetConfigurationTableParameter = systemGetConfigurationTableParameter;
	this.subscriberGetOperatorName = subscriberGetOperatorName;
	this.systemSetPartitionRestriction = systemSetPartitionRestriction;
}

function systemSetPartitionRestriction(restrict){	
}

//Default test values
var testretrievaldnis = "6121121";
var testshortcodeprefix = "121";
var testshortcodelength = "4";
var testmigrationenabled = "yes";
var testserveraddress = "172.123.45";
var testserverport = "2222";
var testopconame = "opco1";
var testanalyzedphonenumber = undefined;
var isTestPhoneNumberSet = false;

function systemAnalyzeNumber(rule, phoneNumber, callerPhoneNumber) {
    if(isTestPhoneNumberSet)
    	return testanalyzedphonenumber;
    else
    	return phoneNumber;
}

function systemGetConfigurationParameter(group, parameterName) {
	if (parameterName == "retrievaldnis")
		return testretrievaldnis;
	else if (parameterName == "shortcodeprefix")
		return testshortcodeprefix;
	else if (parameterName == "shortcodelength")
		return testshortcodelength;
	else if (parameterName == "migrationenabled")
		return testmigrationenabled;
	else
		return "unknown configuration parameter";
}

function systemGetConfigurationTableParameter(application, table, tableItemKey, parameterName){
	if (parameterName == "serverAddress")
		return testserveraddress;
	else if (parameterName == "serverPort")
		return testserverport;
	else
		return "unknown configuration parameter";
}

function subscriberGetOperatorName(phoneNumber){
	if(testopconame == SUBSCRIBER_LOOKUP_ERROR){
		throw "error.com.mobeon.platform.system";
	}
	return testopconame;
}

//Used to set up test cases
function setTestValue(testVariable, testValue){
	if(testVariable == "testretrievaldnis")
		testretrievaldnis = testValue;
	else if(testVariable == "testshortcodeprefix")
		testshortcodeprefix = testValue;
	else if(testVariable == "testshortcodelength")
		testshortcodelength = testValue;
	else if(testVariable == "testmigrationenabled")
		testmigrationenabled = testValue;
	else if(testVariable == "testserveraddress")
		testserveraddress = testValue;
	else if(testVariable == "testserverport")
		testserverport = testValue;
	else if(testVariable == "testopconame")
		testopconame = testValue;
	else if(testVariable == "testanalyzedphonenumber"){
		isTestPhoneNumberSet = true;
		testanalyzedphonenumber = testValue;
	}
}