/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main.services;


import static allai.utils.ALLAILogger.logError;
import fastily.jwiki.core.*;
import static allai.utils.ALLAILogger.logInfo;


/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class WikipediaService extends Service {

    private String error = "[X] Lo siento, no puedo buscar información si no me dices sobre qué.";
    private String noEncontrado = "Lo siento, no sé qué o quién es eso!";

    
    private enum Service {
        WIKI, ERROR
    }
    private Service service;
    
    private String search = "";
    
    public WikipediaService(String[] arg) {
        logInfo("WikipediaService: Initiated");
        int arguments = arg.length;
        if (arguments < 2) {
            service = Service.ERROR;
        } else {
            service = Service.WIKI;
            for (int x = 1; x < arguments; x++) {
                String temp = arg[x].replaceAll("/[^A-Za-z0-9 ]/", "");
                search += temp.substring(0, 1).toUpperCase() + temp.substring(1) + " ";
            }
        }
        search = search.substring(0, search.length()-1);
    }
    
    private String getInfo() throws Exception {    
        Wiki wiki = new Wiki("es.wikipedia.org");
        String response;
        if (wiki.exists(search)){
            response = wiki.getTextExtract(search).split("\n")[0];
            response = quitarIndicesReferencia(response);
        }else{
            response = noEncontrado;
        }
        return response.isEmpty() ? noEncontrado : response;
    }


    @Override
    public String getResponse() {
        String response = error;
        switch (service) {
            case WIKI:
                try{
                    response = getInfo();
                }catch(Exception e){
                    response = noEncontrado;
                }
                break;
            case ERROR:
                response = error;
                break;
        }
        return response;
    }
    
    private String quitarIndicesReferencia(String paragraphs) {
        String response = "";
        boolean isText = true;
        char[] chars = new char[paragraphs.length()];
        try{
            paragraphs.getChars(0, paragraphs.length(), chars, 0);
        } catch (Exception e){
            logError("WikipediaService: I really don't know what went wrong with this one: " + e.getMessage());
        }
        for (int x=0; x<chars.length; x++){
            if (isText){
                if (chars[x] == '['){
                    isText=false;
                }else{
                    response += (char)chars[x];
                }
            }else{
                if (chars[x] == ']'){
                    isText=true;
                }
            }
        }
        return response;
    }
}
