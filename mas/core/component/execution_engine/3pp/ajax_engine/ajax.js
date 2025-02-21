/// ajax.js
// Common Javascript methods and global objects
// Ajax framework for Internet Explorer (6.0, ...) and Firefox (1.0, ...)
// Copyright (c) by Matthias Hertel, http://www.mathertel.de
// This work is licensed under a BSD style license. See http://www.mathertel.de/License.aspx
// More information on: http://ajaxaspects.blogspot.com/ and http://ajaxaspekte.blogspot.com/
// -----
// 05.06.2005 created by Matthias Hertel.
// 19.06.2005 minor corrections to webservices.
// 25.06.2005 ajax action queue and timing.
// 02.07.2005 queue up actions fixed.
// 10.07.2005 ajax.timeout
// 10.07.2005 a option object that is passed from ajax.Start() to prepare() is also queued.
// 10.07.2005 a option object that is passed from ajax.Start() to prepare(), finish()
//            and onException() is also queued.
// 12.07.2005 correct xml encoding when CallSoap()
// 20.07.2005 more datatypes and XML Documents 
// 20.07.2005 more datatypes and XML Documents fixed
// 06.08.2005 caching implemented.
// 07.08.2005 bugs fixed, when queuing without a delay time.
// 04.09.2005 bugs fixed, when entering non-multiple actions.
// 07.09.2005 proxies.IsActive added
// 27.09.2005 fixed error in handling bool as a datatype
// 13.12.2005 WebServices with arrays on strings, ints, floats and booleans - still undocumented
// 27.12.2005 fixed: empty string return values enabled.
// 27.12.2005 enable the late binding of proxy methods.
// 21.01.2006 void return bug fixed.
// 18.02.2006 typo: Finsh -> Finish.
// 25.02.2006 better xmlhttp request object retrieval, see http://blogs.msdn.com/ie/archive/2006/01/23/516393.aspx
// 22.04.2006 progress indicator added.
// 28.01.2006 void return bug fixed again?
// 09.03.2006 enable late binding of prepare and finish methods by using an expression.
// 14.07.2006 Safari Browser Version 2.03/Mac OS X 10.4. compatibility: xNode.textContent || xNode.innerText || xNode.text || xNode.childNodes[0].nodeValue
// 10.08.2006 date to xml format fixed by Kars Veling
// 16.09.2006 .postUrl -- unfinished...
// 26.11.2006 enable null for xml based objects
// 19.05.2007 enabling ajax engine calls with multiple parameters

// ----- global variable for the proxies to webservices. -----

/// <summary>The root object for the proxies to webservices.</summary>
var proxies = new Object();

proxies.current = null; // the current active webservice call.
proxies.xmlhttp = null; // The current active xmlhttp object.
proxies.soapversion = "1.2";  //The version is 1.2 by default  
proxies.soapns = "http://www.w3.org/2003/05/soap-envelope";
if(proxies.soapversion != "1.2") {
    proxies.soapns = "http://schemas.xmlsoap.org/soap/envelope/";
}

proxies.readtimeout = 0; // The timeout value to be set in the xmlhttp
proxies.httpstatus = 0;  // The http result code of the last request

// ----- global variable for the ajax engine. -----

/// <summary>The root object for the ajax engine.</summary>
var ajax = new Object();

ajax.current = null; /// The current active AJAX action.
ajax.option = null; /// The options for the current active AJAX action.

ajax.queue = new Array(); /// The pending AJAX actions.
ajax.options = new Array(); /// The options for the pending AJAX actions.

ajax.timer = null; /// The timer for delayed actions.

ajax.progress = false; /// show a progress indicator
ajax.progressTimer = null; /// a timer-object that help displaying the progress indicator not too often.

// ----- AJAX engine and actions implementation -----

///<summary>Start an AJAX action by entering it into the queue</summary>
ajax.Start = function (action, options) {
  ajax.Add(action, options);
  // check if the action should start
  if ((ajax.current == null) && (ajax.timer == null))
    ajax._next(false);
} // ajax.Start


