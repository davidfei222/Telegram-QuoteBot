import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Scanner;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

public class Spambot extends TelegramLongPollingBot {
	
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
		// TODO Auto-generated method stub
		System.out.println("Update received at" + LocalDateTime.now());
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
