package javazoom.jl.decoder;

import java.io.InputStream;

public abstract class SeekableInput extends InputStream
{
    abstract public void unread(int howmany);
    public abstract int tell();
    public abstract void seek(int filePosition);
}
