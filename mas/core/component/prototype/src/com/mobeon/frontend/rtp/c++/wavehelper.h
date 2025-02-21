#ifndef WAVEHELPER_H
#define WAVEHELPER_H

#include <iostream>
#include <fstream>
#include <cmath>
#include <climits>
#include <cstdlib>

using namespace std;


class riffBlock {
 public:
  char riffTag[4]; // always "RIFF"
  long length; // of entire file
  char wavTag[4]; // always "WAVE"
};

class formatBlock  {
 public:
  char fmtTag[4]; // always "fmt "
  long length; // of format block, always 16
  short compression; // always equal to 0x01
  short channels; // always 1 = mono, 2 = stereo
  long sampleRate; // in Hz
  long bytesPerSec;
  short align;
  short significat_bits;
};


class dataBlock {
 public:
  char dataTag[4]; // always "data"
  long length; // length of data in block to follow
};

class wavehelper {
 public:
  void makeHeader(ofstream &ofs,long sampleRate,long dataLen, short bytesPerSample);
};


#endif
