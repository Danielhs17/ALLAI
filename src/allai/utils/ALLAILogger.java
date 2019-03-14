/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Formatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author Daniel Alejandro Hurtado Simoes Universidad de Málaga TFG - Grado en
 * Ingeniería Telemática
 */
public class ALLAILogger {

    private static Logger LOGGER = null;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        LOGGER = Logger.getLogger(ALLAILogger.class.getName());
        FileHandler fh;
        try {
            new File("logs").mkdir();
            new File("logs/ALLAI.log").delete();
            fh = new FileHandler("logs/ALLAI.log", true);
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
        } catch (IOException | SecurityException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static void logInfo(String input) {
        LOGGER.info(input.replaceAll("\n", " "));
    }

    public static void logError(String input) {
        LOGGER.log(Level.SEVERE, input);
    }
}
