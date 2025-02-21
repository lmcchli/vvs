Folder inbox = null;
Vector msgList = null;

function getPrompt(id) {
    return PromptManager.getMedia(id,1) + ' ';
}

function getAudio(audio) {
    if (audio == 1)
        return "one_UL.wav";
    else
        return "two_UL.wav";
}

function attachSubscription() {
    if (TerminalSubscription != null) {
       if (inbox == null) {
            inbox = TerminalSubscription.getFolder("Inbox");
       }
    }
}

function getNumOfVoiceMsg(type) {
    if (TerminalSubscription != null) {
       attachSubscription();
       BitSet type = new BitSet();
       type.set(Message.VOICE);
       MessageFlags flagsAll = new MessageFlags();
       int voiceAll = inbox.getNumberOf(type, flagsAll);
    }
    else {
        return null;
    }
}


function getVoiceMsgMedia(folder, id) {

    try {
        attachSubscription();
         if (msgList == null) {
            msgList = new Vector();
            MessageFlags flagsAll = new MessageFlags();
            if (inbox == null) {
                return null;
            }
            inbox.getMessageList(msgList, type, flagsAll, Message.FIFO);
            if (id >= msgList.size()) {
                return null;
            }
            Message msg = (Message) msgList.elementAt(i);
            Vector bodyList = (Vector) msg.getHeader(Message.BODY);
            String body = bodyList.elementAt(0); // Get the first body only
            return (String) msg.getBody(body, Message.MEDIA);
         }
    } catch(DataException e) {
        System.err.println("DataException: " + e.getMessage());
        return null;
    } catch(SyntaxException e) {
        System.err.println("SyntaxException: " + e.getMessage());
        return null;
    } catch(SystemException e) {
        System.err.println("SystemException: " + e.getMessage());
        return null;
    } catch(TimeoutException e) {
        System.err.println("TimeoutException: " + e.getMessage());
        return null;
    }
}


function toStr(num) {
    return PromptManager.toString(num);
}


function getProfile(COS) {
    try {
         if (TerminalSubscription != null)
            return TerminalSubscription.getProfile(COS);
         else
            return null;
    }
    catch(DataException e) {
            System.err.println("DataException: " + e.getMessage());
    } catch(SyntaxException e) {
            System.err.println("SyntaxException: " + e.getMessage());
    } catch(SystemException e) {
            System.err.println("SystemException: " + e.getMessage());
    } catch(TimeoutException e) {
            System.err.println("TimeoutException: " + e.getMessage());
    }
    return null; // Or some other simple error indicator for the VXML developer
}

