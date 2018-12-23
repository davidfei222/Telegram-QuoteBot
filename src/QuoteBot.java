import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
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
	private String tables;
	private String admins;

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
			tables = confMap.get("Tables");
			admins = confMap.get("Admins");
			reader.close();
		} catch(FileNotFoundException e) {
			System.out.println("Could not read or locate a valid config file.");
			e.printStackTrace();
		}
		
		/*System.out.println(user);
		System.out.println(token);
		System.out.println(statusmsg);
		System.out.println(db);
		System.out.println(dbuser);
		System.out.println(dbpass);*/
	}

	@Override
	public void onUpdateReceived(Update update)
	{
		System.out.println("Update received at " + LocalDateTime.now());
		
		Message messageObj;
		User t_user = null;
		String message = "";
		long ts = 0;
		// Get the message and its details from the update object.
		if (update.hasMessage())
		{
			messageObj = update.getMessage();
			message = update.getMessage().getText();
			t_user = messageObj.getFrom();
			ts = messageObj.getDate();
		}
		// Convert the unix timestamp of the message to appropriate format
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date wants the timestamp in milliseconds, but Telegram provides it in seconds so
		// we have to multiply it by 1000.
		String time = fmt.format(new Date(ts*1000)); 
		//System.out.println(time);
		// Split the message into its components for later commands
		String[] pieces = message.split(" ", 4);
		
		// A help message for the bot.  This describes how to format commands to the bot and
		// what types of categories people can store quotes as (from the Tables variable).
		if (message.toLowerCase().equals("!help")) {
			sendHelpMessage(update);
		}
		// Add a quote to the db
		else if (pieces[0].equals("!add")) {
			// Validate command for correct format 
			
			// Quote(String user, String type, String quote, String timestamp)
			Quote item = new Quote(pieces[1], pieces[2], pieces[3], time);
			addQuote(item);
		}
		else if (pieces[0].equals("!dump")) {
			// Validate table exists
			// Send message with a file attached to it
		}
		// Respond with a status message if none of the valid commands are used.
		else {
			SendMessage status = new SendMessage()
					.setChatId(update.getMessage().getChatId())
					.setText(statusmsg + "\nType !help for more information on how to use me.");
			try {
				execute(status); // Call method to send the message
				System.out.println("Status message sent out at " + LocalDateTime.now());
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}
		
		// Personal commands for my own use.
		if (t_user.getId() == 275215669) {
			// TODO: Implement special commands that only I can use
		}
	}
	
	/*
	 * Add a quote sent by a group member to the appropriate database
	 */
	private void addQuote(Quote quote)
	{
		// TODO: Implement writing quotes to the database
		System.out.println(quote.getType() + "\n" + quote.getUser() + "\n" + quote.getQuote() + "\n" + quote.getTime());
	}
	
	/*
	 * Send a message containing a help message when !help or incorrect command.
	 */
	private void sendHelpMessage(Update update)
	{
		SendMessage help = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText("To add a quote to the database, send a private message to the bot "
                		+ "with the following format (without the <> brackets):\n"
                		+ "!add <type of quote> <name of person being quoted> <\"the quote itself\">\n\n"
                		+ "I can currently store the following types of quotes:\n"
                		+ tables + "\n\n"
                		+ "To request the records for a type of quote, use the following command "
                		+ "(this will only work if you are defined as an admin user):\n"
                		+ "!dump <type of quote>\n");
		try {
			execute(help); // Call method to send the message
			System.out.println("Help message sent out at " + LocalDateTime.now());
		} catch (TelegramApiException e) {
			e.printStackTrace();
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
