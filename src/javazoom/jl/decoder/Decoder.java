/*
 * 11/19/04		1.0 moved to LGPL.
 * 01/12/99		Initial version.	mdm@techie.com
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
 * The <code>Decoder</code> class encapsulates the details of
 * decoding an MPEG audio frame. 
 * 
 * @author	MDM	
 * @version 0.0.7 12/12/99
 * @since	0.0.5
 */
public class Decoder 
{
	/**
	 * The Obuffer instance that will receive the decoded
	 * PCM samples.
	 */
	private Obuffer			output;

	/**
	 * Synthesis filter for the left channel.
	 */
	private SynthesisFilter			filter1;

	/**
	 * Sythesis filter for the right channel.
	 */
	private SynthesisFilter			filter2;	

	/**
	 * The decoder used to decode layer III frames.
	 */
	private LayerIIIDecoder			l3decoder;
	private LayerIIDecoder			l2decoder;
	private LayerIDecoder			l1decoder;

	private int						outputFrequency;
	private int						outputChannels;
	private boolean					initialized;

	final private int channelChoice;
    final private boolean spectralContent;

	/**
	 * Creates a new <code>Decoder</code> instance with default 
	 * parameters.
	 */
	public Decoder(int channelChoice, boolean spectralContent)
	{
		this.channelChoice=channelChoice;
        this.spectralContent=spectralContent;
	}

	/**
	 * Decodes one frame from an MPEG audio bitstream.
	 *
	 * @param stream		The bitstream that provides the bits for the body of the frame.
	 * 
	 * @return A SampleBuffer containing the decoded samples.
	 * @throws JavaLayerException
	 */
	public Obuffer decodeFrame(Bitstream stream) throws JavaLayerException
	{
		if (!initialized)
			initialize(stream);

		int layer = stream.layer();
		final FrameDecoder decoder = retrieveDecoder( stream, layer);
		try
        {
            decoder.decodeFrame();
        }
        catch(ArrayIndexOutOfBoundsException oob)
        {
            throw new DecoderOutOfBounds(oob);
        }
		return output;
	}

	/**
	 * Changes the output buffer. This will take effect the next time
	 * decodeFrame() is called. 
	 */
	public void setOutputBuffer(Obuffer out)
	{
		output = out;
	}

	/**
	 * Retrieves the sample frequency of the PCM samples output
	 * by this decoder. This typically corresponds to the sample
	 * rate encoded in the MPEG audio stream.
	 * 
	 * @return the sample rate (in Hz) of the samples written to the
	 *		output buffer when decoding. 
	 */
	public int getOutputFrequency() // NO_UCD (unused code)
	{
		return outputFrequency;
	}

	/**
	 * Retrieves the number of channels of PCM samples output by
	 * this decoder. This usually corresponds to the number of
	 * channels in the MPEG audio stream, although it may differ.
	 * 
	 * @return The number of output channels in the decoded samples: 1 
	 *		for mono, or 2 for stereo.
	 *		
	 */
	public int getOutputChannels() // NO_UCD (unused code)
	{
		return outputChannels;	
	}

	private FrameDecoder retrieveDecoder( Bitstream stream, int layer) throws UnsuportedLayer
	{
		// REVIEW: allow channel output selection type
		// (LEFT, RIGHT, BOTH, DOWNMIX)
		switch (layer)
		{
		case 3:
			if (l3decoder==null)
			{
				l3decoder = new LayerIIIDecoder(stream, filter1, filter2, output, channelChoice);
			}
			return l3decoder;
		case 2:
			if (l2decoder==null)
			{
				l2decoder = new LayerIIDecoder();
				l2decoder.create(stream, filter1, filter2, output, channelChoice);				
			}
			return l2decoder;
		case 1:
			if (l1decoder==null)
			{
				l1decoder = new LayerIDecoder();
				l1decoder.create(stream, filter1, filter2, output, channelChoice);				
			}
			return l1decoder;
		}
		throw new UnsuportedLayer();
	}

	private void initialize(Header header) throws JavaLayerException
	{
		// REVIEW: allow customizable scale factor
		int mode = header.mode();
		int channels = mode==Header.SINGLE_CHANNEL ? 1 : 2;

		// set up output buffer if not set up by client.
		if (output == null)
			output = new SampleBuffer(channels);

		filter1 = new SynthesisFilter(0,spectralContent);

		// REVIEW: allow mono output for stereo
		if (channels==2) 
			filter2 = new SynthesisFilter(1,spectralContent);

		outputChannels = channels;
		outputFrequency = header.frequency();

		initialized = true;
	}

	public void seek_notify() 
	{
		if (l3decoder!=null) l3decoder.seek_notify();
		if (l2decoder!=null) l2decoder.seek_notify();
		if (l1decoder!=null) l1decoder.seek_notify();
	}

	public void reset() 
	{
		l3decoder=null;
		l2decoder=null;
		l1decoder=null;
		initialized=false;
	}
}

