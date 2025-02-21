#ifndef _Atom_h_
#define _Atom_h_

#include <AtomName.h>
#include <string>

namespace quicktime {
    class AtomReader;
    class AtomWriter;
    
    /**
     * QuickTime file format Atom.
     * This class is the base class for all the QuickTime file media format
     * atoms. 
     */
    class Atom {
		public:
		/**
		 * Atom constructor.
		 * Input:
		 *   - name, the atom name
		 */
		Atom(AtomName name, unsigned size=0);

		/**
		 * Destructor.
		 */
		virtual ~Atom();
		
		/**
		 * Abstract save method.
		 * Writes the atom data to memory or file.
		 */
		virtual bool saveGuts(AtomWriter& atomWriter) = 0;
		
		/**
		 * Abstract load method.
		 * Reads the atom data from memory or file.
		 * The implementation of the restore guts method must be
		 * able to parse the sub-atoms of the currently restored atoms.
		 */
		virtual bool restoreGuts(AtomReader& atomReader, 
					 unsigned atomSize) = 0;

		/**
		 * Atom name getter.
		 */
		unsigned getName();
		
		/** change the name of the Atom
		 */
		inline void setName(AtomName name) {
			m_atomName=name;
		}
		
		/**
		 * Returns the AtomName as a printable string.
		 * Mostly for debuging.
		 * Only creates the string if called.
		 * 	THIS OBJECT IS RESPONIBLE FOR DELETING THE ALLOCATED STRING.
		 * */
		std::string* getAtomNameAsString();

		/**
		 * Calculates and returns the total size of the atom.
		 * The calcuation is based upon the size of this atom
		 * and all sub-atoms.
		 */
		virtual unsigned getAtomSize();

		/**
		 * Gets the size of an atoms name and size fields.
		 *@return the number of bytes needed for the atoms size and name.
		 */
		virtual unsigned getSizeOfSizeAndType();

		unsigned getInnerSize();
		int getAsInt(unsigned index);
		short getAsShort(unsigned index); 
		char getAsChar(unsigned index); 

		protected:
		/**
		 * The atom name;
		 */
		AtomName m_atomName;
		std::string *m_atomNameString;

		/**
		 * The atom size.
		 * Actual size on file.
		 */
		unsigned m_atomSize;
		unsigned char* m_innerData;
		unsigned m_innerSize;
    };
};

inline unsigned quicktime::Atom::getSizeOfSizeAndType() {
    /* Simplified implementation suitable for non-huge files. */
    return 8;
}


inline unsigned quicktime::Atom::getName() 
{
    return m_atomName;
}

inline unsigned quicktime::Atom::getAtomSize()
{
    return m_atomSize;
}

#endif	
	    
