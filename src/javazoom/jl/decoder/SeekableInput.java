package javazoom.jl.decoder;

import java.io.InputStream;

public abstract class SeekableInput extends InputStream
{
    // The semantics of the InputStream are somewhat different from the declared ones.
    // Read normally returns the number of bytes read or -1. Thereby it is assumed that
    // a write can be short, and consecutive reads must be called then. This is no longer
    // true. A read will fill the buffer up to the required amount.
    abstract public void unread(int howmany);
    public abstract int tell();
    public abstract void seek(int filePosition);
}
