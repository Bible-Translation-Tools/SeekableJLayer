/*
 * 14/12/12 I noticed that the scalefactor was computed for each of the outputsamples, 
 * 			while at the same time applying an equalizing for each of the input samples. 
 * 			Obviously this is a totally useless operation, since we could achieve the 
 * 			scalefactor as well using the equalizer directly. This would even be more 
 * 			efficient since the scalefactor is then applied to 32 samples, and 
 * 			automatically expanded to 512 samples. - WVB
 * 
 * 11/19/04 1.0 moved to LGPL.
 * 
 * 04/01/00 Fixes for running under build 23xx Microsoft JVM. mdm.
 * 
 * 19/12/99 Performance improvements to compute_pcm_samples().  
 *			Mat McGowan. mdm@techie.com. 
 *
 * 16/02/99 Java Conversion by E.B , javalayer@javazoom.net
 *
 *  @(#) synthesis_filter.h 1.8, last edit: 6/15/94 16:52:00
 *  @(#) Copyright (C) 1993, 1994 Tobias Bading (bading@cs.tu-berlin.de)
 *  @(#) Berlin University of Technology
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
 * WVB - TODO - we have two of these instances floating around; which is fine. Each instance will try to tell an OBuffer 
 * that it has data. This is silly because I believe we can do that at a synchronous position ourselves. That would mean
 * that we would pass two arrays to the OBuffer, and that the filter would not be responsible for knowing the OBuffer.
 */

/**
 * A class for the synthesis filter bank.
 * This class does a fast downsampling from 32, 44.1 or 48 kHz to 8 kHz, if ULAW is defined.
 * Frequencies above 4 kHz are removed by ignoring higher subbands.
 */
final class SynthesisFilter
{
	private final float[] v1;
	private final float[] v2;
	private float[]		  actual_v;					// v1 or v2
	private int 		  actual_write_pos;	   		// 0-15
	final float[]		  samples  = new float[32];	// 32 new input subband samples
	private final int	  channel;
    private final boolean spectralContent;

    public SynthesisFilter(int channelnumber, boolean spectralContent)
	{  	 
		d16 = splitArray(Sfd.SFD, 16);
		v1 = new float[512];
		v2 = new float[512];
		channel = channelnumber;
        this.spectralContent=spectralContent;
		reset();
	}

	private void reset()
	{
		for (int p=0;p<512;p++) 
			v1[p] = v2[p] = 0.0f;

		for (int p2=0;p2<32;p2++) 
			samples[p2] = 0.0f;

		actual_v = v1;
		actual_write_pos = 15;
	}

	/**
	 * Inject Sample. Only called from the layer 1 and layer 2 decoders.
	 */
	public void input_sample(float sample, int subbandnumber)
	{	 	 		  
		samples[subbandnumber] = sample;
	}

