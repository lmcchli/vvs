#ifndef _VideoSampleDescription_h_
#define _VideoSampleDescription_h_

#include <Atom.h>
#include "SampleDescription.h"

namespace quicktime {
    /**
     * This class is the Video Sample Description ('h263')
     * According to the QuickTime spec: the video sample description
     * contains information that defines how to interpret the video data.
     */
    class VideoSampleDescription : public sampleDescriptionAtom {
    public:
	/**
	 * This is the default constructor.
	 */
	VideoSampleDescription();

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
	bool operator==(VideoSampleDescription& leftAtom);

	/**
	 * This is the equality operator.
	 */
	bool operator!=(VideoSampleDescription& leftAtom);

    private:

	/**
	 * The version number of the compressed data.
	 */
	unsigned short m_version;

	/**
	 * This must be set to 0.
	 */
	unsigned short m_revision;

	/**
	 * Specifies the vendor that generated the compressed data.
	 * Default 'appl' for Apple Computer inc.
	 */
	unsigned m_vendor;

	/**
	 * Temporal quality (0..1023) indicates the degree of temporal
	 * compression.
	 */
	unsigned m_temporalQuality;

	/**
	 * Temporal quality (0..1024) indicates the degree of spatial
	 * compression.
	 */
	unsigned m_spatialQuality;

	/**
	 * Source image width in pxels.
	 */
	unsigned short m_width;

	/**
	 * Source image height in pixels.
	 */
	unsigned short m_height;

	/**
	 * Horizontal resolution in pixels per inch.
	 */
	unsigned m_horizontalResolution;
	
	/**
	 * Vertical resolution in pixels per inch.
	 */
	unsigned m_vericalResolution;

	/**
	 * Must be set to 0.
	 */
	unsigned m_dataSize;

	/**
	 * Number of frames of compressed data stored in each sample.
	 * Usually set to 1.
	 */
	unsigned short m_frameCount;

	/**
	 * Binary data ...
	 */
	unsigned m_data[8];

	/**
	 * Pixel depth of the compressed image.
	 */
	unsigned short m_colorDepth;

	/**
	 * The color table to use.
	 */
	unsigned short m_colorTableId;
    };
};

#endif
