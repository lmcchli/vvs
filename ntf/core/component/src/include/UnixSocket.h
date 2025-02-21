#ifndef UNIXSOCKET_H
#define UNIXSOCKET_H

/*
  File:		UnixSocket.h
  Description:	This class implements a UDP Unix socket for
                either a client or a server.
  Originated:	2004-04-19
  Author:	Joakim Nilsson
  Signature:	ermjnil


  Copyright (c) 2004 MOBEON AB

  The copyright to the computer program(s) herein is the property of 
  MOBEON AB, Sweden. The programs may be used and/or copied only 
  with the written permission from MOBEON AB or in accordance with 
  the terms and conditions stipulated in the agreement/contract under 
  which the programs have been supplied.
*/

#include <string>
#include <sys/select.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

using namespace std;


class UnixSocket {
    
public:
    // Use this function to open a socket
    // Return SUCCESSFUL if socket is opened, SYSTEM_ERROR if a socket exception occures.
    int openSocket();

    // Use this function to close a socket
    // Return SUCCESSFUL if socket is closed, SYSTEM_ERROR if a socket exception occures.
    int closeSocket();

    // This function sends a UDP packet to port _port and host _sendhost
    // data is the data string to be sent.
    // Return SUCCESSFUL if data is sent, SYSTEM_ERROR if a socket exception occures.
    int send(string data);

    // This function sends a UDP packet to port _port
    // data is the data string to be sent.
    // host is the host where the data shall be sent to
    // Return SUCCESSFUL if data is sent, SYSTEM_ERROR if a socket exception occures.
    int send(string data, string host);

    // This function sends a UDP packet
    // data is the data string to be sent.
    // host is the host where the data shall be sent to
    // port is the receiving application port
    // Return SUCCESSFUL if data is sent, SYSTEM_ERROR if a socket exception occures.
    int send(string data, string host, int port);

    // This function receive data on port _port
    // data is a string to store the data in
    // Return SUCCESSFUL if data is received, SYSTEM_ERROR if a socket exception occures.
    int receive(string &data);

    // Convert a hostname to an IP address.
    string host2ipaddr(string host);

    enum t_socket {client, server};

    // Get port number
    int getPort() const;
    
    UnixSocket(int port, string host, t_socket stype);
    ~UnixSocket();
    string getClassName() { return _name; }
    string getErrorMsg() const;

private:
    unsigned long ipaddr2unsloint(string host);
    
    string _name;
    bool _isConnBroken;
    string _errmsg;
    int _socket_fd;
    fd_set _writeset;
    fd_set _readset;
    int _port;
    string _host;
    string _sendHost;
    struct sockaddr_in _s_in; // Used as reveiver socket address
    t_socket _socketType; // Type of socket user, client or server
    
};
#endif
