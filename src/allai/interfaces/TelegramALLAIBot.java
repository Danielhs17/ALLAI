/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.interfaces;

import allai.interfaces.keys.KEYS;
import allai.interfaces.audio.*;
import static allai.main.ALLAI.makeTextPretty;
import allai.main.Interpreter;
import static allai.utils.ALLAILogger.logError;
import java.io.File;
import java.io.IOException;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes Universidad de Málaga TFG - Grado en
 * Ingeniería Telemática
 */
public class TelegramALLAIBot extends TelegramLongPollingBot {

    final private String TOKEN = KEYS.telegramBotToken;
    final private String url = "https://api.telegram.org/bot" + TOKEN + "/";

    @Override
    public void onUpdateReceived(final Update update) {
        RequestWorker thread = new RequestWorker(update);
        thread.start();
    }

    private void respondMessage(String response, long chatId) {
        if (!response.equals("")) {
            SendMessage responseMessage = new SendMessage().setChatId(chatId).setText(response);
            try {
                execute(responseMessage);
            } catch (TelegramApiException e) {
                logError("Telegram: ERROR, could not send telegram message to chatId " + chatId + ":  " + e.getMessage());
            }
        }
    }

    @Override
    public String getBotUsername() {
        // Se devuelve el nombre que dimos al bot al crearlo con el BotFather
        return "AllaiBot";
    }

    @Override
    public String getBotToken() {
        // Se devuelve el token que nos generó el BotFather de nuestro bot
        return TOKEN;
    }

    public class RequestWorker extends Thread {

        private Update update;
        private long id;

        public RequestWorker(Update update) {
            this.update = update;
            id = Thread.currentThread().getId();
        }

        @Override
        public void run() {
            boolean isAudio = false;
            boolean isText = false;
            String response = null;
            Message message = update.getMessage();
            final long chatId = update.getMessage().getChatId();
            Voice voice = message.getVoice();
            String text = message.getText();
            if (voice != null) {

                logInfo("Telegram: Got request (AUDIO) from chatId " + chatId);
                isAudio = true;
            } else if (text != null) {
                logInfo("Telegram: Got request (TEXT) from chatId " + chatId);
                isText = true;
            }
            if (isText) {
                String messageTextReceived = update.getMessage().getText();
                logInfo("Telegram: Received text: " + messageTextReceived);
                response = getResponse(messageTextReceived, chatId);
                logInfo("Telegram: Response: " + response);
            } else if (isAudio) {
                File dir = new File("audio");
                if (!dir.exists()) {
                    try {
                        dir.mkdir();
                    } catch (SecurityException e) {
                        logError("Telegram: ERROR, no permission to create audio directory: " + e.getMessage());
                        return;
                    }
                }
                try {
                    TelegramAudioTranscriptor transcriptor = new TelegramAudioTranscriptor(message, url);
                    String transcribed = transcriptor.getTranscribedText();
                    logInfo("Telegram: audio transcribed: " + transcribed);
                    response = getResponse(transcribed, chatId);
                    logInfo("Telegram: Response: " + response);
                } catch (IOException e) {
                    logError("Telegram: Error occurred during Telegram Audio transcription: " + e.getMessage());
                }
            }
            respondMessage(response, chatId);
            return;
        }

        public String getResponse(String phrase, long chatId) {
            Interpreter interpreter = new Interpreter();
            return makeTextPretty(interpreter.getResponse(phrase, chatId));
        }
    }
}
