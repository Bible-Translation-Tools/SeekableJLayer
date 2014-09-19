/*
 * 11/19/04			1.0 moved to LGPL.
 * 
 * 12/12/99 0.0.7	Implementation stores single bits 
 *					as ints for better performance. mdm@techie.com.
 *
 * 02/28/99 0.0     Java Conversion by E.B, javalayer@javazoom.net
 *
 *                  Adapted from the public c code by Jeff Tsay.
 *
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public 
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */ 
package javazoom.jl.decoder;

/** 
 * Implementation of Bit Reservoir for Layer III.
 * <p>
 * The implementation stores single bits as a word in the buffer. If
 * a bit is set, the corresponding word in the buffer will be non-zero.
 * If a bit is clear, the corresponding word is zero. Although this
 * may seem wasteful, this can be a factor of two quicker than 
 * packing 8 bits to a byte and extracting. 
 * 
 * WVB - is this still true ? This feels as if it were a C++ world
 * <p> 
 */

// REVIEW: there is no range checking, so buffer underflow or overflow
// can silently occur.
final class BitReserve
{
	/**
	 * Size of the internal buffer to store the reserved bits.
	 * Must be a power of 2. And x8, as each bit is stored as a single
	 * entry.
	 */
	private static final int	BUFSIZE = 4096 * 8;

	/**
	 * Mask that can be used to quickly implement the
	 * modulus operation on BUFSIZE.
	 */
	private static final int	BUFSIZE_MASK = BUFSIZE-1;

	private int 				offset, totbit, buf_byte_idx;
	private final boolean[] 	buf = new boolean[BUFSIZE];

	BitReserve()
	{
		offset = 0;
		totbit = 0;
		buf_byte_idx = 0;	  
	}

	/**
	 * Return totbit Field.
	 */
	public int hsstell() 
	{ 
		return totbit;
	}

	/**
	 * Read a number bits from the bit stream.
	 * @param N the number of bits to get
	 */
	public int hgetbits(int N)
	{
		totbit += N;
		int val = 0;
		int pos = buf_byte_idx;
		if (pos+N < BUFSIZE)
		{
			while (N-- > 0)
			{
				val <<= 1;
				val |= (buf[pos++] ? 1 : 0);
			}
		}
		else
		{	 
			while (N-- > 0)
			{
				val <<= 1;			 
				val |= (buf[pos] ? 1 : 0);
				pos = (pos+1) & BUFSIZE_MASK;
			}		 
		}	
		buf_byte_idx = pos;
		return val;
	}

    public void skipBits(int N)
    {
        totbit += N;
        buf_byte_idx = (buf_byte_idx+N)&BUFSIZE_MASK;
    }

	/**
	 * Read 1 bit from the bit stream.
	 */
	/*
   public int hget1bit_old()
   {
   	  int val;
	  totbit++;
	  if (buf_bit_idx == 0)
	  {
         buf_bit_idx = 8;
	     buf_byte_idx++;		 
	  }
      // BUFSIZE = 4096 = 2^12, so
      // buf_byte_idx%BUFSIZE == buf_byte_idx & 0xfff
      val = buf[buf_byte_idx & BUFSIZE_MASK] & putmask[buf_bit_idx];
      buf_bit_idx--;
	  val = val >>> buf_bit_idx;
      return val;   
   }
	 */

	/**
	 * Returns next bit from reserve.
	 */
	public final boolean hget1bit()
	{   	  
		totbit++;	  
		final boolean val = buf[buf_byte_idx];
		buf_byte_idx = (buf_byte_idx+1) & BUFSIZE_MASK;
		return val;
	}

	/**
	 * Retrieves bits from the reserve.     
	 */
	/*   
   public int readBits(int[] out, int len)
   {
		if (buf_bit_idx == 0)
		{
		   buf_bit_idx = 8;
		   buf_byte_idx++;
		   current = buf[buf_byte_idx & BUFSIZE_MASK];
		}      

		// save total number of bits returned
		len = buf_bit_idx;
		buf_bit_idx = 0;

		int b = current;
		int count = len-1;

		while (count >= 0)
		{
		    out[count--] = (b & 0x1);
		    b >>>= 1;
		}

		totbit += len;
		return len;
   }
	 */

	/**
	 * Write 8 bits into the bit stream.
	 */
	public void hputbuf(int val)
    {
        int ofs = offset;
        buf[ofs++] = (val & 0x80) != 0;
        buf[ofs++] = (val & 0x40) != 0;
        buf[ofs++] = (val & 0x20) != 0;
        buf[ofs++] = (val & 0x10) != 0;
        buf[ofs++] = (val & 0x08) != 0;
        buf[ofs++] = (val & 0x04) != 0;
        buf[ofs++] = (val & 0x02) != 0;
        buf[ofs++] = (val & 0x01) != 0;

        if (ofs == BUFSIZE)
            offset = 0;
        else
            offset = ofs;
    }

	public void rewindNbits(int N)
	{
		totbit -= N;	  	  
		buf_byte_idx -= N;
		if (buf_byte_idx<0)
			buf_byte_idx += BUFSIZE;
	}
}
