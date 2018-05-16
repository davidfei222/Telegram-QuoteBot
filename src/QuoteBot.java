import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.HashMap;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class QuoteBot extends TelegramLongPollingBot {

	// Configuration variables
	private String configFile = System.getenv("HOME") + "/.Telegram-QuoteBot/config";
	private File config;
	private String user;
	private String token;
	private String statusmsg;
	private String db;
	private String dbuser;
	private String dbpass;
	//private String tables;

	public QuoteBot()
	{
		super();
		try {
			// Parse the config file for things needed to run the bot.
			config = new File(configFile);
			Scanner reader = new Scanner(config);
			HashMap<String, String> confMap = new HashMap<String, String>(7);
			String line;
			while (reader.hasNextLine()) {
				line = reader.nextLine();
				String[] pair = line.split(":", 2);
				confMap.put(pair[0].trim(), pair[1].trim());
			}
			user = confMap.get("User");
			token = confMap.get("Token");
			statusmsg = confMap.get("StatusMessage");
			db = confMap.get("DB");
			dbuser = confMap.get("DBuser");
			dbpass = confMap.get("DBpass");
			//tables = confMap.get("Tables");
			reader.close();
		} catch(FileNotFoundException e) {
			System.out.println("Could not read or locate the config file.");
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
		int time = 0;

		// Get the message and its details from the update object.
		if (update.hasMessage())
		{
			messageObj = update.getMessage();
			message = update.getMessage().getText();
			t_user = messageObj.getFrom();
			time = messageObj.getDate();
		}

		// Check for if the bot is alive.
		if (message.toLowerCase().equals("hi bot"))
		{
			SendMessage status = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText(statusmsg);
			try {
				execute(status); // Call method to send the message
				System.out.println("Message sent out at " + LocalDateTime.now());
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}
		
		// Personal commands for my own use.
		if (t_user.getId() == 275215669) {
			
		}
		
	}
	
	/*
	 * Add a quote sent by a group member to the appropriate database
	 */
	public void addQuote(Quote quote)
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
