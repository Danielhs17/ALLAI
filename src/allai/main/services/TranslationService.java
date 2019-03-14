/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main.services;

import allai.interfaces.keys.KEYS;
import static allai.utils.ALLAILogger.logError;
import allai.utils.HTTPUtils;
import allai.utils.XMLParser;
import java.io.*;
import java.net.*;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class TranslationService extends Service {

    final String KEY = KEYS.yandexApiKey;
    String errorEx = "Lo siento, no se cómo traducir eso :(";
    String error = "Lo siento, no he entendido qué quieres traducir, ¡asegúrate de decirme el idioma y después el texto!";
    String wrongLanguage = "Lo siento, ¡no sé que idioma es ese! ¿Estás seguro de que lo escribiste bien?";
    String toLanguage = "";
    String text = "";

    private enum Service {
        TRANSLATE, ERROR, WRONGLANGUAGE
    }
    private Service service;

    public TranslationService(String[] arg) {
        logInfo("TranslationService: Initiated");
        int arguments = arg.length;
        if (arguments < 3) {
            service = Service.ERROR;
        } else {
            service = Service.TRANSLATE;
            try {
                toLanguage = getLanguage(arg[1]);
            } catch (Exception e){
                service = Service.WRONGLANGUAGE;
            }
            for (int x = 2; x < arguments; x++) {
                text += arg[x] + " ";
            }
        }
        text = text.substring(0, text.length() - 1);
    }

    private String getTranslation() {
        String response;
        String urlEncodedText = "";
        try {
            urlEncodedText = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logError("TranslationService: UnsuportedEncodingException? It is impossible that this exception raises: " + ex.getMessage());
        }
        String url = "https://translate.yandex.net/api/v1.5/tr/translate?key=" + KEY + "&lang=" + toLanguage + "&text=" + urlEncodedText;
        try {
            String getResult = HTTPUtils.doGet(url);
            XMLParser parser = new XMLParser(getResult);
            response = parser.getFirst("text");
            if (response.isEmpty()) {
                response = errorEx;
            }
        } catch (Exception ex) {
            logError("TranslationService: An error occurred while trying to translate: " + ex.getMessage());
            response = errorEx;
        }
        return response;
    }

    @Override
    public String getResponse() {
        String response = error;
        switch (service) {
            case TRANSLATE:
                response = getTranslation();
                break;
            case WRONGLANGUAGE:
                response = wrongLanguage;
                break;
            case ERROR:
                response = error;
                break;
        }
        return response;
    }

    private String getLanguage(String string) throws Exception {
        String langCode = null;
        String userGiven = removeAccents(string).toLowerCase();
        boolean isAlreadyCode = false;
        String[][] languagesTable = {{"azeri","az"}, {"albanes","sq"}, {"amarico","am"}, {"ingles","en"}, {"arabe","ar"}, {"armenio","hy"}, {"afrikaans","af"}, {"vasco","eu"}, {"baskir","ba"}, {"bielorruso","be"}, {"bengali","bn"}, {"birmano","my"}, {"bulgaro","bg"}, {"bosnio","bs"}, {"gales","cy"}, {"hungaro","hu"}, {"vietnamita","vi"}, {"haitiano","ht"}, {"gallego","gl"}, {"holandes","nl"}, {"maridelascolinas","mrj"}, {"griego","el"}, {"georgiano","ka"}, {"gujarati","gu"}, {"danes","da"}, {"hebreo","he"}, {"yidis","yi"}, {"indonesio","id"}, {"irlandes","ga"}, {"italiano","it"}, {"islandes","is"}, {"espanol","es"}, {"kazajo","kk"}, {"canares","kn"}, {"catalan","ca"}, {"kirguis","ky"}, {"chino","zh"}, {"koreano","ko"}, {"xhosa","xh"}, {"camboyano","km"}, {"lao","lo"}, {"latin","la"}, {"leton","lv"}, {"lituano","lt"}, {"luxemburgues","lb"}, {"malagache","mg"}, {"malayo","ms"}, {"malabar","ml"}, {"maltes","mt"}, {"macedonio","mk"}, {"maori","mi"}, {"marathi","mr"}, {"maridelascolinas","mhr"}, {"mongol","mn"}, {"aleman","de"}, {"nepali","ne"}, {"noruego","no"}, {"panyabi","pa"}, {"papiamento","pap"}, {"persa","fa"}, {"polaco","pl"}, {"portugues","pt"}, {"rumano","ro"}, {"ruso","ru"}, {"cebuano","ceb"}, {"serbio","sr"}, {"cingales","si"}, {"eslovaco","sk"}, {"esloveno","sl"}, {"suajili","sw"}, {"sondanes","su"}, {"tajiko","tg"}, {"tailandes","th"}, {"tagalo","tl"}, {"tamil","ta"}, {"tartaro","tt"}, {"telugu","te"}, {"turco","tr"}, {"udmurto","udm"}, {"uzbeko","uz"}, {"ucraniano","uk"}, {"urdu","ur"}, {"finlandes","fi"}, {"frances","fr"}, {"hindi","hi"}, {"croata","hr"}, {"checo","cs"}, {"sueco","sv"}, {"escoces","gd"}, {"estonio","et"}, {"esperanto","eo"}, {"javanes","jv"}, {"japones","ja"}};
        int languagesCount = 93;
        for (int x=0; x<languagesCount; x++){
            if (userGiven.equals(languagesTable[x][0])){
                langCode = languagesTable[x][1];
            } else if (userGiven.equals(languagesTable[x][1])){
                isAlreadyCode = true;
            }
        }
        if (isAlreadyCode){
            return userGiven;
        } else if (langCode.isEmpty()){
            throw new Exception ("Language not supported");
        } else {
            return langCode;
        }
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

}
