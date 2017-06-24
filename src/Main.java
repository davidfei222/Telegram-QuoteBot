import java.time.LocalDateTime;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Main 
{
	public static void main(String[] args) 
	{	
		ApiContextInitializer.init();	
		TelegramBotsApi botsApi = new TelegramBotsApi();
		
		try {
			botsApi.registerBot(new Spambot());
	        System.out.println("Bot logged in at " + LocalDateTime.now());
	    } catch (TelegramApiException e) {
	        e.printStackTrace();
	    }
	}
}
