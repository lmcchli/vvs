var inbox = null;
var trash = null;

var type = null;


// Return the call type based on incomign call parameters
function callType() {
    var callType = -1;
    if (dnis == depositNumber)
        callType = 2;		// DepositAskMboxId
    else if (dnis == ivrToSms)
        callType = 3;		// IVR to SMS
    else if (rdnis > '')
        callType = 1;		// Deposit
    else if (ani > '')
        callType = 0;		// Retrival
    else
        callType = 4;		// Ask Deposit Or Retrival

    return callType;
}

// Retrieve the specified prompt from the PromptManager, Using ENGLISH (1) as language
function getPrompt(id) {
    return "wav/UM_" + id + ".wav";
}

// Play number
function playNumber(id) {
    return "wav/" + id + ".wav";
}

function distributionListActive(count) { 
    return false; 
}

function callCoverageActive(count) { 
    return false; 
}

function loginOptionsActive(count) { 
    count++;
    return true; 
} 

function notificationActive(count) { 
    count++;
    return true; 
} 

function messagePlayActive(count) { 
    count++;
    return true; 
} 

function timeSettingsActive(count) { 
    count++;
    return true; 
} 

function defaultFaxActive(count) { 
    count++;
    return true; 
} 

function pauseActive(count) { 
    count++;
    return true; 
} 

function previousMenuActive(count) { 
    count++;
    return true; 
} 

function moreOptionsActive(nr, max) { 
    return nr < max; 
} 

function playPrompt(nr, max, services) {
    if (nr == services - 1)
        return true;
    else
        return nr < max;		
}

function getNofRowsToPlay() {
    return 3;		
}

// Convert the specified num to the corresponding string
function toStr(num) {
    return num;
}

// Get the COS for the current mailbox/user
function getProfile(COS) {
    return null; // Or some other simple error indicator for the VXML developer
}


// Retrieve the Inbox and Trash folders from the backend
function attachSubscription() {
}

// Retrieve the number of messages in the specified folder, using the default set
// of message flags
function getNumOfVoiceMsg(folder) {
        return 15;
}


// Retrieve the message media descriptor for the specified message in the specified folder
function getVoiceMsgMedia(folder, id) {

  
}

// Store the specified msgMedia as a new message in the specified folder
function storeMsg(msgMedia) {
    // STORE MESSAGE
}

// Rewind the specifec num sec. in the current playing mediafile
function rewind(numSec) {
    // Not implemented yet
}

// Fast-forward the specifec num sec. in the current playing mediafile
function forward(numSec) {
    // Not implemented yet
}

// Pause the recording/playout on the specified stream
function pause(stream) {
    // Not implemented yet
}
// Interrupt the recording/playout on the specifed stream
function interrupt(stream) {
    // Not implemented yet
}

// Retrieve the input stream
function getInStream() {
    // Not implemented yet
}

// Return the output stream
function getOutStream() {

    // Not implemented yet
}




