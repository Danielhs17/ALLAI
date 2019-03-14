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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class WeatherService extends Service {

    private final String APIKEY = KEYS.openWeatherApiKey;
    private final String malagaCityId = "2514256";
    private final String url = "http://api.openweathermap.org/data/2.5/weather?id=" + malagaCityId + "&APPID=" + APIKEY;
    private final String alternativeURL = "http://api.openweathermap.org/data/2.5/weather?q=";

    //api.openweathermap.org/data/2.5/weather?q={city name}
    private enum Service {
        WEATHER, ERROR
    }
    private Service service;
    private String error = "Te has complicado mucho! Este comando no lleva argumentos, simplemente escribe /clima";
    private boolean alternative = false;
    private String clouds;
    private String temperature;
    private String humidity;
    private String wind;
    private String city = "Málaga, ES";

    public WeatherService(String[] arg) {
        logInfo("WeatherService: Initiated");
        int arguments = arg.length;
        if (arguments > 2) {
            service = Service.ERROR;
        } else {
            if (arguments == 2){
                alternative = true;
                city = arg[1].toLowerCase().replaceAll("[^A-Za-z0-9]", "");;
            }
            service = Service.WEATHER;
        }
    }

    @Override
    public String getResponse() {
        String response = error;
        switch (service) {
            case WEATHER:
                response = getWeather();
                break;
            case ERROR:
                response = error;
                break;
        }
        return response;
    }

    private String getWeather() {
        String response;
        String usedUrl;
        if (alternative){
            usedUrl = alternativeURL + city + "&APPID=" + APIKEY;
        } else {
            usedUrl = url;
        }
        try {
            String getResult = HTTPUtils.doGet(usedUrl);
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(getResult);
            JSONObject main = (JSONObject) object.get("main");
            double tempF = (double) main.get("temp");
            temperature = truncateDecimal(kelvinToCelsius(tempF),2) + " °C";
            humidity = main.get("humidity") + "%";
            JSONObject windObject = (JSONObject) object.get("wind");
            double speed = toKMH((double) windObject.get("speed"));
            wind = truncateDecimal(speed, 2) + " kmh";
            JSONObject cloudsObject = (JSONObject) object.get("clouds");
            long cloudsPercentage = (long) cloudsObject.get("all");
            if (cloudsPercentage >= 0 && cloudsPercentage <= 5) {
                clouds = "Despejado";
            } else if (cloudsPercentage >= 5 && cloudsPercentage <= 10) {
                clouds = "Parcialmente Despejado";
            } else if (cloudsPercentage >= 10 && cloudsPercentage <= 20) {
                clouds = "Ligeramente Nublado";
            } else if (cloudsPercentage >= 20 && cloudsPercentage <= 35) {
                clouds = "Parcialmente Nublado";
            } else if (cloudsPercentage >= 35) {
                clouds = "Nublado";
            }
            response = "Clima en " + city + ":\n"
                    + "Temperatura: " + temperature + "\n"
                    + "Humedad: " + humidity + "\n"
                    + "Viento: " + wind + "\n"
                    + "Cielo: " + clouds;

        } catch (Exception ex) {
            logError("WeatherService: An error occurred while trying to retrieve the weather: " + ex.getMessage());
            response = "No he podido obtener información del clima ahora mismo, ¡creo que se ha roto mi termómetro! ¡Lo siento!";
        }
        return response;
    }

    private static double kelvinToCelsius(double f) {
        return f-273.15;
    }

    private double toKMH(double mph) {
        return mph * 1.609;
    }

    private static BigDecimal truncateDecimal(double x, int numberofDecimals) {
        if (x > 0) {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR);
        } else {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING);
        }
    }
}
