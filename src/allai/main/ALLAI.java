package allai.main;

import allai.interfaces.TelegramALLAIBot;
import allai.interfaces.WebSocketALLAIServer;
import allai.main.services.ReminderThread;
import static allai.utils.ALLAILogger.logError;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class ALLAI {

    private Interpreter interpreter;
    private static ArrayList<Long> quietModeOn = new ArrayList<>();
    
    private static Thread webSocketServerThread;
    private static Thread reminder;
    
    public ALLAI(int threadId){
        interpreter = new Interpreter(threadId);
    }
    
    public static void main(String[] args) throws Exception {
        logInfo("ALLAI Initializing: Launching Threads");
        initializeDictionary();
        launchWebSocketServer();
        launchTelegramBot();
        launchReminderThread();
        joinThreads();
    }

    /**
     * * Call this function to get a response from ALLAI to a given sentence.
     * @param phrase: The phrase entered by the user, that ALLAI should respond to.
     * @return A response for the given phrase ***/
    public String getResponse(String phrase) {
        return makeTextPretty(interpreter.getResponse(phrase));
    }
    
    /*** Call this function to get a response from ALLAI to a given sentence when using the Telegram Client.
     * @param phrase: The phrase entered by the user, that ALLAI should respond to.
     * @param chatId: The chatId from where the user contacted ALLAI.
     * @return A response for the given phrase ***/
    public String getResponse(String phrase, long chatId) {
        return makeTextPretty(interpreter.getResponse(phrase, chatId));
    }

    /*** Get a random phrase from ALLAI.
     * @return A String containing the phrase.***/
    public String getRandomPhrase() {
        return makeTextPretty(interpreter.getRandomPhrase());
    }
    
    /*** Checks if Quiet Mode is ON for a given chatId.
     @param chatId: The user's chatId for whom to check if quiet mode is ON.
     @return True if quiet mode is ON for this user.***/
    public static boolean quietModeOn(long chatId){
        return quietModeOn.contains(Long.valueOf(chatId));
    }
    
    /*** Sets Quiet Mode to ON for a given user's chatId.
     @param chatId: The user's chatId for whom to set quiet mode ON.***/
    public static void setQuietModeOn(long chatId){
        logInfo("ALLAI: Quiet Mode ON for Chat Id " + chatId);
        quietModeOn.add(Long.valueOf(chatId));
    }
    
    /*** Sets Quiet Mode to OFF for a given user's chatId.
     @param chatId: The user's chatId for whom to set quiet mode OFF.***/
    public static void setQuietModeOff(long chatId){
        logInfo("ALLAI: Quiet Mode OFF for Chat Id " + chatId);
        quietModeOn.remove(Long.valueOf(chatId));
    }

    /*** Returns a String with first letter as upper case and ending dot.
     * @param input: The String to be transformed
     * @return The same input String, but with the first letter as upper case,
     * and ending with a dot. ***/
    public String makeTextPretty(String input) {
        if (input.length() > 1) {
            return input.substring(0, 1).toUpperCase() + input.substring(1);
        } else {
            return input;
        }
    }

    private static void initializeDictionary() {
        Dictionary.start();
    }
        
    /*** Launch Telegram interface. ***/
    private static void launchTelegramBot() {
        ApiContextInitializer.init();
        final TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new TelegramALLAIBot());
            logInfo("ALLAI: Telegram Thread initialized");
        } catch (TelegramApiException e) {
            logError("ALLAI: ERROR, Telegram thread could not be initialized: " + e.getMessage());
        }
    }

    /*** Launch a WebSocket interface. ***/
    private static void launchWebSocketServer() throws UnknownHostException, InterruptedException {
        WebSocketALLAIServer webServer = new WebSocketALLAIServer(6789);
        webSocketServerThread = new Thread(webServer);
        webSocketServerThread.start();
        logInfo("ALLAI: Web Socket Thread initialized");
    }
    
    /*** Launch a thread to provide the reminders service. ***/
    private static void launchReminderThread() {
        reminder = new ReminderThread();
        reminder.start();
        logInfo("ALLAI: Reminder Thread initialized");
    }
    
    /*** Join threads. ***/
    private static void joinThreads() {
        try {
            webSocketServerThread.join();
            reminder.join();
        } catch (InterruptedException ex) {
            logError("ALLAI: A thread interrupted: " + ex.getMessage());
        }
    }
}
