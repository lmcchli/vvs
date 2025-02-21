/*
 * DelayInfo.java
 *
 * Created on den 12 augusti 2004, 12:03
 */

package com.mobeon.common.storedelay;

/**
 * Holds information about delayed data.
 * The data is identified by the user that the data is for and the
 * type of data it is. The type is represented by a short, it is
 * the responsibility of the client to define unique id's for the
 * different types of data that is used. <p />
 * The data is deleyed until a given time, the time representation is a
 * long whose value corresponds to milliseconds value used in the Calendar 
 * class see
 * {@link java.util.Calendar#getTimeInMillis() Calendar.getTimeInMillis()}. 
 * Note that the constructor does not
 * take a delay time, it is assumed that the actual delayer will set the 
 * millisecond value based on some more readable notation. <br />
 * This class contains both a string and a bytearray to hold the data that is to
 * be delayed, a client may use either or both of these for its data. If one
 * is not used it should be set to NULL.
 */
public class DelayInfo
{
   // --------------------------------------
   // Private data
   // --------------------------------------
    
   private String key;      // String key for info, identifies together with type.
   private short type;      // Client defined type for the data
   private long wantTime;   // Time the info should be handled
   private String strInfo;  // A string with data, not interpreted by this class
   private byte[] byteInfo; // Byte Array with data, not interpreted by this class

   /**
    * Creates information to be delayed.
    * Note that the delay time must be set separately.
    * @param key Identifier for this data. Must
    *             be unique together with type.
    * @param type Type of information.
    * @param strInfo String to store, set to null if no string info to store.
    * @param byteInfo Byte array to store, set to null if no byte info to store.
    */
   public DelayInfo(String key, short type, String strInfo, byte[] byteInfo)
   {
      this.key = key;
      this.type = type;
      this.strInfo = strInfo;
      this.byteInfo = byteInfo;
   }
   
   /**
    * DelayInfo objects are equal if the have the same key and type.
    * Note especially that the delaytime and data are <b>not</b> used when
    * determining equality. This simplifies searching for existing
    * DelayInfos. For a equality definition that includes the
    * wanted time see {@link DelayInfoComparator}
    */
   public boolean equals(Object other)
   {
       if (!(other instanceof DelayInfo)) return false;
       DelayInfo otherDI = (DelayInfo)other;
       return key.equals(otherDI.key) && type == otherDI.type;
   }
   
   /**
    * Hash is based on key and type.
    * This is to allow compatibility with definition of equals.
    */
   public int hashCode()
   {
       return key.hashCode() ^ type;
   }

   /**
    * Descripive string for debugging.
    */
   public String toString()
   {
      String byteDescription = "null";
      if (byteInfo != null) {
          byteDescription = "" +byteInfo.length + "bytes";
      }
      return "" + key + " " + type + " " + wantTime + " " +
            strInfo + " " + byteDescription;
   }
   
   

   // -------------------------------------------------------------------
   // Getters and setters, setters are normally with default (package)
   // protection to control the useage of this info
   // -------------------------------------------------------------------

   /**
    * Getter for property wantTime.
    * @return Value of property wantTime.
    */
   public long getWantTime()
   {
      return wantTime;
   }

   /**
    * Setter for property wantTime.
    * @param wantTime New value of property wantTime.
    */
   public void setWantTime(long wantTime)
   {
      this.wantTime = wantTime;
   }

   /**
    * Getter for property type.
    * @return Value of property type.
    */
   public short getType()
   {
      return type;
   }

   /**
    * Setter for property type.
    * @param type New value of property type.
    */
   void setType(short type)
   {
      this.type = type;
   }

   /**
    * Get user identity.
    * @return Identity of user
    */
   public String getKey()
   {
      return key;
   }

   /**
    * Get the stored string data.
    * @return Stored data, null if no data.
    **/
   public String getStrInfo()
   {
      return strInfo;
   }

   /**
    * Update the stored string data
    * @param strInfo New data for this delayinfo
    */
   public void setStrInfo(String strInfo)
   {
      this.strInfo = strInfo;
   }

   /**
    * Get the stored byte array data.
    * This return a reference to the internal storege, so any changes
 * to the array affects the delayinfo.
    * @return Stored data, null if no data.
    */
   public byte[] getByteInfo()
   {
      return byteInfo;
   }

