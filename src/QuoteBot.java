import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
		// TODO: File I/O stuff (try to open file, but if it doesn't exist create it and write to it)	
		if(null != sayer && null != chat)
		{
			addQuote(chatid, message, name);
		}
	}
	
	/**
	 * Adds a new quote to the record for the specified group.
	 * 
	 * @param	id		The ID of the chat that the message originated from.
	 * @param	message		The message to be quoted.
	 * @param	name	The name of the person being quoted.
	 */
	private void addQuote(long id, String message, String name)
	{
		System.out.println("Adding quote from chat" + id);
		String filepath = workingDir + "quotes/" + id;
		File quotefile = new File(filepath);
		if (!quotefile.exists())
		{
			try {
				quotefile.createNewFile();
			} catch (IOException e) {
				System.out.println("Failed to create quote file for this chat.");
				e.printStackTrace();
			}
		}
		try {
			Writer quoteWriter = new BufferedWriter(new FileWriter(filepath, true));
			quoteWriter.append(name + "\t" + message + "\n");
			quoteWriter.close();
			System.out.println("Successfully add quote from chat" + id);
		} catch (IOException e) {
			System.out.println("Failed to open quote file for writing.");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Retrieves a quote for the record from the specified group and sends a message with it.
	 * 
	 */
	private void sendQuote()
	{
		
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