	/**
	 * Compute new values via a fast cosine transform.
	 */
	private void compute_new_v()
	{	  
		final float new_v0, new_v1, new_v2, new_v3, new_v4, new_v5, new_v6, new_v7, new_v8, new_v9;
		final float new_v10, new_v11, new_v12, new_v13, new_v14, new_v15, new_v16, new_v17, new_v18, new_v19;
		final float new_v20, new_v21, new_v22, new_v23, new_v24, new_v25, new_v26, new_v27, new_v28, new_v29;
		final float new_v30, new_v31;

		final float[] s = samples;

		final float s0 = s[0];
		final float s1 = s[1];
		final float s2 = s[2];
		final float s3 = s[3];
		final float s4 = s[4];
		final float s5 = s[5];
		final float s6 = s[6];
		final float s7 = s[7];
		final float s8 = s[8];
		final float s9 = s[9];
		final float s10 = s[10];	
		final float s11 = s[11];
		final float s12 = s[12];
		final float s13 = s[13];
		final float s14 = s[14];
		final float s15 = s[15];
		final float s16 = s[16];
		final float s17 = s[17];
		final float s18 = s[18];
		final float s19 = s[19];
		final float s20 = s[20];	
		final float s21 = s[21];
		final float s22 = s[22];
		final float s23 = s[23];
		final float s24 = s[24];
		final float s25 = s[25];
		final float s26 = s[26];
		final float s27 = s[27];
		final float s28 = s[28];
		final float s29 = s[29];
		final float s30 = s[30];	
		final float s31 = s[31];

		float p0 = s0 + s31;
		float p1 = s1 + s30;
		float p2 = s2 + s29;
		float p3 = s3 + s28;
		float p4 = s4 + s27;
		float p5 = s5 + s26;
		float p6 = s6 + s25;
		float p7 = s7 + s24;
		float p8 = s8 + s23;
		float p9 = s9 + s22;
		float p10 = s10 + s21;
		float p11 = s11 + s20;
		float p12 = s12 + s19;
		float p13 = s13 + s18;
		float p14 = s14 + s17;
		float p15 = s15 + s16;

		float pp0 = p0 + p15;
		float pp1 = p1 + p14;
		float pp2 = p2 + p13;
		float pp3 = p3 + p12;
		float pp4 = p4 + p11;
		float pp5 = p5 + p10;
		float pp6 = p6 + p9;
		float pp7 = p7 + p8;
		float pp8 = (p0 - p15) * COS_1_32;
		float pp9 = (p1 - p14) * COS_3_32;
		float pp10 = (p2 - p13) * COS_5_32;
		float pp11 = (p3 - p12) * COS_7_32;
		float pp12 = (p4 - p11) * COS_9_32;
		float pp13 = (p5 - p10) * COS_11_32;
		float pp14 = (p6 - p9) * COS_13_32;
		float pp15 = (p7 - p8) * COS_15_32;

		p0 = pp0 + pp7;
		p1 = pp1 + pp6;
		p2 = pp2 + pp5;
		p3 = pp3 + pp4;
		p4 = (pp0 - pp7) * COS_1_16;
		p5 = (pp1 - pp6) * COS_3_16;
		p6 = (pp2 - pp5) * COS_5_16;
		p7 = (pp3 - pp4) * COS_7_16;
		p8 = pp8 + pp15;
		p9 = pp9 + pp14;
		p10 = pp10 + pp13;
		p11 = pp11 + pp12;
		p12 = (pp8 - pp15) * COS_1_16;
		p13 = (pp9 - pp14) * COS_3_16;
		p14 = (pp10 - pp13) * COS_5_16;
		p15 = (pp11 - pp12) * COS_7_16;


		pp0 = p0 + p3;
		pp1 = p1 + p2;
		pp2 = (p0 - p3) * COS_1_8;
		pp3 = (p1 - p2) * COS_3_8;
		pp4 = p4 + p7;
		pp5 = p5 + p6;
		pp6 = (p4 - p7) * COS_1_8;
		pp7 = (p5 - p6) * COS_3_8;
		pp8 = p8 + p11;
		pp9 = p9 + p10;
		pp10 = (p8 - p11) * COS_1_8;
		pp11 = (p9 - p10) * COS_3_8;
		pp12 = p12 + p15;
		pp13 = p13 + p14;
		pp14 = (p12 - p15) * COS_1_8;
		pp15 = (p13 - p14) * COS_3_8;

		p0 = pp0 + pp1;
		p1 = (pp0 - pp1) * COS_1_4;
		p2 = pp2 + pp3;
		p3 = (pp2 - pp3) * COS_1_4;
		p4 = pp4 + pp5;
		p5 = (pp4 - pp5) * COS_1_4;
		p6 = pp6 + pp7;
		p7 = (pp6 - pp7) * COS_1_4;
		p8 = pp8 + pp9;
		p9 = (pp8 - pp9) * COS_1_4;
		p10 = pp10 + pp11;
		p11 = (pp10 - pp11) * COS_1_4;
		p12 = pp12 + pp13;
		p13 = (pp12 - pp13) * COS_1_4;
		p14 = pp14 + pp15;
		p15 = (pp14 - pp15) * COS_1_4;

		// this is pretty insane coding
		float tmp1;
		new_v19/*36-17*/ = -(new_v4 = (new_v12 = p7) + p5) - p6;
		new_v27/*44-17*/ = -p6 - p7 - p4;
		new_v6 = (new_v10 = (new_v14 = p15) + p11) + p13;
		new_v17/*34-17*/ = -(new_v2 = p15 + p13 + p9) - p14;
		new_v21/*38-17*/ = (tmp1 = -p14 - p15 - p10 - p11) - p13;
		new_v29/*46-17*/ = -p14 - p15 - p12 - p8;
		new_v25/*42-17*/ = tmp1 - p12;
		new_v31/*48-17*/ = -p0;
		new_v0 = p1;
		new_v23/*40-17*/ = -(new_v8 = p3) - p2;

		p0 = (s0 - s31) * COS_1_64;
		p1 = (s1 - s30) * COS_3_64;
		p2 = (s2 - s29) * COS_5_64;
		p3 = (s3 - s28) * COS_7_64;
		p4 = (s4 - s27) * COS_9_64;
		p5 = (s5 - s26) * COS_11_64;
		p6 = (s6 - s25) * COS_13_64;
		p7 = (s7 - s24) * COS_15_64;
		p8 = (s8 - s23) * COS_17_64;
		p9 = (s9 - s22) * COS_19_64;
		p10 = (s10 - s21) * COS_21_64;
		p11 = (s11 - s20) * COS_23_64;
		p12 = (s12 - s19) * COS_25_64;
		p13 = (s13 - s18) * COS_27_64;
		p14 = (s14 - s17) * COS_29_64;
		p15 = (s15 - s16) * COS_31_64;


		pp0 = p0 + p15;
		pp1 = p1 + p14;
		pp2 = p2 + p13;
		pp3 = p3 + p12;
		pp4 = p4 + p11;
		pp5 = p5 + p10;
		pp6 = p6 + p9;
		pp7 = p7 + p8;
		pp8 = (p0 - p15) * COS_1_32;
		pp9 = (p1 - p14) * COS_3_32;
		pp10 = (p2 - p13) * COS_5_32;
		pp11 = (p3 - p12) * COS_7_32;
		pp12 = (p4 - p11) * COS_9_32;
		pp13 = (p5 - p10) * COS_11_32;
		pp14 = (p6 - p9) * COS_13_32;
		pp15 = (p7 - p8) * COS_15_32;


		p0 = pp0 + pp7;
		p1 = pp1 + pp6;
		p2 = pp2 + pp5;
		p3 = pp3 + pp4;
		p4 = (pp0 - pp7) * COS_1_16;
		p5 = (pp1 - pp6) * COS_3_16;
		p6 = (pp2 - pp5) * COS_5_16;
		p7 = (pp3 - pp4) * COS_7_16;
		p8 = pp8 + pp15;
		p9 = pp9 + pp14;
		p10 = pp10 + pp13;
		p11 = pp11 + pp12;
		p12 = (pp8 - pp15) * COS_1_16;
		p13 = (pp9 - pp14) * COS_3_16;
		p14 = (pp10 - pp13) * COS_5_16;
		p15 = (pp11 - pp12) * COS_7_16;

		pp0 = p0 + p3;
		pp1 = p1 + p2;
		pp2 = (p0 - p3) * COS_1_8;
		pp3 = (p1 - p2) * COS_3_8;
		pp4 = p4 + p7;
		pp5 = p5 + p6;
		pp6 = (p4 - p7) * COS_1_8;
		pp7 = (p5 - p6) * COS_3_8;
		pp8 = p8 + p11;
		pp9 = p9 + p10;
		pp10 = (p8 - p11) * COS_1_8;
		pp11 = (p9 - p10) * COS_3_8;
		pp12 = p12 + p15;
		pp13 = p13 + p14;
		pp14 = (p12 - p15) * COS_1_8;
		pp15 = (p13 - p14) * COS_3_8;

		p0 = pp0 + pp1;
		p1 = (pp0 - pp1) * COS_1_4;
		p2 = pp2 + pp3;
		p3 = (pp2 - pp3) * COS_1_4;
		p4 = pp4 + pp5;
		p5 = (pp4 - pp5) * COS_1_4;
		p6 = pp6 + pp7;
		p7 = (pp6 - pp7) * COS_1_4;
		p8 = pp8 + pp9;
		p9 = (pp8 - pp9) * COS_1_4;
		p10 = pp10 + pp11;
		p11 = (pp10 - pp11) * COS_1_4;
		p12 = pp12 + pp13;
		p13 = (pp12 - pp13) * COS_1_4;
		p14 = pp14 + pp15;
		p15 = (pp14 - pp15) * COS_1_4;

		// manually doing something that a compiler should handle sucks
		// coding like this is hard to read
		float tmp2;
		new_v5 = (new_v11 = (new_v13 = (new_v15 = p15) + p7) + p11)
				+ p5 + p13;
		new_v7 = (new_v9 = p15 + p11 + p3) + p13;
		new_v16/*33-17*/ = -(new_v1 = (tmp1 = p13 + p15 + p9) + p1) - p14;
		new_v18/*35-17*/ = -(new_v3 = tmp1 + p5 + p7) - p6 - p14;

		new_v22/*39-17*/ = (tmp1 = -p10 - p11 - p14 - p15)
				- p13 - p2 - p3;
		new_v20/*37-17*/ = tmp1 - p13 - p5 - p6 - p7;
		new_v24/*41-17*/ = tmp1 - p12 - p2 - p3;
		new_v26/*43-17*/ = tmp1 - p12 - (tmp2 = p4 + p6 + p7);
		new_v30/*47-17*/ = (tmp1 = -p8 - p12 - p14 - p15) - p0;
		new_v28/*45-17*/ = tmp1 - tmp2;

		// insert V[0-15] (== new_v[0-15]) into actual v:	
		// float[] x2 = actual_v + actual_write_pos;
		float dest[] = actual_v;

		int pos = actual_write_pos;

		dest[pos] = new_v0;
		dest[16 + pos] = new_v1;
		dest[32 + pos] = new_v2;
		dest[48 + pos] = new_v3;
		dest[64 + pos] = new_v4;
		dest[80 + pos] = new_v5;
		dest[96 + pos] = new_v6;
		dest[112 + pos] = new_v7;
		dest[128 + pos] = new_v8;
		dest[144 + pos] = new_v9;
		dest[160 + pos] = new_v10;
		dest[176 + pos] = new_v11;
		dest[192 + pos] = new_v12;
		dest[208 + pos] = new_v13;
		dest[224 + pos] = new_v14;
		dest[240 + pos] = new_v15;

		// V[16] is always 0.0:
		dest[256 + pos] = 0.0f;

		// insert V[17-31] (== -new_v[15-1]) into actual v:
		dest[272 + pos] = -new_v15;
		dest[288 + pos] = -new_v14;
		dest[304 + pos] = -new_v13;
		dest[320 + pos] = -new_v12;
		dest[336 + pos] = -new_v11;
		dest[352 + pos] = -new_v10;
		dest[368 + pos] = -new_v9;
		dest[384 + pos] = -new_v8;
		dest[400 + pos] = -new_v7;
		dest[416 + pos] = -new_v6;
		dest[432 + pos] = -new_v5;
		dest[448 + pos] = -new_v4;
		dest[464 + pos] = -new_v3;
		dest[480 + pos] = -new_v2;
		dest[496 + pos] = -new_v1;

		// insert V[32] (== -new_v[0]) into other v:
		dest = (actual_v==v1) ? v2 : v1;

		dest[pos] = -new_v0;
		// insert V[33-48] (== new_v[16-31]) into other v:
		dest[16 + pos] = new_v16;
		dest[32 + pos] = new_v17;
		dest[48 + pos] = new_v18;
		dest[64 + pos] = new_v19;
		dest[80 + pos] = new_v20;
		dest[96 + pos] = new_v21;
		dest[112 + pos] = new_v22;
		dest[128 + pos] = new_v23;
		dest[144 + pos] = new_v24;
		dest[160 + pos] = new_v25;
		dest[176 + pos] = new_v26;
		dest[192 + pos] = new_v27;
		dest[208 + pos] = new_v28;
		dest[224 + pos] = new_v29;
		dest[240 + pos] = new_v30;
		dest[256 + pos] = new_v31;

		// insert V[49-63] (== new_v[30-16]) into other v:
		dest[272 + pos] = new_v30;
		dest[288 + pos] = new_v29;
		dest[304 + pos] = new_v28;
		dest[320 + pos] = new_v27;
		dest[336 + pos] = new_v26;
		dest[352 + pos] = new_v25;
		dest[368 + pos] = new_v24;
		dest[384 + pos] = new_v23;
		dest[400 + pos] = new_v22;
		dest[416 + pos] = new_v21;
		dest[432 + pos] = new_v20;
		dest[448 + pos] = new_v19;
		dest[464 + pos] = new_v18;
		dest[480 + pos] = new_v17;
		dest[496 + pos] = new_v16; 			
	}

