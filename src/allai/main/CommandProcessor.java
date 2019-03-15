/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main;

import allai.main.services.*;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class CommandProcessor {

    private int threadId;
    
    public String error = "[X] No he entendido el comando que has enviado. Por favor, utiliza el comando \"/ayuda\" para más información.";
    private String startCommand = "/start";
    private String busCommand = "/bus";
    private String parkingCommand = "/parking";
    private String helpCommand = "/ayuda";
    private String searchCommand = "/google";
    private String jokeCommand = "/chiste";
    private String youtubeCommand = "/youtube";
    private String wikipediaCommand = "/wiki";
    private String translateCommand = "/traducir";
    private String listCommand = "/lista";
    private String weatherCommand = "/clima";
    private String remindCommand = "/recordar";
    private String quietOn = "/callar";
    private String quietOff = "/hablar";
    
    private String quietOnResponse = "Está bien, a partir de ahora solo contestaré a comandos! Si quieres que vuelva a hablar envía /hablar o dime \"Allai, habla\"";
    private String quietOffResponse = "Bien! Empezaré a hablar de nuevo!";

    public CommandProcessor(int threadId){
        this.threadId = threadId;
    }
    
    /*** Determines if a phrase received from the user is a raw command. A raw command is a command in the shape /command [arguments].
     @param input: The possible command.
     @return True if it is a raw comand or false otherwise.***/
    public boolean isARawCommand(String input) {
        return input.startsWith("/");
    }

    /*** Receives a command, processes it and responds it.
     * @param input: The command to be responded.
     * @param chatId: The chatId associated to the received command.
     * @return A response for the given command.***/
    public String respondCommand(String input, long chatId) {
        logInfo("CommandProcessor " + threadId + ": Command received, processing");
        return processCommand(input, chatId);
    }

    private String processCommand(String input, long chatId) {
        String[] args = split(input);
        if (input.startsWith(busCommand)){
            return getServiceOutput(new BusService(args));
        } else if (input.startsWith(parkingCommand)){
            return getServiceOutput(new ParkingService(args));
        } else if (input.startsWith(helpCommand) || input.startsWith(startCommand)){
            return getServiceOutput(new HelpService(args));
        } else if (input.startsWith(searchCommand)){
            return getServiceOutput(new GoogleSearchService(args));
        } else if (input.startsWith(jokeCommand)){
            return getServiceOutput(new ChisteService(args));
        } else if (input.startsWith(youtubeCommand)){
            return getServiceOutput(new YouTubeService(args));
        } else if (input.startsWith(wikipediaCommand)){
            return getServiceOutput(new WikipediaService(args));
        } else if (input.startsWith(translateCommand)){
            return getServiceOutput(new TranslationService(args));
        } else if (input.startsWith(listCommand) && chatId != 0){
            return getServiceOutput(new ListService(args, chatId));
        } else if (input.startsWith(weatherCommand)){
            return getServiceOutput(new WeatherService(args));
        } else if (input.startsWith(remindCommand) && chatId != 0){
            return getServiceOutput(new ReminderService(args, chatId));
        } else if (input.startsWith(quietOn) && chatId != 0){
            setQuietMode(true, chatId);
            return quietOnResponse;
        } else if (input.startsWith(quietOff) && chatId != 0){
            setQuietMode(false, chatId);
            return quietOffResponse;
        } else {
            return error;
        }
    }

    private String[] split(String input){
        return input.split(" ");
    }
    
    private String getServiceOutput(Service service) {
        return service.getResponse();
    }

    private void setQuietMode(boolean b, long chatId) {
        if (b){
            ALLAI.setQuietModeOn(chatId);
        }else{
            ALLAI.setQuietModeOff(chatId);
        }
    }
}
