// A class to represent a quote and its metadata
public class Quote {
	private String name;
	private String type;
	private String quote;
	private String time;
	
	public Quote(String type, String name, String quote, String time)
	{
		this.name = name;
		this.type = type;
		this.quote = quote;
		this.time = time;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getType()
	{
		return type;
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