	private final float[] _tmpOut = new float[32];
	private void compute_pcm_samples0( )
	{
		final float[] vp = actual_v;	
		//int inc = v_inc;
		final float[] tmpOut = _tmpOut;
		int dvp =0;

		// fat chance of having this loop unroll
		for( int i=0; i<32; i++)
		{
			float pcm_sample;
			final D16 dp = d16[i];
			pcm_sample = ((vp[dvp] * dp.d0) +
					(vp[15 + dvp] * dp.d1) +
					(vp[14 + dvp] * dp.d2) +
					(vp[13 + dvp] * dp.d3) +
					(vp[12 + dvp] * dp.d4) +
					(vp[11 + dvp] * dp.d5) +
					(vp[10 + dvp] * dp.d6) +
					(vp[9 + dvp] * dp.d7) +
					(vp[8 + dvp] * dp.d8) +
					(vp[7 + dvp] * dp.d9) +
					(vp[6 + dvp] * dp.d10) +
					(vp[5 + dvp] * dp.d11) +
					(vp[4 + dvp] * dp.d12) +
					(vp[3 + dvp] * dp.d13) +
					(vp[2 + dvp] * dp.d14) +
					(vp[1 + dvp] * dp.d15)
					);
			tmpOut[i] = pcm_sample;
			dvp += 16;
		} // for
	}
	private void compute_pcm_samples1( )
	{
		final float[] vp = actual_v;
		final float[] tmpOut = _tmpOut;
		int dvp =0;
		for( int i=0; i<32; i++)
		{
			final D16 dp = d16[i];
			float pcm_sample;
			pcm_sample = (((vp[1 + dvp] * dp.d0) +
					(vp[dvp] * dp.d1) +
					(vp[15 + dvp] * dp.d2) +
					(vp[14 + dvp] * dp.d3) +
					(vp[13 + dvp] * dp.d4) +
					(vp[12 + dvp] * dp.d5) +
					(vp[11 + dvp] * dp.d6) +
					(vp[10 + dvp] * dp.d7) +
					(vp[9 + dvp] * dp.d8) +
					(vp[8 + dvp] * dp.d9) +
					(vp[7 + dvp] * dp.d10) +
					(vp[6 + dvp] * dp.d11) +
					(vp[5 + dvp] * dp.d12) +
					(vp[4 + dvp] * dp.d13) +
					(vp[3 + dvp] * dp.d14) +
					(vp[2 + dvp] * dp.d15)
					) );

			tmpOut[i] = pcm_sample;
			dvp += 16;
		}
	}
	private void compute_pcm_samples2( )
	{
		final float[] vp = actual_v;

		//int inc = v_inc;
		final float[] tmpOut = _tmpOut;
		int dvp =0;

		// fat chance of having this loop unroll
		for( int i=0; i<32; i++)
		{
			final D16 dp = d16[i];
			float pcm_sample;

			pcm_sample = ((vp[2 + dvp] * dp.d0) +
					(vp[1 + dvp] * dp.d1) +
					(vp[dvp] * dp.d2) +
					(vp[15 + dvp] * dp.d3) +
					(vp[14 + dvp] * dp.d4) +
					(vp[13 + dvp] * dp.d5) +
					(vp[12 + dvp] * dp.d6) +
					(vp[11 + dvp] * dp.d7) +
					(vp[10 + dvp] * dp.d8) +
					(vp[9 + dvp] * dp.d9) +
					(vp[8 + dvp] * dp.d10) +
					(vp[7 + dvp] * dp.d11) +
					(vp[6 + dvp] * dp.d12) +
					(vp[5 + dvp] * dp.d13) +
					(vp[4 + dvp] * dp.d14) +
					(vp[3 + dvp] * dp.d15)
					);

			tmpOut[i] = pcm_sample;

			dvp += 16;
		} // for
	}
	private void compute_pcm_samples3( )
	{
		final float[] vp = actual_v;

		//int inc = v_inc;
		final float[] tmpOut = _tmpOut;
		int dvp =0;

		// fat chance of having this loop unroll
		for( int i=0; i<32; i++)
		{
			final D16 dp = d16[i];
			float pcm_sample;

			pcm_sample = ((vp[3 + dvp] * dp.d0) +
					(vp[2 + dvp] * dp.d1) +
					(vp[1 + dvp] * dp.d2) +
					(vp[dvp] * dp.d3) +
					(vp[15 + dvp] * dp.d4) +
					(vp[14 + dvp] * dp.d5) +
					(vp[13 + dvp] * dp.d6) +
					(vp[12 + dvp] * dp.d7) +
					(vp[11 + dvp] * dp.d8) +
					(vp[10 + dvp] * dp.d9) +
					(vp[9 + dvp] * dp.d10) +
					(vp[8 + dvp] * dp.d11) +
					(vp[7 + dvp] * dp.d12) +
					(vp[6 + dvp] * dp.d13) +
					(vp[5 + dvp] * dp.d14) +
					(vp[4 + dvp] * dp.d15)
					);

			tmpOut[i] = pcm_sample;

			dvp += 16;
		} // for
	}
	private void compute_pcm_samples4()
	{
		final float[] vp = actual_v;

		//int inc = v_inc;
		final float[] tmpOut = _tmpOut;
		int dvp =0;

		// fat chance of having this loop unroll
		for( int i=0; i<32; i++)
		{
			final D16 dp = d16[i];
			float pcm_sample;

			pcm_sample = ((vp[4 + dvp] * dp.d0) +
					(vp[3 + dvp] * dp.d1) +
					(vp[2 + dvp] * dp.d2) +
					(vp[1 + dvp] * dp.d3) +
					(vp[dvp] * dp.d4) +
					(vp[15 + dvp] * dp.d5) +
					(vp[14 + dvp] * dp.d6) +
					(vp[13 + dvp] * dp.d7) +
					(vp[12 + dvp] * dp.d8) +
					(vp[11 + dvp] * dp.d9) +
					(vp[10 + dvp] * dp.d10) +
					(vp[9 + dvp] * dp.d11) +
					(vp[8 + dvp] * dp.d12) +
					(vp[7 + dvp] * dp.d13) +
					(vp[6 + dvp] * dp.d14) +
					(vp[5 + dvp] * dp.d15)
					);

			tmpOut[i] = pcm_sample;

			dvp += 16;
		} // for
	}
	private void compute_pcm_samples5()
	{
		final float[] vp = actual_v;

		//int inc = v_inc;
		final float[] tmpOut = _tmpOut;
		int dvp =0;

		for( int i=0; i<32; i++) {
            final D16 dp = d16[i];
            float pcm_sample;
            pcm_sample = ((vp[5 + dvp] * dp.d0) +
                    (vp[4 + dvp] * dp.d1) +
                    (vp[3 + dvp] * dp.d2) +
                    (vp[2 + dvp] * dp.d3) +
                    (vp[1 + dvp] * dp.d4) +
                    (vp[dvp] * dp.d5) +
                    (vp[15 + dvp] * dp.d6) +
                    (vp[14 + dvp] * dp.d7) +
                    (vp[13 + dvp] * dp.d8) +
                    (vp[12 + dvp] * dp.d9) +
                    (vp[11 + dvp] * dp.d10) +
                    (vp[10 + dvp] * dp.d11) +
                    (vp[9 + dvp] * dp.d12) +
                    (vp[8 + dvp] * dp.d13) +
                    (vp[7 + dvp] * dp.d14) +
                    (vp[6 + dvp] * dp.d15)
            );
            tmpOut[i] = pcm_sample;
            dvp += 16;
        }
	}
	private void compute_pcm_samples6()
	{
		final float[] vp = actual_v;	
		//int inc = v_inc;
		final float[] tmpOut = _tmpOut;
		int dvp =0;

		// fat chance of having this loop unroll
		for( int i=0; i<32; i++)
		{
			final D16 dp = d16[i];
			float pcm_sample;

			pcm_sample = ((vp[6 + dvp] * dp.d0) +
					(vp[5 + dvp] * dp.d1) +
					(vp[4 + dvp] * dp.d2) +
					(vp[3 + dvp] * dp.d3) +
					(vp[2 + dvp] * dp.d4) +
					(vp[1 + dvp] * dp.d5) +
					(vp[dvp] * dp.d6) +
					(vp[15 + dvp] * dp.d7) +
					(vp[14 + dvp] * dp.d8) +
					(vp[13 + dvp] * dp.d9) +
					(vp[12 + dvp] * dp.d10) +
					(vp[11 + dvp] * dp.d11) +
					(vp[10 + dvp] * dp.d12) +
					(vp[9 + dvp] * dp.d13) +
					(vp[8 + dvp] * dp.d14) +
					(vp[7 + dvp] * dp.d15)
					);

			tmpOut[i] = pcm_sample;

			dvp += 16;
		}
	}
	private void compute_pcm_samples7()
	{
		final float[] vp = actual_v;
		final float[] tmpOut = _tmpOut;
		int dvp =0;

		for( int i=0; i<32; i++)
		{
			final D16 dp = d16[i];
			float pcm_sample;

			pcm_sample = ((vp[7 + dvp] * dp.d0) +
					(vp[6 + dvp] * dp.d1) +
					(vp[5 + dvp] * dp.d2) +
					(vp[4 + dvp] * dp.d3) +
					(vp[3 + dvp] * dp.d4) +
					(vp[2 + dvp] * dp.d5) +
					(vp[1 + dvp] * dp.d6) +
					(vp[dvp] * dp.d7) +
					(vp[15 + dvp] * dp.d8) +
					(vp[14 + dvp] * dp.d9) +
					(vp[13 + dvp] * dp.d10) +
					(vp[12 + dvp] * dp.d11) +
					(vp[11 + dvp] * dp.d12) +
					(vp[10 + dvp] * dp.d13) +
					(vp[9 + dvp] * dp.d14) +
					(vp[8 + dvp] * dp.d15)
					);

			tmpOut[i] = pcm_sample;
			dvp += 16;
		}
	}
	private void compute_pcm_samples8()
	{
		final float[] vp = actual_v;
		final float[] tmpOut = _tmpOut;
		int dvp =0;
		for( int i=0; i<32; i++)
		{
			final D16 dp = d16[i];
			float pcm_sample;

			pcm_sample = ((vp[8 + dvp] * dp.d0) +
					(vp[7 + dvp] * dp.d1) +
					(vp[6 + dvp] * dp.d2) +
					(vp[5 + dvp] * dp.d3) +
					(vp[4 + dvp] * dp.d4) +
					(vp[3 + dvp] * dp.d5) +
					(vp[2 + dvp] * dp.d6) +
					(vp[1 + dvp] * dp.d7) +
					(vp[dvp] * dp.d8) +
					(vp[15 + dvp] * dp.d9) +
					(vp[14 + dvp] * dp.d10) +
					(vp[13 + dvp] * dp.d11) +
					(vp[12 + dvp] * dp.d12) +
					(vp[11 + dvp] * dp.d13) +
					(vp[10 + dvp] * dp.d14) +
					(vp[9 + dvp] * dp.d15)
					);

			tmpOut[i] = pcm_sample;
			dvp += 16;
		}
	}
	private void compute_pcm_samples9()
	{
		final float[] vp = actual_v;
		final float[] tmpOut = _tmpOut;
		int dvp =0;
		for( int i=0; i<32; i++)
		{
			final D16 dp = d16[i];
			float pcm_sample;

			pcm_sample = ((vp[9 + dvp] * dp.d0) +
					(vp[8 + dvp] * dp.d1) +
					(vp[7 + dvp] * dp.d2) +
					(vp[6 + dvp] * dp.d3) +
					(vp[5 + dvp] * dp.d4) +
					(vp[4 + dvp] * dp.d5) +
					(vp[3 + dvp] * dp.d6) +
					(vp[2 + dvp] * dp.d7) +
					(vp[1 + dvp] * dp.d8) +
					(vp[dvp] * dp.d9) +
					(vp[15 + dvp] * dp.d10) +
					(vp[14 + dvp] * dp.d11) +
					(vp[13 + dvp] * dp.d12) +
					(vp[12 + dvp] * dp.d13) +
					(vp[11 + dvp] * dp.d14) +
					(vp[10 + dvp] * dp.d15)
					);
			tmpOut[i] = pcm_sample;
			dvp += 16;
		}
	}
	private void compute_pcm_samples10()
	{
		final float[] vp = actual_v;
		final float[] tmpOut = _tmpOut;
		int dvp =0;
		for( int i=0; i<32; i++)
		{
			final D16 dp = d16[i];
			float pcm_sample;
			pcm_sample = ((vp[10 + dvp] * dp.d0) +
					(vp[9 + dvp] * dp.d1) +
					(vp[8 + dvp] * dp.d2) +
					(vp[7 + dvp] * dp.d3) +
					(vp[6 + dvp] * dp.d4) +
					(vp[5 + dvp] * dp.d5) +
					(vp[4 + dvp] * dp.d6) +
					(vp[3 + dvp] * dp.d7) +
					(vp[2 + dvp] * dp.d8) +
					(vp[1 + dvp] * dp.d9) +
					(vp[dvp] * dp.d10) +
					(vp[15 + dvp] * dp.d11) +
					(vp[14 + dvp] * dp.d12) +
					(vp[13 + dvp] * dp.d13) +
					(vp[12 + dvp] * dp.d14) +
					(vp[11 + dvp] * dp.d15)
					);

			tmpOut[i] = pcm_sample;
			dvp += 16;
		}
	}
	private void compute_pcm_samples11()
	{
		final float[] vp = actual_v;
		final float[] tmpOut = _tmpOut;
		int dvp =0;
		for( int i=0; i<32; i++)
		{
			final D16 dp = d16[i];
			float pcm_sample;
			pcm_sample = ((vp[11 + dvp] * dp.d0) +
					(vp[10 + dvp] * dp.d1) +
					(vp[9 + dvp] * dp.d2) +
					(vp[8 + dvp] * dp.d3) +
					(vp[7 + dvp] * dp.d4) +
					(vp[6 + dvp] * dp.d5) +
					(vp[5 + dvp] * dp.d6) +
					(vp[4 + dvp] * dp.d7) +
					(vp[3 + dvp] * dp.d8) +
					(vp[2 + dvp] * dp.d9) +
					(vp[1 + dvp] * dp.d10) +
					(vp[dvp] * dp.d11) +
					(vp[15 + dvp] * dp.d12) +
					(vp[14 + dvp] * dp.d13) +
					(vp[13 + dvp] * dp.d14) +
					(vp[12 + dvp] * dp.d15)
					);
			tmpOut[i] = pcm_sample;
			dvp += 16;
		}
	}
	private void compute_pcm_samples12()
	{
		final float[] vp = actual_v;	
		//int inc = v_inc;
		final float[] tmpOut = _tmpOut;
		int dvp =0;

		// fat chance of having this loop unroll
		for( int i=0; i<32; i++)
		{
			final D16 dp = d16[i];
			float pcm_sample;

			pcm_sample = ((vp[12 + dvp] * dp.d0) +
					(vp[11 + dvp] * dp.d1) +
					(vp[10 + dvp] * dp.d2) +
					(vp[9 + dvp] * dp.d3) +
					(vp[8 + dvp] * dp.d4) +
					(vp[7 + dvp] * dp.d5) +
					(vp[6 + dvp] * dp.d6) +
					(vp[5 + dvp] * dp.d7) +
					(vp[4 + dvp] * dp.d8) +
					(vp[3 + dvp] * dp.d9) +
					(vp[2 + dvp] * dp.d10) +
					(vp[1 + dvp] * dp.d11) +
					(vp[dvp] * dp.d12) +
					(vp[15 + dvp] * dp.d13) +
					(vp[14 + dvp] * dp.d14) +
					(vp[13 + dvp] * dp.d15)
					);

			tmpOut[i] = pcm_sample;

			dvp += 16;
		}
	}
	private void compute_pcm_samples13()
	{
		final float[] vp = actual_v;
		final float[] tmpOut = _tmpOut;
		int dvp =0;
		for( int i=0; i<32; i++)
		{
			final D16 dp = d16[i];
			float pcm_sample;
			pcm_sample = ((vp[13 + dvp] * dp.d0) +
					(vp[12 + dvp] * dp.d1) +
					(vp[11 + dvp] * dp.d2) +
					(vp[10 + dvp] * dp.d3) +
					(vp[9 + dvp] * dp.d4) +
					(vp[8 + dvp] * dp.d5) +
					(vp[7 + dvp] * dp.d6) +
					(vp[6 + dvp] * dp.d7) +
					(vp[5 + dvp] * dp.d8) +
					(vp[4 + dvp] * dp.d9) +
					(vp[3 + dvp] * dp.d10) +
					(vp[2 + dvp] * dp.d11) +
					(vp[1 + dvp] * dp.d12) +
					(vp[dvp] * dp.d13) +
					(vp[15 + dvp] * dp.d14) +
					(vp[14 + dvp] * dp.d15)
					);
			tmpOut[i] = pcm_sample;
			dvp += 16;
		}
	}
	private void compute_pcm_samples14()
	{
		final float[] vp = actual_v;
		final float[] tmpOut = _tmpOut;
		int dvp =0;
		for( int i=0; i<32; i++)
		{
			final D16 dp = d16[i];
			float pcm_sample;

			pcm_sample = (vp[14 + dvp] * dp.d0) +
					(vp[13 + dvp] * dp.d1) +
					(vp[12 + dvp] * dp.d2) +
					(vp[11 + dvp] * dp.d3) +
					(vp[10 + dvp] * dp.d4) +
					(vp[9 + dvp] * dp.d5) +
					(vp[8 + dvp] * dp.d6) +
					(vp[7 + dvp] * dp.d7) +
					(vp[6 + dvp] * dp.d8) +
					(vp[5 + dvp] * dp.d9) +
					(vp[4 + dvp] * dp.d10) +
					(vp[3 + dvp] * dp.d11) +
					(vp[2 + dvp] * dp.d12) +
					(vp[1 + dvp] * dp.d13) +
					(vp[dvp] * dp.d14) +
					(vp[15 + dvp] * dp.d15);
			tmpOut[i] = pcm_sample;
			dvp += 16;
		}
	}
	private void compute_pcm_samples15()
	{
		final float[] vp = actual_v;
		final float[] tmpOut = _tmpOut;
		int dvp =0;
		for( int i=0; i<32; i++)
		{
			float pcm_sample;
			final D16 dp = d16[i];
			pcm_sample = (vp[15 + dvp] * dp.d0) +
					(vp[14 + dvp] * dp.d1) +
					(vp[13 + dvp] * dp.d2) +
					(vp[12 + dvp] * dp.d3) +
					(vp[11 + dvp] * dp.d4) +
					(vp[10 + dvp] * dp.d5) +
					(vp[9 + dvp] * dp.d6) +
					(vp[8 + dvp] * dp.d7) +
					(vp[7 + dvp] * dp.d8) +
					(vp[6 + dvp] * dp.d9) +
					(vp[5 + dvp] * dp.d10) +
					(vp[4 + dvp] * dp.d11) +
					(vp[3 + dvp] * dp.d12) +
					(vp[2 + dvp] * dp.d13) +
					(vp[1 + dvp] * dp.d14) +
					(vp[dvp] * dp.d15);
			tmpOut[i] = pcm_sample;			
			dvp += 16;
		}
	}

