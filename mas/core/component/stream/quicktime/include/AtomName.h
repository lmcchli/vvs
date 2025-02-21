#ifndef _AtomName_h_
#define _AtomName_h_

#ifdef MOV_PRINT_DEBUG
#include <iostream>
#define MOV_DEBUG(MSG) std::cout << "MOV_DEBUG: " << MSG << std::endl;
#else
#define MOV_DEBUG(MSG)
#endif

#define createAtomName(n, a, m, e) (n<<24 | a<<16 | m<<8 | e<<0)

namespace quicktime {

    enum AtomName {
	INIT = createAtomName('i', 'n', 'i', 't'), //default atom.
	MDAT = createAtomName('m', 'd', 'a', 't'),
	MOOV = createAtomName('m', 'o', 'o', 'v'),
	FREE = createAtomName('f', 'r', 'e', 'e'),
	SKIP = createAtomName('s', 'k', 'i', 'p'),
	WIDE = createAtomName('w', 'i', 'd', 'e'),
	PNOT = createAtomName('p', 'n', 'o', 't'),
	FTYP = createAtomName('f', 't', 'y', 'p'),
	
	MVHD = createAtomName('m', 'v', 'h', 'd'),
	CLIP = createAtomName('c', 'l', 'i', 'p'),
	TRAK = createAtomName('t', 'r', 'a', 'k'),
	UDTA = createAtomName('u', 'd', 't', 'a'),
	CTAB = createAtomName('c', 't', 'a', 'b'),
	
	TKHD = createAtomName('t', 'k', 'h', 'd'),
	HINT = createAtomName('h', 'i', 'n', 't'),
	SOUN = createAtomName('s', 'o', 'u', 'n'),
	VIDE = createAtomName('v', 'i', 'd', 'e'),
	STBL = createAtomName('s', 't', 'b', 'l'),
	STSD = createAtomName('s', 't', 's', 'd'),
	STTS = createAtomName('s', 't', 't', 's'),
	STSS = createAtomName('s', 't', 's', 's'),
	STSC = createAtomName('s', 't', 's', 'c'),
	STSZ = createAtomName('s', 't', 's', 'z'),
	STCO = createAtomName('s', 't', 'c', 'o'),
	STSH = createAtomName('s', 't', 's', 'h'),
	MDHD = createAtomName('m', 'd', 'h', 'd'),
	HDLR = createAtomName('h', 'd', 'l', 'r'),
	MINF = createAtomName('m', 'i', 'n', 'f'),
	MDIA = createAtomName('m', 'd', 'i', 'a'),
	TREF = createAtomName('t', 'r', 'e', 'f'),
	NAME = createAtomName('n', 'a', 'm', 'e'),
	HINF = createAtomName('h', 'i', 'n', 'f'),
	HNTI = createAtomName('h', 'n', 't', 'i'),
	RTP  = createAtomName('r', 't', 'p', ' '),

	ULAW = createAtomName('u', 'l', 'a', 'w'),
	ALAW = createAtomName('a', 'l', 'a', 'w'),
	SAMR = createAtomName('s', 'a', 'm', 'r'), //AMR
	SAWB = createAtomName('s', 'a', 'w', 'b'), //AMR-WB
	H263 = createAtomName('h', '2', '6', '3'),
	S263 = createAtomName('s', '2', '6', '3'),
	D263 = createAtomName('d', '2', '6', '3'),
	
	DAMR = createAtomName('d', 'a', 'm', 'r'), //3gpp AMRSpecificBox for AMR AND AMR-WB

	EDTS = createAtomName('e', 'd', 't', 's'),
	ELST = createAtomName('e', 'l', 's', 't'),

	DINF = createAtomName('d', 'i', 'n', 'f'),
	DREF = createAtomName('d', 'r', 'e', 'f'),

	SMHD = createAtomName('s', 'm', 'h', 'd'),
	VMHD = createAtomName('v', 'm', 'h', 'd'),
	GMHD = createAtomName('g', 'm', 'h', 'd'),
	GMIN = createAtomName('g', 'm', 'i', 'n'),

	APPL = createAtomName('a', 'p', 'p', 'l'),
	MHLR = createAtomName('m', 'h', 'l', 'r'),
	DHLR = createAtomName('d', 'h', 'l', 'r'),
	ALIS = createAtomName('a', 'l', 'i', 's'),

	BAJS = createAtomName('B', 'A', 'J', 'S'),
	END  = createAtomName('e', 'n', 'd', ' ')

    };

};
#endif
