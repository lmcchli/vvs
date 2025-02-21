Before running purifyjava set up purify environment by soursing the file purifyplus_setup.csh:
source /usr/local/rational/purifyplus_setup.csh

Update LD_LIBRARY_PATH to include paths to all .so files loaded by the program.

Start purify on the java class by running the command:
purifyjava class args

class shall be specified by full packet name (separated by "/").

