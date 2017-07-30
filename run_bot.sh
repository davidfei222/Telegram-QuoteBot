#!/bin/bash

# This script compiles and runs the bot (so I don't have to keep typing in those commands)

cd ~/Documents/GitHub/Telegram-QuoteBot/src
javac -cp ~/Documents/GitHub/Telegram-QuoteBot/lib/telegrambots-2.4.4.5-jar-with-dependencies.jar:. Main.java
java -cp ~/Documents/GitHub/Telegram-QuoteBot/lib/telegrambots-2.4.4.5-jar-with-dependencies.jar:. Main

