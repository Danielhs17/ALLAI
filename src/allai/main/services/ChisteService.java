/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main.services;

import allai.utils.HTTPUtils;
import java.util.ArrayList;
import org.jsoup.parser.Parser;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class ChisteService extends Service {

    private String chistesURL = "http://www.holasoyramon.com/chistes/aleatorio/";
    private String error = "[X] Lo siento, no me he sentido lo suficientemente gracioso. Quizás has añadido palabras de más. Intenta con /chiste";
    
    private enum Service {
        CHISTE, ERROR
    }
    private Service service;
    
    public ChisteService(String[] arg) {
        logInfo("ChisteService: Initiated");
        int arguments = arg.length;
        if (arguments != 1) {
            service = Service.ERROR;
        } else {
            service = Service.CHISTE;
        }
    }
    
    private String getJoke() throws Exception {
        String html = HTTPUtils.doGet(chistesURL);
        String half = html.split("Chiste Aleatorio:")[1];
        String jokeBeginning = half.split("</h1>")[1].substring(1);
        String jokeSection = jokeBeginning.split("</div>")[0];
        String joke = jokeSection.replaceAll("<p>", "").replaceAll("</p>", "\n");
        String decoded = Parser.unescapeEntities(joke, true);
        if (decoded.toLowerCase().contains("chiste enviado")){
            String[] lines = decoded.split("\n");
            String temp = "";
            for (int x=0; x<lines.length-1; x++){
                temp += lines[x];
            }
            decoded = temp;
        }
        return decoded;
    }


    @Override
    public String getResponse() {
        String response = error;
        switch (service) {
            case CHISTE:
                try{
                    response = getJoke();
                    while ((response.length() >= 400) || blackList(response)){ //Max Length for Telegram / Black List
                        response = getJoke();
                    }
                }catch(Exception e){
                    response = error;
                }
                break;
            case ERROR:
                response = error;
                break;
        }
        return response;
    }
    
    private boolean blackList(String joke) {
        boolean blackJoke = false;
        ArrayList<String> blackList = new ArrayList<>();
        blackList.add("negro");
        blackList.add("gay");
        blackList.add("maricón");
        blackList.add("mujer");
        blackList.add("homosexual");
        blackList.add("puto");
        blackList.add("puta");
        blackList.add("prostituta");
        blackList.add("panchito");
        blackList.add("mierda");
        blackList.add("sexo");
        blackList.add("follo");
        blackList.add("andaluz");
        blackList.add("catal");
        blackList.add("joder");
        blackList.add("judio");
        blackList.add("judío");
        blackList.add("jodido");
        blackList.add("musulm");
        blackList.add("moro");
        blackList.add("mora");
        blackList.add("coño");
        blackList.add("ostia");
        blackList.add("69");
        blackList.add("folla");
        blackList.add("chica");
        blackList.add("lepe");
        blackList.add("etiop");
        blackList.add("etíop");
        blackList.add("africa");
        blackList.add("áfrica");
        blackList.add("vagina");
        blackList.add("pene");
        blackList.add("israel");
        blackList.add("venezuela");
        blackList.add("palestina");
        blackList.add("españa");

        for (String element : blackList){
            if (joke.toLowerCase().contains(element)){
                blackJoke = true;
            }
        }
        return blackJoke;
    }
}
