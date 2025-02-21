#include "wavehelper.h"
#include "RTPStream.h"
#include <iostream>
#include <fstream>
#include <string.h>


void
wavehelper::makeHeader(ofstream &ofs,long sampleRate,long dataLen, short bytesPerSample)
{
 long fileLen = dataLen + sizeof(riffBlock) + sizeof(formatBlock) +
sizeof(dataBlock) - 8;
 riffBlock riff = {{'R','I','F','F'},fileLen,{'W','A','V','E'}};
 formatBlock format = {
  {'f','m','t',' '} // tag
  ,16 // length, always 16
  ,7 // compression, 70 G711 uLaw
  ,1 // channels, 1 = mono, 2 = stereo
  ,sampleRate // in Hz
  ,sampleRate * bytesPerSample // bytes per second
  ,bytesPerSample // bytes per sample
  ,bytesPerSample * CHAR_BIT // bits per sample
 };
 // factBlock fact = { {'f','a','c','t'}, 
 dataBlock dataHeader = {{'d','a','t','a'},dataLen};
 ofs.write((char*) &riff,sizeof(riffBlock));
 ofs.write((char*)&format,sizeof(formatBlock));
 ofs.write((char*)&dataHeader,sizeof(dataBlock));
}
