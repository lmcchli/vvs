#include "UnixSocket.h"
#include <iostream>
#include <string>
#include <stdlib.h>
#include <unistd.h>
#include "def.h"


using namespace std;

int main (int argc, char *argv[]) {

    int port;
    string data;
    string host;
    enum UnixSocket::t_socket type;

    if (argc > 2) {
	port=atoi(argv[1]);
	host=argv[2];
    }
    else {
	cout<<"Missing arguments!"<<endl<<"Usage: testserver <port> <host>"<<endl<<endl;
	exit(1);
    }
    UnixSocket usock_rx(port, host, UnixSocket::server);
    UnixSocket usock_tx(10002, host, UnixSocket::client);
    usock_tx.openSocket();
    usock_rx.openSocket();
    while(true) {
	data="";
	if (usock_rx.receive(data) == SUCCESSFUL) {
	    cout<<"Tar emot: "<<data<<endl;
	    data="OK";
	}
	else
	    cout<<"Server error!!"<<endl<<usock_rx.getErrorMsg()<<endl;
	//usleep(500000);
	usock_tx.send(data, host);
    }
    usock_tx.closeSocket();
    usock_rx.closeSocket();
    
    
    return 0;
}
