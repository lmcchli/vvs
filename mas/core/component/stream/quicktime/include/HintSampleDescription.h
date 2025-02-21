#ifndef _HintSampleDescription_h_
#define _HintSampleDescription_h_

#include <Atom.h>
#include <SampleDescription.h>

namespace quicktime {
    /**
     * This class is the Hint Sample Description ('rtp ')
     * The hint sample description contains information that defines
     * how to interpret the hint data.
     */
    class HintSampleDescription : public sampleDescriptionAtom {
    public:
	/**
	 * This is the default constructor.
	 */
	HintSampleDescription();

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
	 * This is the equality operator.
	 */
	bool operator==(HintSampleDescription& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(HintSampleDescription& leftAtom);

    private:

	/**
	 * The hint sample description ...
	 */
	unsigned m_data[5];
    };
};

#endif
