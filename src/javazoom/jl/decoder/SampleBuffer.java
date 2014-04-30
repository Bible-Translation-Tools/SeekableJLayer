/* 
 * 11/19/04	 1.0 moved to LGPL.
 * 
 * 12/12/99  Initial Version based on FileObuffer.	mdm@techie.com.
 * 
 * FileObuffer:
 * 15/02/99  Java Conversion by E.B ,javalayer@javazoom.net
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
 * The <code>SampleBuffer</code> class implements an output buffer
 * that provides storage for a fixed size block of samples. 
 */
final class SampleBuffer implements Obuffer
{
	private final short[] buffer;
	// Holds the next pointer for this specific channel
	private final int[] bufferp;
	// Number of channels in this buffer
	private final int channels;

	SampleBuffer(int number_of_channels)
	{
		buffer = new short[OBUFFERSIZE];
		bufferp = new int[MAXCHANNELS];
		channels = number_of_channels;
		for (int i = 0; i < number_of_channels; ++i) 
			bufferp[i] = (short)i;
	}	

	public void appendSamples(int channel, float[] f)
	{
		int pos = bufferp[channel];
		for (int i=0; i<32;)
		{
			float fs = f[i++];
			fs = (fs>32767.0f ? 32767.0f : (fs < -32767.0f ? -32767.0f : fs));
			buffer[pos] = (short)fs;
			pos += channels;
		}
		bufferp[channel] = pos;
	}
}
