/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.utils;

import static allai.utils.ALLAILogger.logError;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class FileManager_old {

    public static BufferedReader readFromFile(String filename) {
        BufferedReader output = null;
        try {
            try {
                output = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                logError("FileManager: Unsupported encoding (not going to happen ever...)");
            }
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
