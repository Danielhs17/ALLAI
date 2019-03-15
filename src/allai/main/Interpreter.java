/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main;

import java.util.ArrayList;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class Interpreter {
    
    private final LanguageAnalyzer analyzer;
    private final CommandProcessor processor;
    private ArrayList<String> implicitCommands;
    private ArrayList<String> relativeCommands;
    private int threadId = 0;

    public Interpreter(int threadId) {
        this.threadId = threadId;
        processor = new CommandProcessor(threadId);
        analyzer = new LanguageAnalyzer(null, false, false, threadId);
        initializeImplicitCommandsList();
    }

    /*** Call this function to get a response from ALLAI to a given sentence.
     * @param phrase: The phrase entered by the user, that ALLAI should respond to.
     * @return A response for the given phrase ***/
    public String getResponse(String phrase) {
        if (isACommand(phrase)) {
            String translated = translateCommand(phrase);
            String responded = processor.respondCommand(translated, 0);
            if (responded.startsWith("[X]") && !processor.isARawCommand(phrase)) {
                return analyzer.getResponse(phrase.toLowerCase());
            } else {
                return responded;
            }
        } else {
                return analyzer.getResponse(phrase.toLowerCase());
        }
    }
    
    /*** Call this function to get a response from ALLAI to a given sentence when using the Telegram Client.
     * @param phrase: The phrase entered by the user, that ALLAI should respond to.
     * @param chatId: The chatId from where the user contacted ALLAI.
     * @return A response for the given phrase ***/
    public String getResponse(String phrase, long chatId){
        if (isACommand(phrase)) {
            logInfo("Interpreter " + threadId + ": Received message is a command");
            String translated = translateCommand(phrase);
            String responded = processor.respondCommand(translated, chatId);
            if (responded.startsWith("[X]") && !processor.isARawCommand(phrase)) {
                return analyzer.getResponse(phrase.toLowerCase());
            } else {
                return responded;
            }
        } else {
            logInfo("Interpreter " + threadId + ": Received message is NOT a command");
            if (ALLAI.quietModeOn(chatId)){
                logInfo("Interpreter " + threadId + ": QuietMode is ON, not responding");
                return "";
            } else {
                logInfo("Interpreter " + threadId + ": QuietMode is OFF, getting response");
                return analyzer.getResponse(phrase.toLowerCase());
            }
        }
    }

    /*** Get a random phrase from ALLAI.
     * @return A String containing the phrase.***/
    public String getRandomPhrase() {
        return analyzer.getRandomPhrase();
    }

    /*** Determines if a phrase received from the user is a command.
     * @return True if it is a comand or false otherwise.***/
    private boolean isACommand(String phrase) {
        if (processor.isARawCommand(phrase)) {
            return true;
        } else {
            //Determinar si es un comando aunque no empiece con /
            if (isInTheImplicitComandList(phrase.toLowerCase())) {
                return true;
            } else {
                return false;
            }
        }
    }

    /*** Translates a common phrase that could be interpreted as a command, to a command.
     * @param phrase: The phrase entered by the user.
     * @return A command equivalent to the received phrase, or the phrase itself
     * if it was already a command. ***/
    private String translateCommand(String phrase) {
        if (processor.isARawCommand(phrase)) {
            return phrase;
        } else {
            logInfo("Interpreter " + threadId + ": Not a raw command, translating");
            String relative = getRelativeCommand(phrase.toLowerCase());
            logInfo("Interpreter " + threadId + ": Translated to " + relative);
            return relative;
        }
    }

    /*** @return True if phrase may be interpreted as a implicit command. ***/
    private boolean isInTheImplicitComandList(String phrase) {
        boolean found = false;
        int x = 0;
        while (x < implicitCommands.size() && !found) {
            if (phrase.startsWith(implicitCommands.get(x))) {
                found = true;
            }
            x++;
        }
        return found;
    }

    /*** @return The relative command to the given implicit command. ***/
    private String getRelativeCommand(String phrase) {
        int position = -1;
        int x = 0;
        while (x < implicitCommands.size() && position < 0) {
            if (phrase.startsWith(implicitCommands.get(x))) {
                position = x;
            }
            x++;
        }
        return phrase.replace(implicitCommands.get(position), relativeCommands.get(position));
    }

    /*** Add a ImplicitCommand-RelativeCommand pair to the list. ***/
    private void addCommandPair(String implicit, String relative) {
        implicitCommands.add(implicit);
        relativeCommands.add(relative);
    }

    /*** Initialize ImplicitCommand-RelativeCommand list. ***/
    private void initializeImplicitCommandsList() {
        implicitCommands = new ArrayList<>();
        relativeCommands = new ArrayList<>();
        addCommandPair("googlea", "/google");
        addCommandPair("busca en google", "/google");
        addCommandPair("cuentame un chiste", "/chiste");
        addCommandPair("cuéntame un chiste", "/chiste");
        addCommandPair("ayuda", "/ayuda");
        addCommandPair("ayúdame", "/ayuda");
        addCommandPair("ayudame", "/ayuda");
        addCommandPair("cuanto le falta al bus", "/bus parada");
        addCommandPair("cuánto le falta al bus", "/bus parada");
        addCommandPair("donde aparco", "/parking");
        addCommandPair("dónde aparco", "/parking");
        addCommandPair("donde puedo aparcar", "/parking");
        addCommandPair("dónde puedo aparcar", "/parking");
        addCommandPair("busca aparcamiento", "/parking");
        addCommandPair("buscar aparcamiento", "/parking");
        addCommandPair("busca el video", "/youtube");
        addCommandPair("buscar video", "/youtube");
        addCommandPair("busca el vídeo", "/youtube");
        addCommandPair("buscar vídeo", "/youtube");
        addCommandPair("que es un ", "/wiki ");
        addCommandPair("qué es un ", "/wiki ");
        addCommandPair("que es una ", "/wiki ");
        addCommandPair("qué es una ", "/wiki ");
        addCommandPair("que es ", "/wiki ");
        addCommandPair("quien es ", "/wiki ");
        addCommandPair("qué es ", "/wiki ");
        addCommandPair("quién es ", "/wiki ");
        addCommandPair("clima en ", "/clima ");
        addCommandPair("clima", "/clima");
        addCommandPair("traduce a ", "/traducir ");
        addCommandPair("crear lista ", "/lista crear ");
        addCommandPair("eliminar lista ", "/lista eliminar ");
        addCommandPair("vaciar lista ", "/lista vaciar ");
        addCommandPair("mostrar lista ", "/lista mostrar ");
        addCommandPair("mostrar listas", "/lista todas ");     
        addCommandPair("recuérdame ", "/recordar ");
        addCommandPair("recuerdame ", "/recordar ");     
        addCommandPair("allai cállate", "/callar");
        addCommandPair("allai callate", "/callar");
        addCommandPair("allai, callate", "/callar");
        addCommandPair("allai, habla", "/hablar");
        addCommandPair("allai habla", "/hablar");
    }
}
