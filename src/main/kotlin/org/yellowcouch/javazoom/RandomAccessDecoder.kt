/**
 * @author Werner Van Belle
 * http://bpmdj.yellowcouch.org/credits.html
 */
package org.yellowcouch.javazoom

import java.io.IOException
import java.util.logging.Logger
import javazoom.jl.decoder.BitStreamEOF
import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.Decoder
import javazoom.jl.decoder.JavaLayerException
import javazoom.jl.decoder.Obuffer
import javazoom.jl.decoder.OutputChannels
import javazoom.jl.decoder.SeekableFile

/**
 * This is a fairly specialized class.
 * - when a file is opened it generate a table of contents
 * - that table of contents is then used to seek into the mp3 in a 'crude' manner (11 frames in front of the required playposition)
 * - the reported data is then placed at the correct position in the buffer such that the user of this class can simply
 * - seek(wantedPosition) and retrieve the data from audioShorts.
 */
class RandomAccessDecoder(filename: String?) : Obuffer {
    /**
     * How many samples are currently in the leftRight buffer ?
     */
    private var filledUpToSamples = 0

    /**
     * is set when the leftRightBuffer is filled with the last piece of data
     */
    private var isLastBuffer = false

    /**
     * The playposition at the start of the Buffer, in 'playrate' samples.
     * The positioning is expressed in 32 bit integers, which means that we can at most
     * play files with 2G of data, which should not be a problem for a dj app.
     */
    private var playPosition: Int

    /**
     * The playposition of the next frame that will be decoded. Is set through a seek.
     */
    private var nextBufferPlayPosition = 0

    /**
     * how many samples can be decoded in this file, -1 if unknown.
     */
    var sampleCount = -1
    val audioShorts = ShortArray(BUFFER_LAST + 1)
    private var bufferSize = 0
    private var bufferFrontIdx = 0
    private var bufferNextIdx = 0
    private var playPosAtFront = Int.MAX_VALUE
    private var playPosAtNext = Int.MIN_VALUE

    /**
     * Seeks the buffer to the required position and ensure a certain amount of samples is present.
     * @param retrieveFromPlayPosition the position that should be present in the buffer
     * @param wantedSamples including so many samples
     * @return the offset (in shorts) in the buffer where the playposition starts.
     */
    @Throws(IOException::class)
    fun seek(retrieveFromPlayPosition: Int, wantedSamples: Int): Int {
        val wantedShorts = wantedSamples shl 1
        if (wantedShorts > BUFFER_LAST) return -1
        /*
		  When the playposition is smaller than the current playposition, then we need a hard seek.
		  or when the playposition is larger than the bufferend + the walkdistance
		 */if (retrieveFromPlayPosition < playPosAtFront || retrieveFromPlayPosition > playPosAtNext + walkDistance) {
            val skipEverythingBefore = seekCrude(retrieveFromPlayPosition)

            // Reset all flags, which will then result in a retrieval of all relevant information
            bufferFrontIdx = 0
            bufferNextIdx = 0
            if (retrieveFromPlayPosition < 0) {
                playPosAtFront = retrieveFromPlayPosition
                playPosAtNext = retrieveFromPlayPosition
            } else if (retrieveFromPlayPosition >= sampleCount) {
                playPosAtFront = retrieveFromPlayPosition
                playPosAtNext = retrieveFromPlayPosition
            } else {
                playPosAtFront = skipEverythingBefore
                playPosAtNext = skipEverythingBefore
            }
            bufferSize = 0
        }
        val retrievalEndPosition = retrieveFromPlayPosition + wantedSamples
        while (retrievalEndPosition > playPosAtNext) {
            // Chop of the front of the buffer if we won't have enough space to store
            // a decoded frame.
            var writableShorts = audioShorts.size - bufferSize
            if (writableShorts < RESERVE_SHORTS) {
                bufferFrontIdx = bufferFrontIdx + RESERVE_SHORTS and BUFFER_LAST
                playPosAtFront += RESERVE_SHORTS shr 1
                bufferSize -= RESERVE_SHORTS
            }
            writableShorts = audioShorts.size - bufferSize
            // if the next playposition is < 0 or we've seen the last buffer then we just fill up zeros
            if (playPosAtNext < 0 || isLastBuffer) {
                val todoShorts = if (isLastBuffer) writableShorts else Math.min(writableShorts, -2 * playPosAtNext)
                for (i in 0 until todoShorts) {
                    audioShorts[bufferNextIdx] = 0
                    bufferNextIdx = bufferNextIdx + 1 and BUFFER_LAST
                }
                playPosAtNext += todoShorts shr 1
                bufferSize += todoShorts
            } else {
                // we decode a frame.
                val todoShorts = decodeFrame(bufferNextIdx)
                if (isLastBuffer) continue
                // if it is before the place where we should be we simple ditch the buffer.
                // this can happen due to the priming of the mp3 decoder.
                if (playPosition < playPosAtNext) continue
                bufferNextIdx = bufferNextIdx + todoShorts and BUFFER_LAST
                playPosAtNext += filledUpToSamples
                bufferSize += todoShorts
            }
            if (bufferSize > audioShorts.size) LOG.severe("Apparently we have more data than the buffer allows")
        }

        // the position where the buffer starts is at
        val result = retrieveFromPlayPosition - playPosAtFront shl 1
        assert(result >= 0)
        return result + bufferFrontIdx
    }