///<summary>Start an AJAX action by entering it into the queue</summary>
ajax.Add = function (action, options) {
  if (action == null) {
    alert("ajax.Start: Argument action must be set.");
    return;
  } // if
  
  // enable the late binding of the methods by using a string that is evaluated.
  if (typeof(action.call) == "string") action.call = eval(action.call);
  if (typeof(action.prepare) == "string") action.prepare = eval(action.prepare);
  if (typeof(action.finish) == "string") action.finish = eval(action.finish);

  if ((action.queueClear != null) && (action.queueClear == true)) {
    ajax.queue = new Array();
    ajax.options = new Array();

  } else if ((ajax.queue.length > 0) && ((action.queueMultiple == null) || (action.queueMultiple == false))) {
    // remove existing action entries from the queue and clear a running timer
    if ((ajax.timer != null) && (ajax.queue[0] == action)) {
      clearTimeout(ajax.timer);
      ajax.timer = null;
    } // if
    
    var n = 0;
    while (n < ajax.queue.length) {
      if (ajax.queue[n] == action) {
        ajax.queue.splice(n, 1);
        ajax.options.splice(n, 1);
      } else {
        n++;
      } // if
    } // while
  } // if
  
  if ((action.queueTop == null) || (action.queueTop == false)) {
    // to the end.
    ajax.queue.push(action);
    ajax.options.push(options);

  } else {
    // to the top
    ajax.queue.unshift(action);
    ajax.options.unshift(options);
  } // if
} // ajax.Add


///<summary>Check, if the next AJAX action can start.
///This is an internal method that should not be called from external.</summary>
///<remarks>for private use only.<remarks>
ajax._next = function (forceStart) {
  var ca = null // current action
  var co = null // current opptions
  var data = null;

  if (ajax.current != null)
    return; // a call is active: wait more time

  if (ajax.timer != null)
    return; // a call is pendig: wait more time

  if (ajax.queue.length == 0)
    return; // nothing to do.

  ca = ajax.queue[0];
  co = ajax.options[0];
  if ((forceStart == true) || (ca.delay == null) || (ca.delay == 0)) {
    // start top action
    ajax.current = ca;
    ajax.queue.shift();
    ajax.option = co;
    ajax.options.shift();

    // get the data
    if (ca.prepare != null)
      try {
        data = ca.prepare(co);
      } catch (ex) { }

    if (ca.call != null) {
      ajax.StartProgress();

      // start the call
      ca.call.func = ajax.Finish;
      ca.call.onException = ajax.Exception;
      if ((data.constructor == Array) && (data.multi != null)) // 19.05.2007
        ca.call.apply(ca, data);
      else
        ca.call(data);
      // start timeout timer
      if (ca.timeout != null)
        ajax.timer = setTimeout(ajax.Cancel, ca.timeout * 1000);

    } else if (ca.postUrl != null) {
      // post raw data to URL

    } else {
      // no call
      ajax.Finish(data);
    } // if
    
  } else {
    // start a timer and wait
    ajax.timer = setTimeout(ajax.EndWait, ca.delay);
  } // if
} // ajax._next


///<summary>The delay time of an action is over.</summary>
ajax.EndWait = function() {
  ajax.timer = null;
  ajax._next(true);
} // ajax.EndWait


///<summary>The current action timed out.</summary>
ajax.Cancel = function() {
  proxies.cancel(false); // cancel the current webservice call.
  ajax.timer = null;
  ajax.current = null;
  ajax.option = null;
  ajax.EndProgress();
  setTimeout(ajax._next, 200); // give some to time to cancel the http connection.
} // ajax.Cancel


///<summary>Finish an AJAX Action the normal way</summary>
ajax.Finish = function (data) {
  // clear timeout timer if set
  if (ajax.timer != null) {
    clearTimeout(ajax.timer);
    ajax.timer = null;
  } // if

  // use the data
  try {
    if ((ajax.current != null) && (ajax.current.finish != null))
      ajax.current.finish(data, ajax.option);
  } catch (ex) { }
  // reset the running action
  ajax.current = null;
  ajax.option = null;
  ajax.EndProgress();
  ajax._next(false)
} // ajax.Finish


///<summary>Finish an AJAX Action with an exception</summary>
ajax.Exception = function (ex) {
  // use the data
  if (ajax.current.onException != null)
    ajax.current.onException(ex, ajax.option);

  // reset the running action
  ajax.current = null;
  ajax.option = null;
  ajax.EndProgress();
} // ajax.Exception


///<summary>Clear the current and all pending AJAX actions.</summary>
ajax.CancelAll = function () {
  ajax.Cancel();
  // clear all pending AJAX actions in the queue.
  ajax.queue = new Array();
  ajax.options = new Array();
} // ajax.CancelAll


