/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main.utils;

import allai.main.Dictionary;
import static allai.utils.ALLAILogger.logError;
import allai.utils.FileManager;
import java.io.BufferedReader;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class DefaultResponsesLoader {
    private String questions = "files\\questions.txt";
    private String responses = "files\\responses.txt";
    private BufferedReader questionsReader;
    private BufferedReader responsesReader;
    public void createResponsesDB(){
        String question;
        String response;
        questionsReader = FileManager.readFromFile(questions);
        responsesReader = FileManager.readFromFile(responses);
        Dictionary.initializeDB();;
        
        try{
            question = questionsReader.readLine();
            while (question != null){
                response = responsesReader.readLine();
                Dictionary.loadDefaultResponse(question, response);
                question = questionsReader.readLine();
            }
        } catch (Exception e){
            logError("DefaultResponsesLoader: An error occured while trying to load the default responses: " + e.getLocalizedMessage());
        }
        Dictionary.commitAndCloseDB();
    }
}
