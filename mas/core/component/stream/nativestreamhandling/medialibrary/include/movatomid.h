#ifndef MovAtomId_h
#define MovAtomId_h

//#ifdef MOV_PRINT_DEBUG
//#include <iostream>
//#define MOV_DEBUG(MSG) std::cout << MSG << std::endl;
//#else
//#define MOV_DEBUG(MSG)
//#endif

#define createAtomId(n, a, m, e) (n<<24 | a<<16 | m<<8 | e<<0) 
//#define createAtomId(n, a, m, e) (n<<0 | a<<8 | m<<16 | e<<24) 

class MovAtomId
{
public:
    enum
    {
        MDAT = createAtomId('m', 'd', 'a', 't'),
        MOOV = createAtomId('m', 'o', 'o', 'v'),
        FREE = createAtomId('f', 'r', 'e', 'e'),
        SKIP = createAtomId('s', 'k', 'i', 'p'),
        WIDE = createAtomId('w', 'i', 'd', 'e'),
        PNOT = createAtomId('p', 'n', 'o', 't'),

        MVHD = createAtomId('m', 'v', 'h', 'd'),
        CLIP = createAtomId('c', 'l', 'i', 'p'),
        TRAK = createAtomId('t', 'r', 'a', 'k'),
        UDTA = createAtomId('u', 'd', 't', 'a'),
        CTAB = createAtomId('c', 't', 'a', 'b'),

        TKHD = createAtomId('t', 'k', 'h', 'd'),
        HINT = createAtomId('h', 'i', 'n', 't'),
        SOUN = createAtomId('s', 'o', 'u', 'n'),
        VIDE = createAtomId('v', 'i', 'd', 'e'),
        STBL = createAtomId('s', 't', 'b', 'l'),
        STSD = createAtomId('s', 't', 's', 'd'),
        STTS = createAtomId('s', 't', 't', 's'),
        STSS = createAtomId('s', 't', 's', 's'),
        STSC = createAtomId('s', 't', 's', 'c'),
        STSZ = createAtomId('s', 't', 's', 'z'),
        STCO = createAtomId('s', 't', 'c', 'o'),
        STSH = createAtomId('s', 't', 's', 'h'),
        MDHD = createAtomId('m', 'd', 'h', 'd'),
        HDLR = createAtomId('h', 'd', 'l', 'r'),
        MINF = createAtomId('m', 'i', 'n', 'f'),
        MDIA = createAtomId('m', 'd', 'i', 'a'),
        TREF = createAtomId('t', 'r', 'e', 'f'),
        NAME = createAtomId('n', 'a', 'm', 'e'),
        HINF = createAtomId('h', 'i', 'n', 'f'),
        HNTI = createAtomId('h', 'n', 't', 'i'),
        RTP = createAtomId('r', 't', 'p', ' '),
        END = createAtomId('e', 'n', 'd', ' ')
    };
};

#endif
