# Telegram to Twitter Bot

## Usage
This Application listens to channels where a telegram bot is admin.

When any of that channels get message, 
this ```onUpdatesReceived``` method is triggered.
This method checks if update has photo.

If message has media, it's uploaded to Twitter via ```uploadChunkedMedia``` method

It translates the message via MS Bing translate.
truncates the message to 280 chars.
(>280 chars tweet is not possible via Twitter API).
If message more than 280 chars, Flood tweets are posted.
If less then 280 chars, single tweet is posted.

## Requirements:

Java 21

Maven 3

Telegram Bot for Telegram Auth

Twitter Dev account for Twitter API Auth

Azure Account for MS Translator Auth

## Installation:
set Auth values to ```application.properties```
```bash 
mvn clean install
mvn spring-boot:run
```

## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

## License
[MIT](https://choosealicense.com/licenses/mit/)


