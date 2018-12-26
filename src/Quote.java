import java.util.Date;

// A class to represent a quote and its metadata
public class Quote {
	private int ID;
	private String Name;
	private String Quote;
	private Date Date;
	
	public Quote(String name, String quote, Date time)
	{
		this.Name = name;
		this.Quote = quote;
		this.Date = time;
	}
	
	public void setID(int id)
	{
		this.ID = id;
	}
	
	public int getID()
	{
		return this.ID;
	}
	
	public void setName(String name)
	{
		this.Name = name;
	}
	
	public String getName()
	{
		return Name;
	}
	
	public void setQuote(String quote)
	{
		this.Quote = quote;
	}
	
	public String getQuote()
	{
		return Quote;
	}
	
	public void setDate(Date time)
	{
		this.Date = time;
	}
	
	public Date getDate()
	{
		return Date;
	}
}
