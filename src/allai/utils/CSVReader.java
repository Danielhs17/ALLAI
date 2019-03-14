/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.utils;

import static allai.utils.ALLAILogger.logError;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class CSVReader {

    private final String csvSplitBy;
    private BufferedReader br;

    public CSVReader(String csvFile, String split) {
        this.csvSplitBy = split;

        try {
            URL url = new URL(csvFile);
            URLConnection connection = url.openConnection();
            InputStreamReader input = new InputStreamReader(connection.getInputStream());
            br = new BufferedReader(input);
        } catch (FileNotFoundException e) {
            logError("CSVReader: ERROR, CSV File not found: " + e.getLocalizedMessage());
        } catch (MalformedURLException e) {
            logError("CSVReader: ERROR, Malformed URL Exception: " + e.getLocalizedMessage());
        } catch (IOException e) {
            logError("CSVReader: ERROR, IO Exception: " + e.getLocalizedMessage());
        }
    }

    public String[] getNextLine() {
        String rawLine = null;
        String[] line = null;

        try {
            rawLine = br.readLine();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (rawLine != null){
            rawLine = rawLine.replace("\"", "");
            line = rawLine.split(csvSplitBy);
            return line;
        }else{
            return null;
        }
    }

    public void closeFile() {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
