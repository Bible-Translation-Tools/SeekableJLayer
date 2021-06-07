package javazoom.jl.decoder;

/**
 * Created by werner on 5/14/14.
 */
class DecoderOutOfBounds extends JavaLayerException
{
    DecoderOutOfBounds(Throwable e)
    {
        super("OOB",e);
    }
}
