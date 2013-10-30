package javazoom.jl.decoder;

public class BitStreamError extends JavaLayerException 
{
	Throwable t;
	BitStreamError(Throwable t)
	{
		this.t=t;
	}
}
