import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Scanner;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class QuoteBot extends TelegramLongPollingBot {

	// These variables are for reading config.txt
	private String configFile = "/home/david/.Telegram-QuoteBot/config";
	private File config;
	private String user;
	private String token;

	public QuoteBot()
	{
		super();
		try {
			config = new File(configFile);
			Scanner reader = new Scanner(config);
			user = reader.next();
			token = reader.next();
			reader.close();
		} catch(FileNotFoundException e) {
			System.out.println("Could not read or locate the config file.");
			e.printStackTrace();
		}
	}

	@Override
	public void onUpdateReceived(Update update)
	{
		System.out.println("Update received at " + LocalDateTime.now());

		Message messageObj;
		User sayer = null;
		Chat chat = null;
		String message = "";
		String name = "";
		long chatid = 0;

		// Get the message from the update object
		if(update.hasMessage())
		{
			messageObj = update.getMessage();
			message = update.getMessage().getText();
			chat = messageObj.getChat();
			chatid = chat.getId();	
			sayer = messageObj.getForwardFrom();
			if (null != sayer)
			{
				name = sayer.getUserName();
			}
		}

		// Check for if the bot is alive
		if(message.toLowerCase().equals("hi bot"))
		{
			SendMessage status = new SendMessage() // Create a SendMessage object
                    .setChatId(update.getMessage().getChatId())
                    .setText("Ready to start accepting snafus, complaints, and band names.\n");
			try {
				sendMessage(status); // Call method to send the message
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
