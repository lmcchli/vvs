// BEGIN dummy code
profile = new Object();
// END dummy code



function isSubscriber(caller) {return true; }
function setPreferredLanguage(caller) { }
function mailBoxRestricted() { return false;}
function getTimeDependantPrompt(prompt_id) { return "greeting.wav"; }
function getDayString() { return "afternoon"; }
function hasFastLogin() { return true;}
function isReinlogged() { return false; }
function getCoSDependantPrompt(prompt_id, caller) {return "welcome.wav"; }
function getCoSDepandantString(string_id, caller) { 
	return "welcome to messaging service";
}
function getString(string_id) { 
	if(string_id == 'LIST_INVENTORY') {
		return "you have two messages";	
	}
}
function quotaOK(caller) { return true; }

function getListInventoryPrompt(caller) {
	nofUrgent = inbox.getNumberOf('VOICE', URGENT);
	nofNormal = inbox.getNumberOf('VOICE', NORMAL);
	
	if(nofUrgent == 0 && nofNormal == 0) {
		return "";	
	}
	if((nofUrgent + nofNormal) == 1) {
			
	}
	
}

function newFamilyMailboxMessageExist(caller) { return false; }
function nextMailBox(caller) { 
	static int i = 0;
	if(i++ > 2) return false;
	return true;
}

function activeRow(nr, max, reverse) {
	if(reverse)
		return nr > max;
	return nr < max;
}

function getFamilyMailboxListPrompt() { return "family_mailbox_list.wav"; }
function setActiveMailBox(input) { return true;  }
