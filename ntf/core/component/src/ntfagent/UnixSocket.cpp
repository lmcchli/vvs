/*
  File:		UnixSocket.cpp
  Originated:	2004-01-01
  Author:	Joakim Nilsson
  Signature:	ermjnil


  Copyright (c) 2004 MOBEON AB

  The copyright to the computer program(s) herein is the property of 
  MOBEON AB, Sweden. The programs may be used and/or copied only 
  with the written permission from MOBEON AB or in accordance with 
  the terms and conditions stipulated in the agreement/contract under 
  which the programs have been supplied.
*/

#include <errno.h>
#include <string.h>
#include <arpa/inet.h>
#include <netdb.h>
#include "IPMSThread.h"
#include "UnixSocket.h"
#include "Logger.h"
#include "def.h"

UnixSocket::UnixSocket(int port, string host, t_socket stype) {

    _port = port;
    if( host.empty() ) {
    	_host = "";
        _sendHost = "127.0.0.1";		
    } else {
    	_host = host;
    	_sendHost = host;
    }
    _socketType = stype;
    _isConnBroken = true;
    FD_ZERO(&_readset);
    FD_ZERO(&_writeset);
    _socket_fd = -1;
    _name.append("UnixSocket");
    
    memset(&_s_in, 0, sizeof(_s_in));
    _s_in.sin_family = AF_INET;
    _s_in.sin_port = htons(0);
    _s_in.sin_addr.s_addr = ipaddr2unsloint(_host); 
}

UnixSocket::~UnixSocket() {
    closeSocket();
}