   /**
    * Update the stored byte array data.
    * Note that the data might also be updated by directly modifying
    * the array content.
    * @param byteInfo New data to store.
    */
   public void setByteInfo(byte[] byteInfo)
   {
      this.byteInfo = byteInfo;
   }


   
   // Help methods to get data in and out of byte arrays
   
   /**
     * Pack an short into 2 bytes of a byte array.
     * @param bytes The byte array
     * @param index Where to start in the array
     * @param value The value to pack
     * @return number of bytes used.
     */
    public static int packShort(byte[] bytes, int index, short value)
    {
        bytes[index++] = (byte) ((value >>> 8) & 0xFF);
        bytes[index++] = (byte) (value & 0xFF);
        return 2;
    }
    
    
    public static int shortPackSize()
    {
        return 2;
    }
    

    /**
     * Get value of the short packed at given position
     * @param bytes The packed data
     * @param index Position to start at
     */
    public static short unpackShort(byte[] bytes, int index)
    {
        byte b1 = bytes[index];
        byte b0 = bytes[index + 1];
        short result = 0;
        result |= (b1 << 8) & 0x0000FF00;
        result |= b0 & 0x000000FF;
        return result;
    }
   
   
    /**
     * Pack an integer into 4 bytes of a byte array.
     * @param bytes The byte array
     * @param index Where to start in the array
     * @param value The value to pack
     * @return number of bytes used.
     */
    public static int packInt(byte[] bytes, int index, int value)
    {
        bytes[index++] = (byte) ((value >>> 24) & 0xFF);
        bytes[index++] = (byte) ((value >>> 16) & 0xFF);
        bytes[index++] = (byte) ((value >>> 8) & 0xFF);
        bytes[index++] = (byte) (value & 0xFF);
        return 4;
    }
    
    
    public static int intPackSize()
    {
        return 4;
    }
    

    /**
     * Get value of the integer packed at given position
     * @param bytes The packed data
     * @param index Position to start at
     */
    public static int unpackInt(byte[] bytes, int index)
    {
        byte b3 = bytes[index];
        byte b2 = bytes[index + 1];
        byte b1 = bytes[index + 2];
        byte b0 = bytes[index + 3];
        int result = 0;
        result |= (b3 << 24) & 0xFF000000;
        result |= (b2 << 16) & 0x00FF0000;
        result |= (b1 << 8) & 0x0000FF00;
        result |= b0 & 0x000000FF;
        return result;
    }

    /**
     * Pack a long into 8 bytes of a byte array.
     * @param bytes The byte array
     * @param index Where to start in the array
     * @param value The value to pack
     * @return number of bytes used.
     */
    public static int packLong(byte[] bytes, int index, long value)
    {
        bytes[index++] = (byte) ((value >>> 56) & 0xFFL);
        bytes[index++] = (byte) ((value >>> 48) & 0xFFL);
        bytes[index++] = (byte) ((value >>> 40) & 0xFFL);
        bytes[index++] = (byte) ((value >>> 32) & 0xFFL);
        bytes[index++] = (byte) ((value >>> 24) & 0xFFL);
        bytes[index++] = (byte) ((value >>> 16) & 0xFFL);
        bytes[index++] = (byte) ((value >>> 8) & 0xFFL);
        bytes[index++] = (byte) (value & 0xFFL);
        return 8;
    }

    public static int longPackSize()
    {
        return 8;
    }


    /**
     * Get value of the long packed at given position
     * @param bytes The packed data
     * @param index Position to start at
     */
    public static long unpackLong(byte[] bytes, int index)
    {
        int pos = index;
        byte b7 = bytes[pos++];
        byte b6 = bytes[pos++];
        byte b5 = bytes[pos++];
        byte b4 = bytes[pos++];
        byte b3 = bytes[pos++];
        byte b2 = bytes[pos++];
        byte b1 = bytes[pos++];
        byte b0 = bytes[pos++];

        long result = 0;
        result |= ((long) b7 << 56) & 0xFF00000000000000L;
        result |= ((long) b6 << 48) & 0x00FF000000000000L;
        result |= ((long) b5 << 40) & 0x0000FF0000000000L;
        result |= ((long) b4 << 32) & 0x000000FF00000000L;
        result |= ((long) b3 << 24) & 0x00000000FF000000L;
        result |= ((long) b2 << 16) & 0x0000000000FF0000L;
        result |= ((long) b1 << 8) & 0x000000000000FF00L;
        result |= (long) b0 & 0x00000000000000FFL;
        return result;
    }
    
}