	private void compute_pcm_samples(Obuffer buffer)
	{
		switch (actual_write_pos)
		{
		case 0: 
			compute_pcm_samples0();
			break;
		case 1: 
			compute_pcm_samples1();
			break;
		case 2: 
			compute_pcm_samples2();
			break;
		case 3: 
			compute_pcm_samples3();
			break;
		case 4: 
			compute_pcm_samples4();
			break;
		case 5: 
			compute_pcm_samples5();
			break;
		case 6: 
			compute_pcm_samples6();
			break;
		case 7: 
			compute_pcm_samples7();
			break;
		case 8: 
			compute_pcm_samples8();
			break;
		case 9: 
			compute_pcm_samples9();
			break;
		case 10: 
			compute_pcm_samples10();
			break;
		case 11: 
			compute_pcm_samples11();
			break;
		case 12: 
			compute_pcm_samples12();
			break;
		case 13: 
			compute_pcm_samples13();
			break;
		case 14: 
			compute_pcm_samples14();
			break;
		case 15: 
			compute_pcm_samples15();
			break;
		}

		if (buffer!=null)
			buffer.appendSamples(channel, _tmpOut);
	}

	/**
	 * Calculate 32 PCM samples and put the into the Obuffer-object.
	 */
	public void calculate_pcm_samples_layer_iii(Obuffer buffer)
	{
        if (spectralContent)
        {
            buffer.appendSamples(channel,samples);
        }
        else
        {
            compute_new_v();
            compute_pcm_samples(buffer);
            actual_write_pos = (actual_write_pos + 1) & 0xf;
            actual_v = (actual_v == v1) ? v2 : v1;
        }
	}