int
UnixSocket::openSocket() {

    int on = 1;

    // Create UDP socket 
    if ((_socket_fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        int e = errno;
        LOG(Logger::ERROR, "UnixSocket", string("Failed to open socket: ") + strerror(e));
        return SYSTEM_ERROR;
    }
    
    // Client
    if (_socketType == UnixSocket::client) {
	if (setsockopt(_socket_fd, SOL_SOCKET, SO_BROADCAST, (void *)&on, sizeof(on)) < 0) {
            int e = errno;
            LOG(Logger::ERROR, "UnixSocket", string("Client failed to set socket options: ") + strerror(e));
	    _errmsg = strerror(e);
	    return SYSTEM_ERROR;
	} 
	if (bind(_socket_fd, (struct sockaddr *) &_s_in, sizeof(_s_in)) < 0) {
            int e = errno;
            LOG(Logger::ERROR, "UnixSocket", string("Client failed to bind: ") + strerror(e));
	    return SYSTEM_ERROR;
        }
    }
    // Server
    else {
	if (setsockopt(_socket_fd, SOL_SOCKET, SO_REUSEADDR,
		       (void *)&on, sizeof(on)) < 0) {
            int e = errno;
            LOG(Logger::ERROR, "UnixSocket", string("Server failed to set socket options: ") + strerror(e));
	    return SYSTEM_ERROR;
        }
	
	if (bind(_socket_fd, (struct sockaddr *) &_s_in, sizeof(_s_in)) < 0) {
            int e = errno;
            LOG(Logger::ERROR, "UnixSocket", string("Server failed to bind: ") + strerror(e));
	    return SYSTEM_ERROR;
        //	_port = _s_in.sin_port;
        }
    }
    return SUCCESSFUL;
}


int 
UnixSocket::closeSocket() {

     if (_socket_fd < 0) {
	_isConnBroken = true;
	return SUCCESSFUL;
    }
    
    if (close(_socket_fd) < 0)
	return SYSTEM_ERROR;
    _isConnBroken = true;

    FD_ZERO(&_writeset);
    FD_ZERO(&_readset);
    _socket_fd = -1;
    
    return SUCCESSFUL;
}

int 
UnixSocket::send(string data) {
	return send(data, _sendHost);
}

int
UnixSocket::send(string data, string host) {
    
    return send(data, host, _port);
}

int
UnixSocket::send(string data, string host, int port) {

    int ret;
    int flag = MSG_DONTROUTE;
    int fdcount;
    int maxfd;

    if (_socket_fd < 0) {
        openSocket();
        if (_socket_fd < 0) {
            return SYSTEM_ERROR;
        }
    }

    LOG(Logger::INFORMATION, "UnixSocket", "Sending " + data + " to "
        + host + ":" + int2string(port));
    
    struct timeval t_wait;
    t_wait.tv_sec = 1; // timeout time in seconds.
    t_wait.tv_usec = 5000; // timeout time in micro seconds.

    struct sockaddr_in s_in;
    memset(&s_in, 0, sizeof(_s_in));
    s_in.sin_family = AF_INET;
    s_in.sin_addr.s_addr = ipaddr2unsloint(host);
    s_in.sin_port = htons(port); 

    // Listen to socket fd to see if it's ready for writing
    errno = 0;
    FD_ZERO(&_writeset);
    FD_SET(_socket_fd, &_writeset);
    maxfd = _socket_fd + 1;
    if ((fdcount = select(maxfd, NULL, &_writeset, NULL, &t_wait)) > 0) {
	// Send message over the socket connection
	if ((ret = sendto(_socket_fd, data.c_str(), strlen(data.c_str()), flag,
			  (struct sockaddr *) &s_in, sizeof(s_in))) == -1)
	    { 
		_errmsg = strerror(errno);
		_isConnBroken = true;
		return SYSTEM_ERROR;
	    }
    }
    else {
	// If timeout
	if (fdcount == 0)
	    _errmsg = "Timeout when sending message.";
	
	// If connection is reset by peer
	if (fdcount == -1) {
	    _errmsg = strerror(errno);
	    _isConnBroken = true;
	}
	return SYSTEM_ERROR;
    }
    return SUCCESSFUL;
    
}

int
UnixSocket::receive(string &data) {
    char msgBuf[16384];
    socklen_t num;
    int fdcount;
    int maxfd;
    socklen_t addrlen;
    bool eofMsgFlag;

    if (_socket_fd < 0) {
        //Wait for send part to open the socket.
        IPMSThread::sleep(1000);
        return SUCCESSFUL;
    }

    struct timeval t_wait;
    t_wait.tv_sec = 0; // timeout time in seconds.
    t_wait.tv_usec = 500000; // timeout time in micro seconds.
    maxfd = _socket_fd + 1;

    struct sockaddr_in req_addr;
    
    eofMsgFlag = false;
    msgBuf[0] = '\0';
    addrlen = sizeof(req_addr);

    msgBuf[0] = '\0';
    errno = 0;
    FD_ZERO(&_readset);
    FD_SET(_socket_fd, &_readset);
    
   
    //pt_wait = &t_wait;    
    // Wait for ready socket file descriptor
    if ((fdcount = select(maxfd, &_readset, NULL, NULL, &t_wait)) > 0) {
	// Receive data
        
		num = recvfrom(_socket_fd, msgBuf, sizeof(msgBuf), 0, 
		       (struct sockaddr *) &req_addr, &addrlen);
	switch (num)
	    {
	    case -1: // error in data transmission
		_errmsg = strerror(errno);
		_isConnBroken = true;
		return SYSTEM_ERROR;

	    case 0: // server has stopped the data transmission
		break;
	    default: // data is received
		msgBuf[num] = '\0';
		data.append(msgBuf, num);
	    }
    } else {
	// If fdcount == 0, then its timeout, else connection problem.
	if (fdcount != 0) {
		_errmsg = strerror(errno);
		_isConnBroken = true;
		return SYSTEM_ERROR;
	    }
    }
    return SUCCESSFUL;
}

unsigned long
UnixSocket::ipaddr2unsloint(string host) {
    unsigned long addr;
    
    if ((addr = inet_addr(host.c_str())) == 0xffffffff)
	{
	    _errmsg = strerror(errno);
	    return SYSTEM_ERROR;
	}
    return addr;
}

string
UnixSocket::host2ipaddr(string host) {

    struct hostent * he, heResult;
    char buf[1024];
    int bufSize=0;
    int * error=0;
    string ret("");
    

#ifdef linux
    if( !(he=(hostent*)gethostbyname_r(host.c_str(), &heResult, buf, bufSize, &he, error))) {
	return ret;
    }
#else
    if( !(he=gethostbyname_r(host.c_str(), &heResult, buf, bufSize, error))) {
	return ret;
    }
#endif
    // Get address of mcrhost
    ret = he->h_name;
    return ret;
}

string
UnixSocket::getErrorMsg() const {
    return _errmsg;
}

int
UnixSocket::getPort() const {
    return _port;
}

/*
 * These variables describe the formatting of this file.  If you don't like the 
 * template defaults, feel free to change them here (not in your .emacs file).
 *
 * Local Variables:
 * mode: C++
 * comment-column: 32
 * c-basic-offset: 4
 * fill-column: 79
 * End:
*/
