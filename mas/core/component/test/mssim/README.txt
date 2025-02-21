MS-SIM Tool (MsSim.class and SmtpServer.class) v 0.1

WHAT IS IT ?
------------------
This program simulates an IMAP server and a SMTP server.
It's primary use is for loadtesting of your client. The program loads the message files in the messages directory and responds with information from these mails.

BUILD & PACKAGE
-------------------
Build with ant

% ant

Package

% tar -cvf MSSim.tar lib/ messages/ mssim.jar README.txt *.sh

RUNNING THE PROGRAM's
------------------
Usage: ./imap_run.sh port
Usage: ./smtp_run.sh port

Options are explained here:

port
Used to set which port to start these servers.


FUTURE PLANS
------------------
Make it configurable, support more IMAP, etc.
