#ifndef _FtypAtom_h_
#define _FtypAtom_h_

#ifndef WIN32
#include <inttypes.h>
#else
typedef unsigned uint32_t;
#endif

#include "Atom.h"

namespace quicktime {
    /**
     * File type atom.
     */
    class FtypAtom : public Atom {
    public:
        /**
         * This is the default constructor.
         */
        FtypAtom();

        /**
         * The destructor.
         */
        ~FtypAtom();

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
         * Returns the size of this atom.
         *
         * The returned size represent the size, on media, which is
         * occupied by this atom.
         */
        unsigned getAtomSize();

        /**
         * This is the equality operator.
         */
        bool operator==(FtypAtom& leftAtom);

        /**
         * This is the equality operator.
         */
        bool operator!=(FtypAtom& leftAtom);

    private:
        /**
         * Major brand of the file, i.e. '3gp5'
         */
        uint32_t m_majorBrand;

        /**
         * The "rest" of the version number, i.e. minus the "5" in the major
         * brand.
         */
        uint32_t m_minorVersion;

        /**
         * One or more major brands of compatible versions. For now only
         * compatible with this version.
         */
        uint32_t m_compatibleVersion1;
        //uint32_t m_CompatibleVersion2;
        //uint32_t m_CompatibleVersion3;
        //                     ...
        //uint32_t m_CompatibleVersionN;
    };

};

#endif
