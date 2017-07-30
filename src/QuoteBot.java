import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Scanner;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class QuoteBot extends TelegramLongPollingBot {
	
	// These variables are for reading config.txt
	private File config;
	private Scanner reader;
	private String user;
	private String token;
	
	public QuoteBot()
	{
		super();
		try {
			config = new File("/home/david/Documents/GitHub/Telegram-QuoteBot/config.txt");
			reader = new Scanner(config);
			user = reader.next();
			token = reader.next();
		} catch(FileNotFoundException e) {
			System.out.println("Could not read or locate the config file.");
			e.printStackTrace();
		}
	}
	
	@Override
	public void onUpdateReceived(Update update) 
	{
		System.out.println("Update received at " + LocalDateTime.now());
		
		String message = "";
		
		// Get the message from the update object
		if(update.hasMessage())
		{
			message = update.getMessage().getText();
		}
		
		// Check for if the bot is alive
		if(message.equals("!status"))
		{
			SendMessage status = new SendMessage() // Create a SendMessage object
                    .setChatId(update.getMessage().getChatId())
                    .setText("Ready to start quoting autistic statements.");
			try {
				sendMessage(status); // Call method to send the message
				System.out.println("Message sent out at " + LocalDateTime.now());
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}
		
		String[] words = message.split(" ");
		// TODO: Add recordkeeping for quotes
		if(words[0].equals("!add"))
		{
			String quote = "";
			for (int i = 1; i < words.length; i++)
			{
				quote = quote.concat(words[i] + " ");
			}
			System.out.println(quote);
		}
	}
	
	@Override
	public String getBotUsername() 
	{
		return user;
	}

	@Override
	public String getBotToken() 
	{
		return token;
	}

}
