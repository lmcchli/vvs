/*
 * ===================================================================
 *  TS 26.104
 *  REL-5 V5.4.0 2004-03
 *  REL-6 V6.1.0 2004-03
 *  3GPP AMR Floating-point Speech Codec
 * ===================================================================
 *
 */

/*
 * encoder.c
 *
 *
 * Project:
 *    AMR Floating-Point Codec
 *
 * Contains:
 *    Speech encoder main program
 *
 */
#include <stdlib.h>
#include <stdio.h>
#include <memory.h>
#include <string.h>
#include "typedef.h"
#include "interf_enc.h"

#ifndef ETSI
#ifndef IF2
#define AMR_MAGIC_NUMBER "#!AMR\n"
#endif
#endif

#define         SIGN_BIT        (0x80)      /* Sign bit for a A-law byte. */
#define         QUANT_MASK      (0xf)       /* Quantization field mask. */
#define         NSEGS           (8)         /* Number of A-law segments. */
#define         SEG_SHIFT       (4)         /* Left shift for segment number. */
#define         SEG_MASK        (0x70)      /* Segment field mask. */

#define         BIAS            (0x84)      /* Bias for linear code. */

static const short modeConv[]={
   475, 515, 59, 67, 74, 795, 102, 122};


 static inline Word16 ulaw2linear(unsigned char u_val)
 {
    Word16 t;

    /* Complement to obtain normal u-law value. */
     u_val = ~u_val;

     t = ((u_val & QUANT_MASK) << 3) + BIAS;
     t <<= ((unsigned)u_val & SEG_MASK) >> SEG_SHIFT;

    return (u_val & SIGN_BIT) ?  (BIAS - t) : (t - BIAS);
 }

 static inline Word16  alaw2linear(unsigned char a_val)
 {
      Word16 t;
      Word16 seg;

      a_val ^= 0x55;

      t = a_val & QUANT_MASK;
      seg = ((unsigned)a_val & SEG_MASK) >> SEG_SHIFT;
      if(seg) t= (t + t + 1 + 32) << (seg + 2);
      else    t= (t + t + 1     ) << 3;

      return (a_val & SIGN_BIT) ? t : -t;
 }



/*
 * main
 *
 *
 * Function:
 *    Speech encoder main program
 *
 *    Usage: encoder speech_file bitstream_file mode dtx mode_file
 *
 *    Format for speech_file:
 *       Speech is read from a binary file of 16 bits data.
 *
 *    Format for ETSI bitstream file:
 *       1 word (2-byte) for the TX frame type
 *       244 words (2-byte) containing 244 bits.
 *          Bit 0 = 0x0000 and Bit 1 = 0x0001
 *       1 word (2-byte) for the mode indication
 *       4 words for future use, currently written as zero
 *
 *    Format for 3GPP bitstream file:
 *       Holds mode information and bits packed to octets.
 *       Size is from 1 byte to 31 bytes.
 *
 *    ETSI bitstream file format is defined using ETSI as preprocessor
 *    definition
 *
 *    mode        : MR475, MR515, MR59, MR67, MR74, MR795, MR102, MR122
 *    mode_file   : reads mode information from a file
 * Returns:
 *    0
 */
int encoder_main (char * infile, char * outfile){

   /* file strucrures */
   FILE * file_speech = NULL;
   FILE * file_encoded = NULL;
   FILE * file_mode = NULL;

   /* codec id  */
   short codecID;

   /* number of channels */
   short nchannel;

  /*  Sample rate */
    int samplerate;

   /* input speech vector */
   short speech[160];

   unsigned char tmp_speech[160];   
   unsigned char tmp_format[5];

   /* counters */
   int byte_counter, frames = 0, bytes = 0;

   /* pointer to encoder state structure */
   int *enstate;

   /* requested mode */
   enum Mode req_mode = MR122;
   int dtx = 0;

   /* temporary variables */
   char mode_string[9];
   long mode_tmp;

   /* bitstream filetype */
#ifndef ETSI
   unsigned char serial_data[32];
#else
   short serial_data[250] = {0};
#endif

   /* Process command line options */

   
      file_encoded = fopen(outfile, "wb");
      if (file_encoded == NULL){
         return 1;
      }
      file_speech = fopen(infile, "rb");
      if (file_speech == NULL){
         fclose(file_encoded);
         return 1;
      }
  
        req_mode=0;
        dtx = 1;
   


   enstate = Encoder_Interface_init(dtx);


   /* write magic number to indicate single channel AMR file storage format */
   	bytes = fwrite(AMR_MAGIC_NUMBER, sizeof(char), strlen(AMR_MAGIC_NUMBER), file_encoded);
     
     /* read first wave headers */

     fread(speech, sizeof (Word16),16,file_speech);

     codecID=speech[10];
     nchannel=speech[11];
     samplerate=speech[12]+speech[13]*256;
     

   fprintf (stderr,"codec id : %d\n",codecID);
   if(nchannel !=1)
   {
     fprintf (stderr,"wave file is not mono!\n ");
     fclose(file_speech);
     fclose(file_encoded);
     return 1;
   }
   if(samplerate !=8000)
   {
     fprintf (stderr,"sample rate is not 8000HZ!\n");
     fclose(file_speech);
     fclose(file_encoded);
     return 1;
   }
    tmp_format[0]=speech[4] & 0xFF;
    tmp_format[1]=speech[4] >>8;
    tmp_format[2]=speech[5] & 0xFF;
    tmp_format[3]=speech[5] >>8;
    tmp_format[4]='\0';

  if (strcmp(tmp_format,"WAVE") !=0) 
   {
      fprintf(stderr,"the file is not wave format!\n");
      return 1;
   } else fprintf(stderr,"the file format is: %s \n",tmp_format);

    rewind(file_speech);



   	if ((codecID==6) || (codecID==7))
   	{   
	     int count;
	     int ii;
             while ((count=fread(tmp_speech,sizeof (Word8), 160,file_speech )) >0)
	     {
	        frames ++;
		for(ii=0;ii<count;ii++)
		{
		   if (codecID==7) {

		   speech[ii]=ulaw2linear(tmp_speech[ii]);
		   }
		   else {

		   speech[ii]=alaw2linear(tmp_speech[ii]);
		   }
                }

    	        byte_counter = Encoder_Interface_Encode(enstate, req_mode, speech, serial_data, 0);

		bytes += byte_counter;


		fwrite(serial_data, sizeof (UWord8), byte_counter, file_encoded );
		fflush(file_encoded);


	     }

   	}
   	else {
   		while (fread( speech, sizeof (Word16), 160, file_speech ) > 0)
   		{

      			frames ++;

		      /* call encoder */
      			byte_counter = Encoder_Interface_Encode(enstate, req_mode, speech, serial_data, 0);

		      bytes += byte_counter;
		      fwrite(serial_data, sizeof (UWord8), byte_counter, file_encoded );
		      fflush(file_encoded);
		   }
             }
       Encoder_Interface_exit(enstate);



   fclose(file_speech);
   fclose(file_encoded);
   if (file_mode != NULL)
      fclose(file_mode);

   return 0;
}
