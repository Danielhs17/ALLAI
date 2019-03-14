/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main.services;

//import com.google.inject.spi.Elements;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class GoogleSearchService extends Service {

    private String google = "http://www.google.com/search?q=";
    private String charset = "UTF-8";
    private String search = "";

    private int numberOfResults = 3;

    private String error = "[X] Lo siento, tuve algún problema al intentar realizar esa búsqueda.";
    private String errorNoArgument = "[X] No me has dicho qué debería buscar. Inténtalo de nuevo de esta forma: [/buscar (ALGO)] (Ejemplo: /google Napoleón";

    private enum Service {
        SEARCH, NOSEARCHQUERY
    }
    private Service service;

    public GoogleSearchService(String[] arg) {
        logInfo("GoogleService: Initiated");
        int arguments = arg.length;
        if (arguments < 2) {
            service = Service.NOSEARCHQUERY;
        } else {
            service = Service.SEARCH;
            for (int x = 1; x < arguments; x++) {
                search += arg[x].replaceAll("/[^A-Za-z0-9 ]/", "") + " ";
            }
        }
    }

    private String[][] search() throws UnsupportedEncodingException, IOException {
        String[][] response = new String[numberOfResults][2];
        String userAgent = "ALLAI"; // Change this to your company's name and bot homepage!
        Elements links = Jsoup.connect(google + URLEncoder.encode(search, charset)).userAgent(userAgent).get().select(".g>.r>a");
        int x = 0;
        while (x < links.size() && x < numberOfResults) {
            String title = links.get(x).text();
            String url = links.get(x).absUrl("href"); // Google returns URLs in format "http://www.google.com/url?q=<url>&sa=U&ei=<someKey>".
            url = URLDecoder.decode(url.substring(url.indexOf('=') + 1, url.indexOf('&')), "UTF-8");
            if (!url.startsWith("http")) {
                continue; // Ads/news/etc.
            }
            response[x][0] = title;
            response[x][1] = url;
            x++;
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
                response = "Esto es lo que he encontrado: \n\n";
                for (int x = 0; x < numberOfResults; x++) {
                    response += "- " + result[x][0] + "\n" + result[x][1] + "\n\n";
                }
                } catch (IOException e){
                    response = error;
                }
                break;
            case NOSEARCHQUERY:
                response = errorNoArgument;
        }
        return response;
    }

    
}
