/**
 * @author Werner Van Belle
 * http://bpmdj.yellowcouch.org/credits.html
 */
package org.yellowcouch.javazoom

internal class PositionEntry {
    var playPosition = 0
    var filePosition = 0

    constructor(playPos: Int, filePos: Int) {
        playPosition = playPos
        filePosition = filePos
    }

    constructor() {}
}

/**
 * At which fileposition do we find what playposition ?
 * It consists of two exclusive parallel views.
 * When building the index, the data is stored in playToFilePosition.
 * When retrieving data it is fetched from the binary searchable array.
 */
internal class DecoderToc {
    val playToFilePosition: ArrayList<PositionEntry>
    private lateinit var playPositions: IntArray
    private lateinit var filePositions: IntArray

    /**
     * Binary search to find the floor index for the given value. That is the largest index
     * for which the value is smaller or equal than the one we are looking for
     * @param value
     * @return the floor index
     */
    private fun idxLeq(value: Int): Int {
        if (value < playPositions[0]) return -1
        var lowerIdx = 0
        var upperIdx = playPositions.size - 1
        while (upperIdx > lowerIdx + 1) {
            val middleIdx = (lowerIdx + upperIdx) / 2
            val middleVal = playPositions[middleIdx]
            if (value > middleVal) lowerIdx = middleIdx else upperIdx = middleIdx
        }
        // select the correct one
        val vLower = playPositions[lowerIdx].toDouble()
        val vUpper = playPositions[upperIdx].toDouble()
        assert(vLower <= value)
        return if (vLower == value.toDouble()) lowerIdx else if (vUpper <= value) upperIdx else lowerIdx
    }

    /**
     * returns the position from where we are stable
     * @param newPlayPosition
     * @param result
     * @return
     */
    fun frameFor(newPlayPosition: Int, result: PositionEntry): Int {
        var floorIdx = idxLeq(newPlayPosition)
        /**
         * If we are sufficiently far in the file then we have 10 frames extra that we can use to stabilize the outcome.
         * This is done by going 10 frames back in the past, which is 1 index, because we only save the output every 10 frames.
         */
        return if (floorIdx > 0) {
            val stableAb = playPositions[floorIdx]
            floorIdx--
            result.playPosition = playPositions[floorIdx]
            result.filePosition = filePositions[floorIdx]
            stableAb
        } else {
            result.playPosition = 0
            result.filePosition = 0
            0
        }
    }

    /**
     * Polishes the index and makes it usable to retrieve seekto positions.
     */
    fun polish(sampleCount: Int) {
        val n = playToFilePosition.size
        playPositions = IntArray(n)
        filePositions = IntArray(n)
        for (i in 0 until n) {
            val entry = playToFilePosition[i]
            playPositions[i] = entry.playPosition
            filePositions[i] = entry.filePosition
        }
    }

    fun add(playPosition: Int, filePos: Int) {
        playToFilePosition.add(PositionEntry(playPosition, filePos))
    }

    init {
        playToFilePosition = ArrayList()
    }
}