// ----- simple http-POST server call -----

ajax.postData = function (url, data, func) {
  var x = proxies._getXHR();
 
  x.open("POST", url, (func != null));

  if (func != null) {
    // async call with xmlhttp-object as parameter
    x.onreadystatechange = func;
    x.send(data);

  } else {
    // sync call
    x.send(soap);
    return(x.responseText);
  } // if
} // ajax.postData


///<summary>Execute a soap call.
///Build the xml for the call of a soap method of a webservice
///and post it to the server.</summary>
proxies.callSoap = function (args) {
  var p = args.callee;
  var x = null;
  proxies.httpstatus = 0;
  
  // check for existing cache-entry
  if (p._cache != null) {
    if ((p.params.length == 1) && (args.length == 1) && (p._cache[args[0]] != null)) {
      if (p.func != null) {
        p.func(p._cache[args[0]]);
        return(null);
      } else {
        return(p._cache[args[0]]);
      } // if
    } else {
      p._cachekey = args[0];
    }// if
  } // if

  proxies.current = p;
  x = proxies._getXHR();
  proxies.xmlhttp = x;
  x.setReadTimeout(proxies.readtimeout);

  // envelope start
  
  var soap = "<?xml version='1.0' encoding='utf-8'?>"
    + "<soap:Envelope xmlns:soap='"+ proxies.soapns +"'>"
    + "<soap:Body>"
    + "<" + p.fname + " xmlns='" + p.service.ns + "'>";

  // parameters    
  for (var n = 0; (n < p.params.length) && (n < args.length); n++) {
    var val = args[n];
    var typ = p.params[n].split(':');
    
    if ((typ.length == 1) || (typ[1] == "string")) {
      val = String(args[n]).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

    } else if (typ[1] == "int") {
      val = parseInt(args[n]);
    } else if (typ[1] == "float") {
      val = parseFloat(args[n]);

    } else if ((typ[1] == "x") && (typeof(args[n]) == "string")) {
      val = args[n];

    } else if ((typ[1] == "x") && (typeof(XMLSerializer) != "undefined")) {
      val = (new XMLSerializer()).serializeToString(args[n].firstChild);

    } else if (typ[1] == "x") {
      if (args[n] != null)
        val = args[n].xml;

    } else if ((typ[1] == "bool") && (typeof(args[n]) == "string")) {
      val = args[n].toLowerCase();
      
    } else if (typ[1] == "bool") {
      val = String(args[n]).toLowerCase();

    } else if (typ[1] == "date") {
      // calculate the xml format for datetime objects from a javascript date object
      var s, ret;
      ret = String(val.getFullYear());
      ret += "-";
      s = String(val.getMonth() + 1);
      ret += (s.length == 1 ? "0" + s : s);
      ret += "-";
      s = String(val.getDate());
      ret += (s.length == 1 ? "0" + s : s);
      ret += "T";
      s = String(val.getHours());
      ret += (s.length == 1 ? "0" + s : s);
      ret += ":";
      s = String(val.getMinutes());
      ret += (s.length == 1 ? "0" + s : s);
      ret += ":";
      s = String(val.getSeconds());
      ret += (s.length == 1 ? "0" + s : s);
      val = ret;

    } else if (typ[1] == "s[]") {
      val = "<string>" + args[n].join("</string><string>") + "</string>";

    } else if (typ[1] == "int[]") {
      val = "<int>" + args[n].join("</int><int>") + "</int>";

    } else if (typ[1] == "float[]") {
      val = "<float>" + args[n].join("</float><float>") + "</float>";

    } else if (typ[1] == "bool[]") {
      val = "<boolean>" + args[n].join("</boolean><boolean>") + "</boolean>";

    } // if
    soap += "<" + typ[0] + ">" + val + "</" + typ[0] + ">"
  } // for

  // envelope end
  soap += "</" + p.fname + ">"
    + "</soap:Body>"
    + "</soap:Envelope>";

  // enable cookieless sessions:
  var u = p.service.url;
 
  x.open("POST", u, (p.func != null));
  
  if( proxies.soapversion == "1.1")
  {
    x.setRequestHeader("SOAPAction", p.action);
    x.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
  } else {
    x.setRequestHeader("Content-Type", "application/soap+xml; charset=utf-8");
  }

  if (p.corefunc != null) {
    // async call with xmlhttp-object as parameter
    x.onreadystatechange = p.corefunc;
    x.send(soap);

  } else if (p.func != null) {
    // async call
    x.onreadystatechange = proxies._response;
    x.send(soap);

  } else {
    // sync call
    x.send(soap);
    return(proxies._response());
  } // if
} // proxies.callSoap


