import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	private String workingDir;
	private File config;
	private String user;
	private String token;

	public QuoteBot()
	{
		super();
		workingDir = "/home/david/Documents/GitHub/Telegram-QuoteBot/";
		try {
			config = new File(workingDir + "config.txt");
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

		// Get the message from the update object
		if(update.hasMessage())
		{
			messageObj = update.getMessage();
			chat = messageObj.getChat();
			sayer = messageObj.getForwardFrom();
			message = update.getMessage().getText();
		}

		// Check for if the bot is alive
		if(message.equals("!status"))
		{
			SendMessage status = new SendMessage() // Create a SendMessage object
                    .setChatId(update.getMessage().getChatId())
                    .setText("Ready to start quoting autistic statements.\n"
						+ "To add a quote for the bot to remember, " 
						+ "forward the original message containing the quote to it.");
			try {
				sendMessage(status); // Call method to send the message
				System.out.println("Message sent out at " + LocalDateTime.now());
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}
		/*
		String[] words = message.split(" ");
		// TODO: Add recordkeeping for quotes (use forwarded messages)
		if(words[0].equals("!add"))
		{
			String name = words[1];
			String quote = "";
			for (int i = 2; i < words.length; i++)
			{
				quote = quote.concat(words[i] + " ");
			}
			// System.out.println(quote);
		}*/
		// TODO: File I/O stuff (try to open file, but if it doesn't exist create it and write to it
		if(null != sayer && null != chat)
		{
			File quotefile = new File(workingDir + "quotes/" + chat.toString());
			if (!quotefile.exists())
			{
				try {
					quotefile.createNewFile();
				} catch (IOException e) {
					System.out.println("Failed to create file.");
					e.printStackTrace();
				}
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
