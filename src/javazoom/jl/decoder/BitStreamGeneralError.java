package javazoom.jl.decoder;

// TODO - rename to general error
public class BitStreamGeneralError extends BitStreamError 
{
	String t;
	BitStreamGeneralError(String s, Throwable e)
	{
		super(e);
		t=s;
	}
}
