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
import java.io.IOException;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class DefaultResponsesLoader {
    
    private int threadId = 0;
    private String questions = "files/questions.txt";
    private String responses = "files/responses.txt";
    private BufferedReader questionsReader;
    private BufferedReader responsesReader;
    private String OS = System.getProperty("os.name").toLowerCase();
    private static boolean filesInUse = false;
    
    public DefaultResponsesLoader(int threadId){
        this.threadId = threadId;
    }
    
    public void createResponsesDB(){
        if (isWindows()){
            questions = "files\\questions.txt";
            responses = "files\\responses.txt";
        }
        String question;
        String response;
        questionsReader = FileManager.readFromFile(questions);
        responsesReader = FileManager.readFromFile(responses);
        
        try{
            question = questionsReader.readLine();
            while (question != null){
                response = responsesReader.readLine();
                Dictionary.addResponse(question, response);
                question = questionsReader.readLine();
            }
        } catch (Exception e){
            logError("DefaultResponsesLoader: An error occured while trying to load the default responses: " + e.getMessage());
        }
    }
    
    public String getDefaultQuestion(String phrase){
        if (isWindows()){
            questions = "files\\questions.txt";
            responses = "files\\responses.txt";
        }
        String output = "";
        phrase = removeAccents(phrase.replaceAll(" ", "_"));
        try {
            String question;
            while (filesInUse){
                try{
                Thread.sleep(100);
                }catch(Exception ex){
                    logError("DefaultResponsesLoader " + threadId + ": Thread interrupted: " + ex.getMessage());
                }
            };
            filesInUse = true;
            questionsReader = FileManager.readFromFile(questions);
            filesInUse = false;
            question = questionsReader.readLine();
            while (question != null){
                if (phrase.contains(question)){
                    output = question;
                    break;
                }
                question = questionsReader.readLine();
            }
        } catch (IOException ex) {
            logError("DefaultResponsesLoader " + threadId + ": An error occured while getting a default question: " + ex.getMessage());
        }
        return output;
    }
    
    private String removeAccents(String texto) {
        String original = "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýÿ";
        String ascii = "AAAAAAACEEEEIIIIDNOOOOOOUUUUYBaaaaaaaceeeeiiiionoooooouuuuyy";
        String output = texto;
        for (int i = 0; i < original.length(); i++) {
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }
        return output;
    }
    
    public boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }
}