    // -------------------------------------
    private var nextWriteLeft = 0
    private var nextWriteRight = 1
    override fun appendSamples(channel: Int, f: FloatArray) {
        var pos = if (channel == 0) nextWriteLeft else nextWriteRight
        val buffer = audioShorts
        for (i in 0..31) {
            val fs = f[i]
            if (fs > 32767f) buffer[pos] = 32767 else if (fs < -32768f) buffer[pos] = -32768 else buffer[pos] =
                fs.toInt().toShort()
            pos = pos + Obuffer.MAXCHANNELS and BUFFER_LAST
        }
        if (channel == 0) nextWriteLeft = pos else nextWriteRight = pos
    }

    private fun setBufferOffset(offset: Int) {
        nextWriteLeft = offset
        nextWriteRight = offset + 1
    }

    // ---------------------------------------------------------------
    private val fileInput: SeekableFile
    private val bitstream: Bitstream
    private val decoder = Decoder(OutputChannels.BOTH_CHANNELS, false)
    private val index: DecoderToc
    private val entry: PositionEntry = PositionEntry()
    @Throws(IOException::class)
    private fun buildIndex() {
        playPosition = 0
        try {
            var frameCount = 0
            do {
                if (frameCount % 10 == 0) {
                    val filePos = fileInput.tell()
                    index.add(playPosition, filePos)
                }
                bitstream.readFrame()
                bitstream.closeFrame()
                val samples = bitstream.samplesPerFrame()
                playPosition += samples
                frameCount++
            } while (true)
        } catch (e: BitStreamEOF) {
            // Normal end
        } catch (e: JavaLayerException) {
            e.printStackTrace()
        }
        sampleCount = playPosition
        index.polish(sampleCount)
        index.frameFor(0, entry)
        fileInput.seek(entry.filePosition)
        decoder.seek_notify()
    }

    /**
     * Requests a decode into the targetbuffer at starting position start.
     * @return returns the number of shorts written in the buffer.
     */
    @Throws(IOException::class)
    private fun decodeFrame(offset: Int): Int {
        if (isLastBuffer) return 0
        val playPosition = nextBufferPlayPosition
        try {
            bitstream.readFrame()
        } catch (e1: JavaLayerException) {
            isLastBuffer = true
            return 0
        }
        setBufferOffset(offset)
        val sampleCountAccordingToHeader = bitstream.samplesPerFrame()
        nextBufferPlayPosition += sampleCountAccordingToHeader
        try {
            decoder.decodeFrame(bitstream)
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        } catch (e: JavaLayerException) {
        }

        /*
		  It is possible that the produced data is not the same as what we were expecting
		  in that case we still assume that the data was produced, otherwise a variety of
		  playposition counters will fail.
		 */
        var shorts = nextWriteLeft - offset
        if (shorts < 0) shorts += audioShorts.size
        val sampleCount = shorts / Obuffer.MAXCHANNELS
        if (sampleCount != sampleCountAccordingToHeader && sampleCount != 0) // TODO - clear the remainder of the buffer.
            LOG.warning("Header sampleCount ($sampleCountAccordingToHeader)vs actual sampleCount ($sampleCount)don't match")
        bitstream.closeFrame()
        filledUpToSamples = sampleCountAccordingToHeader
        this.playPosition = playPosition
        return filledUpToSamples * 2
    }

    fun stop() // NO_UCD (unused code)
    {
        try {
            // The bitstream closes its source, which is the RandomAccessStream, so we
            // don't need to close that ourselves.
            bitstream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Positions the file sufficiently in front of the wanted playPosition.
     * This is of course not accurate.
     * The acurate playpositon is however reported correctly in nextBufferPlayPosition.
     *
     * @return the first playposition that is stable.
     */
    private fun seekCrude(wantedPlayPosition: Int): Int {
        val stableAb: Int = index.frameFor(wantedPlayPosition, entry)
        return if (wantedPlayPosition >= sampleCount) {
            isLastBuffer = true
            0
        } else if (entry.playPosition === 0) {
            // If that is not possible we jump to the start of the file
            fileInput.seek(0)
            nextBufferPlayPosition = 0
            decoder.reset()
            isLastBuffer = false
            stableAb
        } else {
            fileInput.seek(entry.filePosition)
            decoder.seek_notify()
            nextBufferPlayPosition = entry.playPosition
            isLastBuffer = false
            stableAb
        }
    }

    companion object {
        private const val RESERVE_SHORTS = 1152 * 2
        const val BUFFER_LAST = 0x1ffff
        private const val walkDistance = 576 * 20
        private val LOG = Logger.getLogger(RandomAccessDecoder::class.java.name)
    }

    init {
        fileInput = SeekableFile(filename)
        bitstream = Bitstream(fileInput)
        decoder.setOutputBuffer(this, bitstream)
        index = DecoderToc()
        buildIndex()
        playPosition = 0
    }
}
