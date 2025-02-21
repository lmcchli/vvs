importPackage(Packages.com.mobeon.backend)
importPackage(Packages.java.util)

var inbox = null;
var trash = null;
var inboxMsgList = new Vector();
var trashMsgList = new Vector();

var type = null;


// Retrieve the specified prompt from the PromptManager, Using ENGLISH (1) as language
function getPrompt(id) {
    return PromptManager.getMedia(id,1) + ' ';
}

// Convert the specified num to the corresponding string
function toStr(num) {
    return PromptManager.toString(num);
}

// Get the COS for the current mailbox/user
function getProfile(COS) {
    try {
         if (terminalsubscription != null)
            return terminalsubscription.getProfile(COS);
         else {
            print ("No terminal subscription set!");
            return null;
            }
    }
    catch(e) {
        print ("Exception caught in getProfile");
        print("Exception: " + e.getMessage());
    }
    print ("No such COS " + COS);

    return null; // Or some other simple error indicator for the VXML developer
}


// Retrieve the Inbox and Trash folders from the backend
function attachSubscription() {
    if (terminalsubscription != null) {
       if (inbox == null) {
            try {
                print ("Retrieveing inbox");
                inbox = terminalsubscription.getFolder("Inbox");
            }
            catch (e) {
                print("No inbox folder found");
            }
        }
        if (trash == null) {
            try {
                print ("Retrieveing trash");
                trash = terminalsubscription.getFolder("Trash");
            }
            catch (e) {
                print("No trash folder found");
            }
       }
    }
    else {
        print("No terminalsubscription!");
    }
}

// Retrieve the number of messages in the specified folder, using the default set
// of message flags
function getNumOfVoiceMsg(folder) {
    if (terminalsubscription != null) {
       attachSubscription();
       type = new BitSet();
       type.set(MASMessage.VOICE);
       var flags = new MessageFlags();
       if (folder == null) {
            print ("Folder " + folder + " is null");
            return 0;
       }
       return folder.getNumberOf(type, flags);
    }
    else {
        print ("No messages!");
        return null;
    }
}


// Retrieve the message media descriptor for the specified message in the specified folder
function getVoiceMsgMedia(folder, id) {

    try {
        attachSubscription();
        var location = folder.getFolder();
        print ("Location is " + location);
        if (inboxMsgList == null) {
            inboxMsgList = new Vector();
        }
        flagsAll = new MessageFlags();
        if (folder == null) {
            print ("Folder is null!");
            return null;
        }
        print ("Getting msg list");
        folder.getMessageList(inboxMsgList, type, flagsAll, MASMessage.FIFO);
        if (id >= inboxMsgList.size()) {
            print("Mailbox size:");
            print (id + " is outsize size of list : " + inboxMsgList.size());
            return null;
        }
        print ("Getting msg");
        var msg =  inboxMsgList.elementAt(id);
        print ("Getting bodyList");
        var bodyList =  msg.getHeader(MASMessage.BODY);
        print ("Getting body");
        var body = bodyList.elementAt(0); // Get the first body only
        print ("Getting media");
        media =   msg.getBody(body, MASMessage.MEDIA);
        if (media == null) {
            print ("Media is null!");
        }
        print ("Media is " + media);
        media = location + "../" + media;
        return media;
    } catch(e) {
        print ("Exception caught!");
        print("Exception: " + e);
        return null;
    }
}

// Store the specified msgMedia as a new message in the specified folder
function storeMsg(msgMedia) {
 // Not implemented
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
    stream.interrupt();
}

// Retrieve the input stream
function getInStream() {
   return traverser.getInputStream();
}

// Return the output stream
function getOutStream() {

    return traverser.getOutputStream();
}




