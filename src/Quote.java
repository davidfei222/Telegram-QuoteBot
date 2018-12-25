// A class to represent a quote and its metadata
public class Quote {
	private String name;
	private String quote;
	private String time;
	
	public Quote( String name, String quote, String time)
	{
		this.name = name;
		this.quote = quote;
		this.time = time;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getQuote()
	{
		return quote;
	}
	
	public String getTime()
	{
		return time;
	}
}
