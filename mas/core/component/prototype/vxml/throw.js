// Declare a compound Object - this would have been instanciated by the MAS 
// itself. This however is just a dummy object
function aa() {return "kalle";}
function bb() {return 4; }
var a = new Object();
a.getUid = aa;
a.getNofMessages = bb;
// end new OIbject 





// example of throw
function foo(a) {
	if( a == 1) 
		throw new Exception();
}


// example of catch. We need to catch all ECMA exceptions in ECMA; they cannot 
// be caught by voiceXML catch.
function bar(a) {

	try {
	foo(a);
	} catch(e) {
		return -1;
	}
	
	return 1;
}
