// A class to represent a quote and its metadata
public class Quote {
	private String user;
	private String type;
	private String quote;
	private String time;
	
	public Quote(String user, String type, String quote, String time)
	{
		this.user = user;
		this.type = type;
		this.quote = quote;
		this.time = time;
	}
	
	public String getUser()
	{
		return user;
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
