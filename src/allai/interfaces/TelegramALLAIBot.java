/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.interfaces;

import allai.interfaces.keys.KEYS;
import allai.interfaces.audio.*;
import allai.main.ALLAI;
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
import java.util.Random;

/**
 * @author Daniel Alejandro Hurtado Simoes Universidad de Málaga TFG - Grado en
 * Ingeniería Telemática
 */
public class TelegramALLAIBot extends TelegramLongPollingBot {

    final private String TOKEN = KEYS.telegramBotToken;
    final private String url = "https://api.telegram.org/bot" + TOKEN + "/";

    @Override
    public void onUpdateReceived(final Update update) {
        Random r = new Random();
        int id = r.nextInt(1000)+1;
        RequestWorker thread = new RequestWorker(update, id);
        thread.start();
    }

    private void respondMessage(String response, long chatId, int id) {
        if (!response.equals("")) {
            SendMessage responseMessage = new SendMessage().setChatId(chatId).setText(response);
            try {
                execute(responseMessage);
            } catch (TelegramApiException e) {
                logError("Telegram " + id + ": ERROR, could not send telegram message to chatId " + chatId + ":  " + e.getMessage());
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
        private int id;

        public RequestWorker(Update update, int id) {
            this.update = update;
            this.id = id;
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

                logInfo("Telegram " + id + ": Got request (AUDIO) from chatId " + chatId);
                isAudio = true;
            } else if (text != null) {
                logInfo("Telegram " + id + ": Got request (TEXT) from chatId " + chatId);
                isText = true;
            }
            if (isText) {
                String messageTextReceived = update.getMessage().getText();
                logInfo("Telegram " + id + ": Received text: " + messageTextReceived);
                response = getResponse(messageTextReceived, chatId, id);
                logInfo("Telegram " + id + ": Response: " + response);
            } else if (isAudio) {
                File dir = new File("audio");
                if (!dir.exists()) {
                    try {
                        dir.mkdir();
                    } catch (SecurityException e) {
                        response = "Parece que mi elfo ayudante tuvo problemas para escuchar tu audio ahora mismo! Lo siento!";
                        logError("Telegram " + id + ": ERROR, no permission to create audio directory: " + e.getMessage());
                        return;
                    }
                }
                try {
                    TelegramAudioTranscriptor transcriptor = new TelegramAudioTranscriptor(message, url);
                    String transcribed = transcriptor.getTranscribedText();
                    logInfo("Telegram " + id + ": audio transcribed: " + transcribed);
                    response = getResponse(transcribed, chatId, id);
                    logInfo("Telegram " + id + ": Response: " + response);
                } catch (IOException e) {
                    logError("Telegram " + id + ": Error occurred during Telegram Audio transcription: " + e.getMessage());
                    response = "Parece que mi elfo ayudante tuvo problemas para escuchar tu audio ahora mismo! Lo siento!";
                }
            }
            if (response != null) {
                respondMessage(response, chatId, id);
            }
            return;
        }

        public String getResponse(String phrase, long chatId, int id) {
            ALLAI allai = new ALLAI();
            return allai.getResponse(phrase, chatId, id);
        }
    }
}