	public void calculate_pcm_samples_layer_i_ii(Obuffer buffer)
	{
		calculate_pcm_samples_layer_iii(buffer);
		// MDM: this may not be necessary. The Layer III decoder always
		// outputs 32 subband samples, but I haven't checked layer I & II.
		for (int p=0;p<32;p++) 
			samples[p] = 0.0f;
	}

	private static final double MY_PI = 3.14159265358979323846;
	private static final float COS_1_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI        / 64.0)));
	private static final float COS_3_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 3.0  / 64.0)));
	private static final float COS_5_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 5.0  / 64.0)));
	private static final float COS_7_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 7.0  / 64.0)));
	private static final float COS_9_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 9.0  / 64.0)));
	private static final float COS_11_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 11.0 / 64.0)));
	private static final float COS_13_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 13.0 / 64.0)));
	private static final float COS_15_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 15.0 / 64.0)));
	private static final float COS_17_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 17.0 / 64.0)));
	private static final float COS_19_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 19.0 / 64.0)));
	private static final float COS_21_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 21.0 / 64.0)));
	private static final float COS_23_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 23.0 / 64.0)));
	private static final float COS_25_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 25.0 / 64.0)));
	private static final float COS_27_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 27.0 / 64.0)));
	private static final float COS_29_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 29.0 / 64.0)));
	private static final float COS_31_64 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 31.0 / 64.0)));
	private static final float COS_1_32 =(float) (1.0 / (2.0 * Math.cos(MY_PI        / 32.0)));
	private static final float COS_3_32 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 3.0  / 32.0)));
	private static final float COS_5_32 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 5.0  / 32.0)));
	private static final float COS_7_32 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 7.0  / 32.0)));
	private static final float COS_9_32 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 9.0  / 32.0)));
	private static final float COS_11_32 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 11.0 / 32.0)));
	private static final float COS_13_32 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 13.0 / 32.0)));
	private static final float COS_15_32 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 15.0 / 32.0)));
	private static final float COS_1_16 =(float) (1.0 / (2.0 * Math.cos(MY_PI        / 16.0)));
	private static final float COS_3_16 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 3.0  / 16.0)));
	private static final float COS_5_16 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 5.0  / 16.0)));
	private static final float COS_7_16 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 7.0  / 16.0)));
	private static final float COS_1_8 =(float) (1.0 / (2.0 * Math.cos(MY_PI        / 8.0)));
	private static final float COS_3_8 =(float) (1.0 / (2.0 * Math.cos(MY_PI * 3.0  / 8.0)));
	private static final float COS_1_4 =(float) (1.0 / (2.0 * Math.cos(MY_PI / 4.0)));

	/** 
	 * d[] split into subarrays of length 16. This provides for
	 * faster access by allowing a block of 16 to be addressed
	 * with constant offset. 
	 **/
	static private final class D16
	{
		final float d0;
		final float d1;
		final float d2;
		final float d3;
		final float d4;
		final float d5;
		final float d6;
		final float d7;
		final float d8;
		final float d9;
		final float d10;
		final float d11;
		final float d12;
		final float d13;
		final float d14;
		final float d15;
		D16(float[] d)
		{
			d0=d[0];
			d1=d[1];
			d2=d[2];
			d3=d[3];
			d4=d[4];
			d5=d[5];
			d6=d[6];
			d7=d[7];
			d8=d[8];
			d9=d[9];
			d10=d[10];
			d11=d[11];
			d12=d[12];
			d13=d[13];
			d14=d[14];
			d15=d[15];
		}
	}

	private static D16 d16[] = null;

	/**
	 * Converts a 1D array into a number of smaller arrays. This is used
	 * to achieve offset + constant indexing into an array. Each sub-array
	 * represents a block of values of the original array. 
	 * @param array			The array to split up into blocks.
	 * @param blockSize		The size of the blocks to split the array
	 *						into. This must be an exact divisor of
	 *						the length of the array, or some data
	 *						will be lost from the main array.
	 * 
	 * @return	An array of arrays in which each element in the returned
	 *			array will be of length <code>blockSize</code>.
	 */
	static private D16[] splitArray(final float[] array, final int blockSize)
	{
		int size = array.length / blockSize;
		D16[] split = new D16[size];
		for (int i=0; i<size; i++)
			split[i] = subArray(array, i*blockSize, blockSize);
		return split;
	}

	/**
	 * Returns a subarray of an existing array.
	 * 
	 * @param array	The array to retrieve a subarra from.
	 * @param offs	The offset in the array that corresponds to
	 *				the first index of the subarray.
	 * @param len	The number of indeces in the subarray.
	 * @return The subarray, which may be of length 0.
	 */
	static private D16 subArray(final float[] array, final int offs, int len)
	{
		if (offs+len > array.length)
			len = array.length-offs;
		if (len < 0) 
			len = 0;

		float[] subarray = new float[len];
		for (int i=0; i<len; i++)
			subarray[i] = array[offs+i];
		return new D16(subarray);
	}
}