// cancel the running webservice call.
// raise: set raise to false to prevent raising an exception
proxies.cancel = function(raise) {
  var cc = proxies.current;
  var cx = proxies.xmlhttp;
  
  if (raise == null) raise == true;
  
  if (proxies.xmlhttp != null) {
    proxies.xmlhttp.onreadystatechange = function() { };
    proxies.xmlhttp.abort();
    if (raise && (proxies.current.onException != null))
      proxies.current.onException("WebService call was canceled.")
    proxies.current = null;
    proxies.xmlhttp = null;
  } // if
} // proxies.cancel


// px is a proxies.service.func object !
proxies.EnableCache = function (px) {
  // attach an empty _cache object.
  px._cache = new Object();
} // proxies.EnableCache


// check, if a call is currently waiting for a result
proxies.IsActive = function () {
  return(proxies.xmlhttp != null);
} // proxies.IsActive


///<summary>Callback method for a webservice call that dispatches the response to servive.func or service.onException.</summary>
///<remarks>for private use only.<remarks>
proxies._response = function () {
  var ret = new XML();
  var x = proxies.xmlhttp;
  var cc = proxies.current;
  var rtype = null;
  
  proxies.httpstatus = x.status;
  
  if ((cc.rtype.length > 0) && (cc.rtype[0] != null))
    rtype = cc.rtype[0].split(':');
    
  if ((x != null) && (x.readyState == 4)) {
    if (x.status == 200) {
      ret = x.responseXML;
   
      // store to _cache
      if ((cc._cache != null) && (cc._cachekey != null)) {
        cc._cache[cc._cachekey] = ret;
        cc._cachekey = null;
      } // if
      
      proxies.xmlhttp = null;
      proxies.current = null;

      if (cc.func == null) {
        return(ret); // sync
      } else {
        cc.func(ret); // async 
        return(null);
      } // if

    } else if (x.status == 400 || x.status == 500) {
        ret = x.responseXML;
    } else {
      //should not get here
    } // if
    
    proxies.xmlhttp = null;
    proxies.current = null; 
    
  } // if
  
  return(ret);
  
} // proxies._response


///<summary>Callback method to show the result of a soap call in an alert box.</summary>
///<remarks>To set up a debug output in an alert box use:
///proxies.service.method.corefunc = proxies.alertResult;</remarks>
proxies.alertResult = function () {
  var x = proxies.xmlhttp;
  
  if (x.readyState == 4) {
    if (x.status == 200) {
     if (x.responseXML.documentElement.firstChild.firstChild.firstChild == null)
       alert("(no result)");
     else
       alert(x.responseXML.documentElement.firstChild.firstChild.firstChild.firstChild.nodeValue);

    } else if (x.status == 404) { alert("Error!\n\nThe webservice could not be found.");

    } else if (x.status == 500) {
      // a SoapException
      var ex = new Error();
      ex.name = "SoapException";
      var n = x.responseXML.documentElement.firstChild.firstChild.firstChild;
      while (n != null) {
        if (n.nodeName == "faultcode") ex.message = n.firstChild.nodeValue;
        if (n.nodeName == "faultstring") ex.description = n.firstChild.nodeValue;
        n = n.nextSibling;
      } // while
      alert("The server threw an exception.\n\n" + ex.message + "\n\n" + ex.description);
    
    } else if (x.status == 502) { alert("Error!\n\nThe server could not be found.");

    } else {
      // no classified response.
      alert("Result-Status:" + x.status + "\n" + x.responseText);
    } // if
    
    proxies.xmlhttp = null;
    proxies.current = null;
  } // if
} // proxies.alertResult


///<summary>Show all the details of the returned data of a webservice call.
///Use this method for debugging transmission problems.</summary>
///<remarks>To set up a debug output in an alert box use:
///proxies.service.method.corefunc = proxies.alertResponseText;</remarks>
proxies.alertResponseText = function () {
 if (proxies.xmlhttp.readyState == 4)
   alert("Status:" + proxies.xmlhttp.status + "\nRESULT:" + proxies.xmlhttp.responseText);
} // proxies.alertResponseText


