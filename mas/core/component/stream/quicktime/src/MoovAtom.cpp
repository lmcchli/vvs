#include "MoovAtom.h"

#include "MvhdAtom.h"
#include "TrakAtom.h"

#include "AtomReader.h"
#include "AtomWriter.h"

#include <iostream>
using std::cout;
using std::endl;

using namespace quicktime;

MoovAtom::MoovAtom() 
    : Atom(MOOV),
      m_audioTrackAtom(0),
      m_videoTrackAtom(0),
      m_audioHintTrackAtom(0),
      m_videoHintTrackAtom(0) {

}

MoovAtom::~MoovAtom() {
	if (m_audioTrackAtom != NULL) {
		delete m_audioTrackAtom;
		m_audioTrackAtom=NULL;
	}
	if (m_audioHintTrackAtom !=NULL) {
		delete m_audioHintTrackAtom;
		m_audioHintTrackAtom=NULL;
	}
	if (m_videoTrackAtom != NULL) {
		delete m_videoTrackAtom;
		m_videoTrackAtom=NULL;		
	}
	if (m_videoHintTrackAtom != NULL) {
		delete m_videoHintTrackAtom;
		m_videoHintTrackAtom=NULL;
	}
}

bool
MoovAtom::restoreGuts(AtomReader& atomReader, unsigned atomSize) {
    m_atomSize = atomSize;
    int nOfBytesLeft(m_atomSize-8);
    unsigned subAtomName;
    unsigned subAtomSize;
    TrakAtom* tmpTrack(0);

    while (nOfBytesLeft) {
	atomReader.readDW(subAtomSize);
	atomReader.readDW(subAtomName);
	switch (subAtomName) {
	case quicktime::MVHD:
	    MOV_DEBUG("(MOOV) MVHD " << subAtomSize);
	    m_movieHeaderAtom.restoreGuts(atomReader, subAtomSize);
	    break;

	case quicktime::TRAK:
	    MOV_DEBUG("(MOOV) TRAK " << subAtomSize);
	    tmpTrack = new TrakAtom();
	    tmpTrack->restoreGuts(atomReader, subAtomSize);
	    switch(tmpTrack->getSubType()) {
	    case quicktime::SOUN:
                MOV_DEBUG("Found sound track");
		        m_audioTrackAtom = tmpTrack;
		break;

	    case quicktime::VIDE:
                MOV_DEBUG("Found video track");
		        m_videoTrackAtom = tmpTrack;
		break;

	    case quicktime::HINT:
                MOV_DEBUG("Found hint track");
                if (m_videoHintTrackAtom == 0)
		        m_videoHintTrackAtom = tmpTrack;
                else
		        m_audioHintTrackAtom = tmpTrack;
		break;

	    default:
                MOV_DEBUG("Found unknown track");
		delete tmpTrack;
		break;
	    }
	    break;

	case quicktime::CLIP:
	    MOV_DEBUG("(MOOV) CLIP " << subAtomSize);
	    atomReader.seek(subAtomSize-8, AtomReader::SEEK_FORWARD);
	    break;

	case quicktime::UDTA:
	    MOV_DEBUG("(MOOV) UDTA " << subAtomSize);
	    atomReader.seek(subAtomSize-8, AtomReader::SEEK_FORWARD);
	    break;

	case quicktime::CTAB:
	    MOV_DEBUG("(MOOV) CTAB " << subAtomSize);
	    atomReader.seek(subAtomSize-8, AtomReader::SEEK_FORWARD);
	    break;

	default:
	    MOV_DEBUG("(MOOV) " << subAtomName << " " << subAtomSize);
	    atomReader.seek(subAtomSize-8, AtomReader::SEEK_FORWARD);
	    break;
	}
	nOfBytesLeft-=subAtomSize;
    }
    MOV_DEBUG("End of MOOV");
    return true;
}

bool
MoovAtom::saveGuts(AtomWriter& atomWriter) {

    atomWriter.writeDW(MoovAtom::getAtomSize());
    atomWriter.writeDW(getName());
    m_movieHeaderAtom.saveGuts(atomWriter);
    if (m_audioTrackAtom != 0) m_audioTrackAtom->saveGuts(atomWriter);
    if (m_videoTrackAtom != 0) m_videoTrackAtom->saveGuts(atomWriter);
    if (m_videoHintTrackAtom != 0) m_videoHintTrackAtom->saveGuts(atomWriter);
    if (m_audioHintTrackAtom != 0) m_audioHintTrackAtom->saveGuts(atomWriter);
    return true;
}

unsigned
MoovAtom::getAtomSize() {

    unsigned size(8);

    size += m_movieHeaderAtom.getAtomSize();
    if (m_audioTrackAtom != 0) size += m_audioTrackAtom->getAtomSize();
    if (m_videoTrackAtom != 0)  size += m_videoTrackAtom->getAtomSize();
    if (m_videoHintTrackAtom != 0)  size += m_videoHintTrackAtom->getAtomSize();
    if (m_audioHintTrackAtom != 0)  size += m_audioHintTrackAtom->getAtomSize();
    return size;
}

bool MoovAtom::operator==(MoovAtom& leftAtom) {

    if (m_movieHeaderAtom != leftAtom.m_movieHeaderAtom) return false;

    if (m_audioTrackAtom != 0 && leftAtom.m_audioTrackAtom != 0) {
		if (*m_audioTrackAtom != *(leftAtom.m_audioTrackAtom)) return false;
    } else if (m_audioTrackAtom != 0 || leftAtom.m_audioTrackAtom != 0)
		return false;

    if (m_videoTrackAtom != 0 && leftAtom.m_videoTrackAtom != 0) {
	if (*m_videoTrackAtom != *(leftAtom.m_videoTrackAtom)) return false;
    } else if (m_videoTrackAtom != 0 || leftAtom.m_videoTrackAtom != 0)
	return false;

    if (m_videoHintTrackAtom != 0 && leftAtom.m_videoHintTrackAtom != 0) {
	if (*m_videoHintTrackAtom != *(leftAtom.m_videoHintTrackAtom)) return false;
    } else if (m_videoHintTrackAtom != 0 || leftAtom.m_videoHintTrackAtom != 0)
	return false;

    if (m_audioHintTrackAtom != 0 && leftAtom.m_audioHintTrackAtom != 0) {
	if (*m_audioHintTrackAtom != *(leftAtom.m_audioHintTrackAtom)) return false;
    } else if (m_audioHintTrackAtom != 0 || leftAtom.m_audioHintTrackAtom != 0)
	return false;

   return true;
}

bool MoovAtom::operator!=(MoovAtom& leftAtom) {

    return !(*this == leftAtom);
}

