# !/bin/bash

# This script creates a config file and compiles the bot.
config="$HOME/.Telegram-QuoteBot/config"

if [ -e $config ]; then
	echo "Config file already exists at $config."
else
	echo "Config file not found.  Creating new config file at $config..."
	mkdir -p "$HOME/.Telegram-QuoteBot/"
	echo -e "#Telegram QuoteBot Configuration File\nUser:\nToken:\nStatusMessage:\nTables:" > "$HOME/.Telegram-QuoteBot/config"
fi

echo "Compiling code for the bot..."
cd ~/Documents/GitHub/Telegram-QuoteBot/src
javac -cp ~/Documents/GitHub/Telegram-QuoteBot/lib/telegrambots-2.4.4.5-jar-with-dependencies.jar:. Main.java
#java -cp ~/Documents/GitHub/Telegram-QuoteBot/lib/telegrambots-2.4.4.5-jar-with-dependencies.jar:. Main
