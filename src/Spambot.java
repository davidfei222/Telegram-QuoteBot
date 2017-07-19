import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Scanner;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Spambot extends TelegramLongPollingBot {
	
	// These variables are for reading config.txt
	private File config;
	private Scanner reader;
	private String user;
	private String token;
	
	public Spambot()
	{
		super();
		config = new File("config.txt");
		try {
			reader = new Scanner(config);
			user = reader.next();
			token = reader.next();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onUpdateReceived(Update update) 
	{
		System.out.println("Update received at " + LocalDateTime.now());
		
		String message = "";
		
		if(update.hasMessage())
		{
			message = update.getMessage().getText().toLowerCase();
		}
		
		if(message.equals("!status"))
		{
    		SendMessage status = new SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(update.getMessage().getChatId())
                    .setText("Ready to spam the everliving shit out of someone you hate...");
            try {
                sendMessage(status); //Call method to send the message
                System.out.println("Message sent out at " + LocalDateTime.now());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
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
