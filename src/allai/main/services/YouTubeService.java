/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main.services;

import static allai.utils.ALLAILogger.logError;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class YouTubeService extends Service {

    private String youtube = "http://www.youtube.com/results";
    private String charset = "UTF-8";
    private String search = "";

    private int numberOfResults = 1;

    private String error = "[X] Lo siento, tuve algún problema al intentar realizar esa búsqueda.";
    private String errorNoArgument = "[X] No me has dicho qué debería buscar. Inténtalo de nuevo de esta forma: [/buscar (ALGO)] (Ejemplo: /google Napoleón";

    private enum Service {
        SEARCH, NOSEARCHQUERY
    }
    private Service service;

    public YouTubeService(String[] arg) {
        logInfo("YouTubeService: Initiated");
        int arguments = arg.length;
        if (arguments < 2) {
            service = Service.NOSEARCHQUERY;
        } else {
            service = Service.SEARCH;
            for (int x = 1; x < arguments; x++) {
                search += arg[x].replaceAll("/[^A-Za-z0-9 ]/", "") + "+";
            }
        }
    }

    private String[][] search() throws UnsupportedEncodingException, IOException {
        String[][] response = new String[numberOfResults][2];
        String userAgent = "ALLAI"; // Change this to your company's name and bot homepage!
        
        Document doc = Jsoup.connect(youtube)
            .data("search_query", search)
            .userAgent(userAgent)
            .get();

        int x=0;
        for (Element a : doc.select(".yt-lockup-title > a[title]")) {
            if (x<numberOfResults){
                response[x][0] = a.attr("title");
                response[x][1] = "http://www.youtube.com" + a.attr("href");
                x++;
            }
        }
        
        return response;
    }

    @Override
    public String getResponse() {
        String[][] result;
        String response = error;
        switch (service) {
            case SEARCH:
                try {
                result = search();
                response = "";
                for (int x = 0; x < numberOfResults; x++) {
                    response += result[x][0] + "\n" + result[x][1] + "\n\n";
                }
                } catch (IOException e){
                    logError("YouTubeService: An error occured while trying to search YouTube: " + e.getMessage());
                    response = error;
                }
                break;
            case NOSEARCHQUERY:
                response = errorNoArgument;
        }
        return response;
    }

    
}
