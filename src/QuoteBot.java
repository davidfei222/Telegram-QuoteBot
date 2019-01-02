import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.apache.shiro.session.Session;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.session.DefaultChatIdConverter;
import org.telegram.telegrambots.session.TelegramLongPollingSessionBot;

public class QuoteBot extends TelegramLongPollingSessionBot {

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
		super(new DefaultChatIdConverter());
		
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
	public void onUpdateReceived(Update update, Optional<Session> optionalSession)
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
		
		// Prevent the use of this bot from a group chat
		if (!messageObj.getChat().isUserChat()) {
			if (message.matches("@" + user)) {
				sendMessage(chatId, "The use of this bot is restricted to DMs, to add a/an " + itemName
						+ " please send a direct message to @" + user);
			}
			return;
		}
		
		// Determine if this user is currently in the middle of an action
		Session sess;
		if ((sess = optionalSession.get()).getAttribute("ActionSeq") != null) {
			String currAction = (String)sess.getAttribute("ActionSeq");
			boolean isEnded = false;
			switch (currAction) {
			case "!add":
				isEnded = addActionSeq(sess, message, chatId);
				break;
			default:
				System.err.println("An unrecognized action sequence " + currAction + "is being used.");
				break;
			}
			// Stop and clear the session if the action has finished (the library will create a fresh one
			// when it receives the next update).
			if (isEnded) {
				sess.stop();
			}
		}
		// If not, start an action sequence or handle single message commands
		else {
			switch (message) {
			case "!help":
				sendHelpMsg(chatId);
				break;
			case "!add":
				optionalSession.get().setAttribute("ActionSeq", message);
				optionalSession.get().setAttribute("ActionDate", time);
				addActionSeq(optionalSession.get(), message, chatId);
				break;
			case "!dump":
				dumpAction(chatId, t_user);
				break;
			default:
				sendMessage(chatId, statusmsg + "\nType !help for more information on how to use me.");
				break;
			}
		}
	}
	
	/*
	 * Handle a session for creating and adding a new quote to the db.
	 * 
	 * Returns true or false to indicate whether it is safe to terminate the session.
	 */
	private boolean addActionSeq(Session sess, String msg, long chatId)
	{
		if (sess.getAttribute("LastAction") == null) {
			sendMessage(chatId, "Tell me who said this " + itemName + ".");
			sess.setAttribute("LastAction", "Ask for quotee");
		}
		else if (((String)sess.getAttribute("LastAction")).equals("Ask for quotee")) {
			// If the bot just asked for the quotee, then save the response as such.
			sess.setAttribute("Quotee", msg);
			sendMessage(chatId, "Now tell me what they said.");
			sess.setAttribute("LastAction", "Ask for quote");
		}
		else if (((String)sess.getAttribute("LastAction")).equals("Ask for quote")) {
			// If the bot just asked for the quote, then this session has all the info 
			// it needs and can write the item to the database.
			int id = repo.addQuote((String)sess.getAttribute("Quotee"), msg, 
					(Date)sess.getAttribute("ActionDate"));
			if (id > 0) {
				sendMessage(chatId, "Successfully saved " + itemName + ".");
			}
			else {
				sendMessage(chatId, "Failed to save " + itemName + " due to problems with the database.");
			}
			return true;
		}
		return false;
	}
	
	/*
	 * The action triggered when the user asks for a dump (no sequence necessary).
	 * 
	 * Retrieves all items from the db and writes them to a file that can be read easily by humans.
	 */
	private void dumpAction(long chatId, User user)
	{
		// Validate user ID has admin rights
		if (arrayContains(admins, user.getId().toString())) {
			// Send message with a file containing all quotes from table
			List<Quote> quotes = repo.readQuotes();
			for (int i = 0; i < quotes.size(); i++) {
				Quote qt = quotes.get(i);
				String quote = "\"" + qt.getQuote() + "\"\n    -" + qt.getName() + "\n\n";
				try {
					Files.write(Paths.get(itemName + "s.txt"), quote.getBytes(StandardCharsets.UTF_8),
							StandardOpenOption.CREATE, StandardOpenOption.APPEND);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			sendDoc(chatId, itemName + "s.txt");
			// Delete the file when finished to save space on disk
			try {
				Files.delete(Paths.get(itemName + "s.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			sendMessage(chatId, "You do not have admin rights, command not executed.");
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
	 * Send a message to the user
	 */
	private void sendMessage(long chatID, String str)
	{
		SendMessage msg = new SendMessage();
		msg.setChatId(chatID);
		msg.setText(str);
		
		try {
			execute(msg);
			System.out.println("Message sent out at " + LocalDateTime.now());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Send a document to the user
	 */
	private void sendDoc(long chatId, String filePath)
	{
		SendDocument doc = new SendDocument();
		doc.setChatId(chatId);
		doc.setDocument(new File(filePath));
		
		try {
			execute(doc);
			System.out.println("Dump file sent out at " + LocalDateTime.now());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Send a message containing a help message when !help or incorrect command.
	 * 
	 * The help message describes what commands can be initiated.
	 */
	private void sendHelpMsg(long chatID)
	{
		String msg = "To add a/an " + itemName + " to the database, send a message "
			+ "to the bot containing this command:\n"
			+ "!add\n\n"
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
