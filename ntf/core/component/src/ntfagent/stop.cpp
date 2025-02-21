#include "UnixSocket.h"
#include <iostream>
#include <sstream>
#include <string>
#include <stdlib.h>
#include "def.h"


using namespace std;

string getIpAddress(const string strPort);
int getPort(const string strPort);

int main (int argc, char *argv[]) {

    int port;
    int size;
    string ntfName;
    stringstream ss;
    string message;
    string ipaddrAndPort;
    string ipaddr;

    if (argc > 1) {
	ipaddrAndPort=argv[1];
	port = getPort(ipaddrAndPort);
	ipaddr = getIpAddress(ipaddrAndPort);
	ntfName=argv[2];
    }
    else {
	cout<<"Missing arguments!"<<endl<<"Usage: stop [<IP-address>:]<port> <ntfname>"<<endl<<endl;
	exit(1);
    }

    ss << "Type=Set"<<endl;
    ss << "Index="<<0<<endl;
    ss << "Name="<<ntfName<<endl;
    ss << endl;
    ss <<"stop=true"<<endl;

    size=ss.str().size();
    message.assign(int2string(size));
    message.append("\n");
    message.append(ss.str());

    UnixSocket usock_tx(port, ipaddr, UnixSocket::client);
    
    if (usock_tx.openSocket() != 0) {
        usock_tx.closeSocket();
        exit(1);
    }
	
    if (usock_tx.send(message) != 0) {
	usock_tx.closeSocket();
     	exit(1);
    }
    usock_tx.closeSocket();    
    return 0;
}

// Returns the IP-address if the variable port is defined as follow
// <IP address>:<port> 
string getIpAddress(const string strPort) {
    string ret;
    if (strPort.find(":") == string::npos)
	return "127.0.0.1";
    Tokenizer token(strPort, ':');
    return (token.get(ret) == SUCCESSFUL) ? ret : "127.0.0.1"; 	
}

// Returns the port number even if the variable port is defined as follow
// <IP address>:<port> 
int getPort(const string strPort) {
    string ret;
    int port  = 18001;
    if (strPort.find(":") != string::npos) {
	Tokenizer token(strPort, ':');
	token.get(ret); 
	if (token.get(ret) == SUCCESSFUL)
	    port = string2int(ret);
	return port;
    }
    else
	return string2int(strPort);
}

