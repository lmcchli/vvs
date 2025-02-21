#include "UnixSocket.h"
#include <iostream>
#include <string>
#include <stdlib.h>


using namespace std;

int main (int argc, char *argv[]) {

    int port;
    string data;
    string host;
    enum UnixSocket::t_socket type;

    if (argc > 2) {
	port=atoi(argv[1]);
	data=argv[2];
	host=argv[3];
    }
    else {
	cout<<"Missing arguments!"<<endl<<"Usage: testclient <port> <data> <host>"<<endl<<endl;
	exit(1);
    }
    UnixSocket usock_tx(port, host, UnixSocket::server);
    usock_tx.openSocket();
    usock_tx.send(data);
    usock_tx.closeSocket();
        
    return 0;
}
