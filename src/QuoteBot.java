import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.HashMap;
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
	private String db;
	private String dbuser;
	private String dbpass;
	private String[] tables;
	private String[] admins;

	public QuoteBot()
	{
		super();
		try {
			// Parse the config file for things needed to run the bot.
			File config = new File(configFile);
			Scanner reader = new Scanner(config);
			HashMap<String, String> confMap = new HashMap<String, String>(8);
			String line;
			
			while (reader.hasNextLine()) {
				line = reader.nextLine();
				// Skip any lines that are comments or empty lines
				if (line.matches("^#.*$") || line.matches("^$")) {
					continue;
				}
				String[] pair = line.split(":", 2);
				confMap.put(pair[0].trim(), pair[1].trim());
			}
			
			user = confMap.get("User");
			token = confMap.get("Token");
			statusmsg = confMap.get("StatusMessage");
			db = confMap.get("DB");
			dbuser = confMap.get("DBuser");
			dbpass = confMap.get("DBpass");
			
			String tablesstr = confMap.get("Tables");
			tables = tablesstr.split(",");
			
			String adminstr = confMap.get("Admins");
			admins = adminstr.split(",");
			
			reader.close();
		} catch(FileNotFoundException e) {
			System.out.println("Could not read or locate a valid config file.");
			e.printStackTrace();
		}
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
		
		// Convert the unix timestamp of the message to appropriate format
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date wants the timestamp in milliseconds, but Telegram provides it in seconds so
		// we have to multiply it by 1000.
		String time = fmt.format(new Date(ts*1000)); 
		// Split the message into its components for later commands
		String[] pieces = message.split(" ", 4);
		
		// A help message for the bot.  This describes how to format commands to the bot and
		// what types of categories people can store quotes as (from the Tables variable).
		if (message.toLowerCase().equals("!help")) {
			sendHelpMsg(chatId);
		}
		// Add a quote to the db
		else if (pieces[0].equals("!add")) {
			// Validate command for correct format 
			if (pieces.length == 4 && 
				pieces[3].trim().matches("^\".*\"$") &&
				arrayContains(tables, pieces[1])) 
			{
				// Construct object and add to database
				// Quote(String user, String type, String quote, String timestamp)
				Quote item = new Quote(pieces[1], pieces[2], pieces[3], time);
				addQuote(item);
			}
			else {
				sendHelpMsg(chatId);
			}
		}
		else if (pieces[0].equals("!dump")) {
			// Validate correct format, table exists and user ID has admin rights
			if (pieces.length == 2 &&
				arrayContains(tables, pieces[1]) && 
				arrayContains(admins, t_user.getId().toString())) 
			{
				// Send message with a file containing all quotes from table
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
	 * Add a quote sent by a group member to the appropriate database.
	 */
	private void addQuote(Quote quote)
	{
		// TODO: Implement writing quotes to the database
		System.out.println(quote.getType() + "\n" + quote.getName() + "\n" + quote.getQuote() + "\n" + quote.getTime());
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
		String msg = "To add a quote to the database, send a private message to the bot "
        	+ "with the following format (without the <> brackets):\n"
        	+ "!add <type of quote> <name of person being quoted> <\"the quote itself\">\n\n"
        	+ "The following types of quotes can be filled in for <type of quote>:\n"
        	+ Arrays.toString(tables) + "\n\n"
        	+ "To request the records for a type of quote, use the following command "
        	+ "(this will only work if you are defined as an admin user):\n"
        	+ "!dump <type of quote>\n";
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
