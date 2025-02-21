/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */

#include <iostream>

//entry function to the wav-to-amr converter.
extern "C" int encoder_main(char*, char*);

void Usage(char* prog) {
    std::cerr <<"Usage: " <<prog <<" <infile> <outfile> [milliseconds]" <<std::endl;
    exit(1);
}

int main(int argc, char* argv[]) {
    if (argc < 3
	|| 0 == argv[1]
	|| 0 == argv[2]
	|| 0 == strlen(argv[1])
	|| 0 == strlen(argv[2])) {
	Usage(argv[0]);
    }
    int wantedLength = -1;
    if (argc > 3) {
	wantedLength = (int) strtol(argv[3], (char **)NULL, 10);
    }

    int res = encoder_main(argv[1], argv[2]);
    std::cout <<"Converted " <<argv[1] <<" to " <<argv[2] <<"with result: " <<res <<std::endl;
    exit(res);
}
