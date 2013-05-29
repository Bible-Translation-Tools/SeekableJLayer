/**
 * @author Werner Van Belle
 */
package javazoom.jl.decoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * A buffered random file access. Helps to seek in an mp3 file
 */
public class RandomAccessStream extends InputStream
{
	private final RandomAccessFile raf;
	// the file size
	private final int fileSize;
	// start position of the buffer
	private int bufferPosition;
	private int bufferEndPosition;
	// current position in the file
	private int filePosition;
	// retrieval position for the next read
	private int retrievalPosition;

	// to help marking and resetting
	private int markedPosition=0;

	private final static int BUFFER_SIZE=8192; // is about 20 frames.
	private final byte[] buffer=new byte[BUFFER_SIZE];
	public RandomAccessStream(String fn) throws FileNotFoundException 
	{
		File f=new File(fn);
		raf=new RandomAccessFile(f, "r");
		fileSize=(int) f.length();
		bufferPosition=Integer.MIN_VALUE;
		bufferEndPosition=-1;
		retrievalPosition=0;
		filePosition=0;
	}

	@Override
	public int read() throws IOException 
	{
		fillBufferIfNecessary();
		if (retrievalPosition>=bufferEndPosition) 
			return -1;

		// If we don't &0xff the fucker, we get values between -128:127
		int result = buffer[retrievalPosition-bufferPosition]&0xff;
		++retrievalPosition;
		return result;
	}

	private void fillBufferIfNecessary() throws IOException
	{
		if (retrievalPosition>=bufferEndPosition || retrievalPosition<bufferPosition)
		{
			// When the fileposition is not the same as the endposition then we don't need a seek
			if (filePosition!=retrievalPosition)
			{
				raf.seek(retrievalPosition);
				filePosition=retrievalPosition;
			}
			int read=raf.read(buffer);
			if (read==-1) read=0;
			bufferPosition=retrievalPosition;
			filePosition += read;
			bufferEndPosition = filePosition;
		}
	}

	@Override
	public int read(byte[] target, int offset, int length) throws IOException 
	{
		if (length == 0) return 0;
		final int startOffset = offset;
		final int available = available();
		if (available == 0) return -1;
		final int todo = Math.min(length,available);
		final int stopOffset=startOffset+todo;
		while(offset<stopOffset)
		{
			fillBufferIfNecessary();
			// the retrievalposition will always fall within the buffer since we limited the stopOffset based on the filesize
			assert(retrievalPosition<bufferEndPosition); 
			// how much data do we have in the buffer
			int subBlockSize=Math.min(bufferEndPosition-retrievalPosition,stopOffset - offset);
			assert(subBlockSize>0);
			System.arraycopy(buffer, retrievalPosition-bufferPosition, target, offset, subBlockSize);
			retrievalPosition += subBlockSize;
			offset += subBlockSize;
		}
		return todo;
	}

	@Override
	public long skip(long byteCount) throws IOException 
	{
		int oldRetrievalPos = retrievalPosition;
		retrievalPosition += byteCount;
		if (retrievalPosition >= fileSize)
			retrievalPosition = fileSize;
		return retrievalPosition-oldRetrievalPos;
	}

	public void unread(int howmany)
	{
		retrievalPosition -= howmany;
		if (retrievalPosition<0) retrievalPosition=0;
	}

	@Override
	public int available() throws IOException 
	{
		return fileSize - retrievalPosition;
	}

	@Override
	public boolean markSupported() 
	{
		return true;
	}

	@Override
	public void mark(int readlimit) 
	{
		markedPosition = retrievalPosition;
	}

	@Override
	public synchronized void reset() throws IOException 
	{
		retrievalPosition = markedPosition;
	}

	@Override
	public void close() throws IOException 
	{
		super.close();
		raf.close();
	}

	public int tell() 
	{
		return retrievalPosition;
	}

	public void seek(int to) 
	{
		retrievalPosition = to;		
	}
}
