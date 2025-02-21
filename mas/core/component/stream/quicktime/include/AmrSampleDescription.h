#ifndef _AmrSampleDescription_h_
#define _AmrSampleDescription_h_

#include <string.h>
#include <Atom.h>
#include <SampleDescription.h>
#include <AmrSpecificAtom.h>

namespace quicktime {
    /**
     * This class is the AMR  or AMR-WB equivalent of the Sound Sample Description 
	 * The name differs between amr (SAMR) and amr-wb(SAWB) but otherwise is the same
	 */ 
    class AmrSampleDescription : public sampleDescriptionAtom {
    
	public:
	
	/**
	 * This is the default constructor.
	 */
	AmrSampleDescription(AtomName codec);
	~AmrSampleDescription();

	inline void setTimeScale(unsigned short v) { m_timeScale = v; }
	inline unsigned short getTimeScale() { return m_timeScale; }

	/**
	 * Restores the contents of this atom.
	 * Only meta data is restored.
	 */
	bool restoreGuts(AtomReader& atomReader, unsigned atomSize);
	
	/**
	 * Stores this the contents of this atom.
	 * Both sample data and meta data is stored.
	 */ 
	bool saveGuts(AtomWriter& atomWriter);
	
	/**
	 *get the AMR specific box or DAMR which is a subatom of amr 
	 *sample description
	 */
	AmrSpecificAtom * getAMRSpecificAtom();

	/**
	 * This is the equality operator.
	 */
	bool operator==(AmrSampleDescription& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(AmrSampleDescription& leftAtom);
	
	/**
	 * Calculates and returns the total size of the atom.
	 * The calcuation is based upon the size of this atom
	 * and all sub-atoms.
	 */
	unsigned getAtomSize();

    private:
	
	unsigned short m_timeScale; 
	
	AmrSpecificAtom *m_AMRSpecificBox; //The DAMR box or atom.
	
	std::string m_codecName;

    };
};


#endif
