/*
 * 11/19/04 1.0 moved to LGPL.
 * 12/12/99 Initial implementation.		mdm@techie.com. 
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
 * A Type-safe representation of the the supported output channel
 * constants. 
 * 
 * This class is immutable and, hence, is thread safe. 
 * 
 * @author	Mat McGowan 12/12/99 
 * @since	0.0.7
 */
public interface OutputChannels
{		
	/**
	 * Flag to indicate output should include both channels. 
	 */
	static final int BOTH_CHANNELS = 0;
		
	/**
	 * Flag to indicate output should include the left channel only. 
	 */
	static final int LEFT_CHANNEL = 1;

	/**
	 * Flag to indicate output should include the right channel only. 
	 */
	static final int RIGHT_CHANNEL = 2;
		
	/**
	 * Flag to indicate output is mono. 
	 */
	static final int DOWNMIX_CHANNELS = 3;
}