///<summary>show the details about an exception.</summary>
proxies.alertException = function(ex) {
  var s = "Exception:\n\n";

  if (ex.constructor == String) {
    s = ex;
  } else {
    if ((ex.name != null) && (ex.name != ""))
      s += "Type: " + ex.name + "\n\n";
      
    if ((ex.message != null) && (ex.message != ""))
      s += "Message:\n" + ex.message + "\n\n";

    if ((ex.description != null) && (ex.description != "") && (ex.message != ex.description))
      s += "Description:\n" + ex.description + "\n\n";
  } // if
  alert(s);
} // proxies.alertException


///<summary>Get a browser specific implementation of the XMLHttpRequest object.</summary>
// from http://blogs.msdn.com/ie/archive/2006/01/23/516393.aspx
proxies._getXHR = function () {
  var x = null;

  x = new XMLHttpRequest();

  return(x);
} // proxies._getXHR


///<summary>Get a browser specific implementation of the XMLDOM object, containing a XML document.</summary>
///<param name="xmlText">the xml document as string.</param>
ajax._getXMLDOM = function (xmlText) {
  var obj = null;

  if ((document.implementation != null) && (typeof document.implementation.createDocument == "function")) {
    // Gecko / Mozilla / Firefox
    var parser = new DOMParser();
    obj = parser.parseFromString(xmlText, "text/xml");

  } else {    
    // IE
    try {
      obj = new ActiveXObject("MSXML2.DOMDocument");
    } catch (e) { }

    if (obj == null) {
      try {
        obj = new ActiveXObject("Microsoft.XMLDOM");
      } catch (e) { }
    } // if
  
    if (obj != null) {
      obj.async = false;
      obj.validateOnParse = false;
    } // if
    obj.loadXML(xmlText);
  } // if
  return(obj);
} // _getXMLDOM


///<summary>show the details of a javascript object.</summary> 
///<remarks>This helps a lot while developing and debugging.</remarks> 
function inspectObj(obj) {
  var s = "InspectObj:";

  if (obj == null) {
    s = "(null)"; alert(s); return;
  } else if (obj.constructor == String) {
    s = "\"" + obj + "\"";
  } else if (obj.constructor == Array) {
    s += " _ARRAY";
  } else if (typeof(obj) == "function") {
    s += " [function]" + obj;

  } else if ((typeof(XMLSerializer) != "undefined") && (obj.constructor == XMLDocument)) {
    s = "[XMLDocument]:\n" + (new XMLSerializer()).serializeToString(obj.firstChild);
    alert(s); return;

  } else if ((obj.constructor == null) && (typeof(obj) == "object") && (obj.xml != null)) {
    s = "[XML]:\n" + obj.xml;
    alert(s); return;
  }
  
  for (p in obj) {
    try {
      if (obj[p] == null) {
        s += "\n" + String(p) + " (...)";

      } else if (typeof(obj[p]) == "function") {
        s += "\n" + String(p) + " [function]";

      } else if (obj[p].constructor == Array) {
        s += "\n" + String(p) + " [ARRAY]: " + obj[p];
        for (n = 0; n < obj[p].length; n++)
          s += "\n  " + n + ": " + obj[p][n];

      } else {
        s += "\n" + String(p) + " [" + typeof(obj[p]) + "]: " + obj[p];
      } // if
    } catch (e) { s+= e;}
  } // for
  alert(s);
} // inspectObj

// ----- End -----

//Timers	

var timers = [];
var funktions = [];
ajax.setTimeout = function(fn, time) {
	var num;
	return num = setInterval(function() {
		fn();
		clearInterval(num);
	}, time);
};

ajax.clearTimeout = function(fn) {
	var num=0;
    for (var n in functions) {
    	if( n==fn) {
    		clearInterval(num);
    	}
    	num++;
    }
};

ajax.setInterval = function(fn, time) {
	var num = timers.length;

	functions[num]=fn;
	timers[num] = new java.lang.Thread(new java.lang.Runnable( {
		run : function() {
			while (true) {
				java.lang.Thread.currentThread().sleep(time);
				fn();
			}
		}
	}));

	timers[num].start();

	return num;
};

ajax.clearInterval = function(num) {
	if (timers[num]) {
		timers[num].stop();
		delete timers[num];
		timers.splice(num, 1);
		functions.splice(num,1);
	}
};

