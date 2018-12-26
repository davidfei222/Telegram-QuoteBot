import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class QuoteBot extends TelegramLongPollingBot {

	// Configuration variables
	private static String configFile = System.getenv("HOME") + "/.Telegram-QuoteBot/config";
	private String user;
	private String token;
	private String statusmsg;
	private String itemName;
	private String[] admins;
	private QuoteRepository repo;

	public QuoteBot()
	{
		super();
		try {
			// Parse the config file for things needed to run the bot.
			File config = new File(configFile);
			Scanner reader = new Scanner(config);
			String[] confList = new String[5];
			String line;
			
			int i = 0;
			while (reader.hasNextLine()) {
				line = reader.nextLine();
				// Skip any lines that are comments or empty lines
				if (line.matches("^#.*$") || line.matches("^$")) {
					continue;
				}
				String[] pair = line.split(":", 2);
				confList[i] = pair[1];
				i++;
			}
			
			user = confList[0].trim();
			token = confList[1].trim();
			statusmsg = confList[2].trim();
			itemName = confList[3].trim();
			String adminstr = confList[4].trim();
			admins = adminstr.split(",");
			
			reader.close();
			
		} catch(FileNotFoundException e) {
			System.out.println("Could not read or locate a valid config file.");
			e.printStackTrace();
		}
		
		repo = new QuoteRepository();
	}

	@Override
	public void onUpdateReceived(Update update)
	{
		System.out.println("Update received at " + LocalDateTime.now());
		
		Message messageObj;
		User t_user = null;
		String message = "";
		long ts = 0;
		long chatId = 0;
		// Get the message and its details from the update object.
		if (update.hasMessage()) {
			messageObj = update.getMessage();
			message = update.getMessage().getText();
			t_user = messageObj.getFrom();
			ts = messageObj.getDate();
			chatId = update.getMessage().getChatId();
		}
		else {
			return;
		}
		
		// Date wants the timestamp in milliseconds, but Telegram provides it in seconds so
		// we have to multiply it by 1000.
		Date time = new Date(ts*1000); 
		// Split the message into its components for later commands
		String[] pieces = message.split(" ", 3);
		
		// A help message for the bot.  This describes how to format commands to the bot and
		// what types of categories people can store quotes as (from the Tables variable).
		if (message.toLowerCase().equals("!help")) {
			sendHelpMsg(chatId);
		}
		// Add a quote to the db
		else if (pieces[0].equals("!add")) {
			// Validate command for correct format 
			if (pieces.length == 3 && 
				pieces[2].trim().matches("^\".*\"$")) 
			{
				// Construct object and add to database
				// Quote(String user, String type, String quote, String timestamp)
				repo.addQuote(pieces[1], pieces[2], time);
			}
			else {
				sendHelpMsg(chatId);
			}
		}
		else if (pieces[0].equals("!dump")) {
			// Validate user ID has admin rights
			if (arrayContains(admins, t_user.getId().toString())) {
				// Send message with a file containing all quotes from table
				List<Quote> quotes = repo.readQuotes();
				for (int i = 0; i < quotes.size(); i++) {
					Quote qt = quotes.get(i);
					System.out.println(qt.getQuote());
					System.out.println("    -" + qt.getName());
				}
			}
			else {
				sendHelpMsg(chatId);
			}
		}
		// Respond with a status message if none of the valid commands are used.
		else {
			sendMessage(chatId, statusmsg + "\nType !help for more information on how to use me.");
		}
	}
	
	/*
	 * Find an object in an array with linear search.
	 * 
	 * NOTE: I chose not to use the Arrays.binarySearch() method because in this use case
	 * there shouldn't ever be enough items for it to make any meaningful difference.
	 */
	private boolean arrayContains(String[] arr, String str)
	{
		for (int i = 0; i < arr.length; i++) {
			if (str.equals(arr[i])) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Send a message in response to an update
	 */
	private void sendMessage(long chatID, String str)
	{
		SendMessage help = new SendMessage()
                .setChatId(chatID)
                .setText(str);
		try {
			execute(help);
			System.out.println("Message sent out at " + LocalDateTime.now());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Send a message containing a help message when !help or incorrect command.
	 */
	private void sendHelpMsg(long chatID)
	{
		String msg = "To add a/an " + itemName + " to the database, send a private message to the bot "
        	+ "with the following format (without the <> brackets):\n"
        	+ "!add <name of person being quoted> <\"the " + itemName + " itself\">\n\n"
        	+ "To request " + itemName + " records, use the following command "
        	+ "(this will only work if you are defined as an admin user):\n"
        	+ "!dump\n";
		sendMessage(chatID, msg);
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
