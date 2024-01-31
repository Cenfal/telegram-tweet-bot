# Telegram to Twitter Bot

## Usage
This Application listens to telegram channels where a telegram bot is admin.

When any of that channels get message, 
this ```onUpdatesReceived``` method is triggered.
This method checks if update has photo.

If message has media, it's uploaded to Twitter via ```uploadChunkedMedia``` method

It translates the message via _MS Bing translate_ or _Google Translate_.\
truncates the message to 280 chars.
(>280 chars tweet is not possible via Twitter API).\
If message more than 280 chars, Flood tweets are posted.
If less than 280 chars, single tweet is posted.

## Requirements:

* Java 21
* Maven 3
* Telegram Bot for Telegram Auth
* Twitter Dev account for Twitter API Auth
* Azure Account for MS Translator Auth
* Google Cloud account for Google Translate API Auth
## Installation:
set Auth values to ```application.properties```
```bash 
mvn clean install
mvn spring-boot:run
```
If you consume **Google Translation API Basic V2**;\
Create a project on Cloud Console, Get an API Key and use it.\
If you consume **Google Translation API Advanced V3**;\
Need to install Google Cloud CLI and [set up ADC](https://cloud.google.com/sdk/docs/install?_gl=1*5j19lx*_up*MQ..&gclid=Cj0KCQiA2eKtBhDcARIsAEGTG43Gy0xLXhTqrDV0nJHa1BsYiI7sTYx25QoqueBiK1yaZe8raCDpEQAaAqN_EALw_wcB&gclsrc=aw.ds#installation_instructions)
[Check here ](https://cloud.google.com/docs/authentication/provide-credentials-adc?_gl=1*1xz09gb*_up*MQ..&gclid=Cj0KCQiA2eKtBhDcARIsAEGTG43Gy0xLXhTqrDV0nJHa1BsYiI7sTYx25QoqueBiK1yaZe8raCDpEQAaAqN_EALw_wcB&gclsrc=aw.ds#local-dev)
\
and run at gcloud CLI `application-default login`
## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

## License
[MIT](https://choosealicense.com/licenses/mit/)


