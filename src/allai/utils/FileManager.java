/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.utils;

import static allai.utils.ALLAILogger.logError;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class FileManager {

    public static BufferedReader readFromFile(String filename) {
        BufferedReader output = null;
        File file;
        try {
            file = new File(filename);
            output = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            logError("FileManager: ERROR, File not found: " + e.getMessage());
        }
        return output;
    }

    public static void writeToFile(String filename, String content) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(filename, "UTF-8");
            writer.println(content);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            logError("FileManager: An error occured while writing a file: " + ex.getMessage());
        }
    }

}
