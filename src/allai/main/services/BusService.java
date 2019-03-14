/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main.services;

import allai.utils.JSONReader;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONObject;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes Universidad de Málaga TFG - Grado en
 * Ingeniería Telemática
 */
public class BusService extends Service {

    private String parada;
    private String linea;
    private String sentido;

    private enum Service {
        LINEA, PARADA, ERROR
    }
    private Service service;

    private String error = "[X] Argumentos erróneos. Por favor, vuélvelo a intentar con alguno de estos formatos: [/bus parada #PARADA] o [/bus linea #LINEA] (Ejemplo: /bus parada 1553) (Ejemplo2: /bus linea 15)";
    private String emtParadaURL = "http://emtmalaga.es/emt-core/services/esperas/?codParada=";
    private String emtLineaURL = "http://www.emtmalaga.es/emt-core/services/buses/?codLinea=";
    private String emtInfoLineas = "http://www.emtmalaga.es/emt-core/services/lineas/";

    public BusService(String[] arg) {
        logInfo("BusService: Initiated");
        int arguments = arg.length;
        if (arguments != 3) {
            service = Service.ERROR;
        } else {
            if (validatePARADA(arg)) {
                service = Service.PARADA;
                parada = arg[2];
            } else if (validateLINEA(arg)) {
                service = Service.LINEA;
                linea = interpretarNombreLinea(arg[2]);
            } else {
                service = Service.ERROR;
            }
        }
    }

    private boolean validatePARADA(String[] arg) {
        boolean validStopFormat = true;
        try {
            Integer.parseUnsignedInt(arg[2]);
        } catch (Exception e) {
            validStopFormat = false;
        }
        if (arg[1].equals("parada") && validStopFormat) {
            return true;
        } else {
            return false;
        }
    }

    private boolean validateLINEA(String[] arg) {
        if (arg[1].equals("linea")) {
            return true;
        } else {
            return false;
        }
    }

    public String getStopInfo() {
        ArrayList<JSONObject> json;
        String errorParada = "No he podido conseguir información sobre esta parada. ¿Estás seguro de que existe?";
        try {
            json = JSONReader.readJsonArrayFromUrl(emtParadaURL + parada);
        } catch (IOException e) {
            return errorParada;
        }
        if (json.isEmpty()) {
            return errorParada;
        }
        String response = "Aquí tienes los minutos que faltan para cada autobús:\n";
        for (int x = 0; x < json.size(); x++) {
            JSONObject object = json.get(x);
            String espera = object.get("espera") + " min.";
            response += "Linea " + interpretarCodigoLinea(object.get("codLinea").toString()) + " -> " + ((espera.startsWith("null")) ? "No disponible" : espera) + "\n";
        }
        return response;
    }

    private String getLineInfo() {
        ArrayList<JSONObject> json, jsonInfo;
        String errorLinea = "No he podido conseguir información sobre esta línea. Es probable que no haya ningun autobús de esa línea operativo a esta hora. Eso, ¡o esa línea no existe!";
        try {
            json = JSONReader.readJsonArrayFromUrl(emtLineaURL + linea);
            jsonInfo = JSONReader.readJsonArrayFromUrl(emtInfoLineas);
        } catch (IOException e) {
            return errorLinea;
        }
        if (json.isEmpty()) {
            return errorLinea;
        }
        int sentido1 = 0;
        int sentido2 = 0;
        for (int x = 0; x < json.size(); x++) {
            JSONObject object = json.get(x);
            int sentido = (int) object.get("sentido");
            if (sentido == 1) {
                sentido1++;
            } else {
                sentido2++;
            }
        }
        String nombreS1 = "";
        String nombreS2 = "";
        float codLineaFl = Float.parseFloat(linea);
        for (int x = 0; x < jsonInfo.size(); x++) {
            JSONObject object = jsonInfo.get(x);
            float codLineaObj = object.getFloat("codLinea");
            if(codLineaFl == codLineaObj){
                nombreS1 = object.getString("cabeceraIda");
                nombreS2 = object.getString("cabeceraVuelta");
            }
        }
        return "Ahora mismo hay en total " + json.size() + " autobuses de esta línea circulando, " + sentido1 + " de ellos en dirección " + nombreS1 + " y " + sentido2 + " en dirección " + nombreS2 + ".";
    }

    @Override
    public String getResponse() {
        String response = error;
        switch (service) {
            case LINEA:
                response = getLineInfo();
                break;
            case PARADA:
                response = getStopInfo();
                break;
            case ERROR:
                response = error;
                break;
        }
        return response;
    }

    private String interpretarCodigoLinea(String parada) {
        String[][] parejas = {{"75", "A"}, {"71", "C1"}, {"72", "C2"}, {"73", "C3"}, {"76", "C6"}, {"70", "E"}, {"65", "L"}, {"41", "N1"}, {"42", "N2"}, {"43", "N3"}, {"44", "N4"}, {"45", "N5"}};
        String paradaEstilizada = "" + ((int) Float.parseFloat(parada));
        for (int x = 0; x < 12; x++) {
            if (paradaEstilizada.equals(parejas[x][0])) {
                paradaEstilizada = parejas[x][1];
            }
        }
        return paradaEstilizada;
    }

    private String interpretarNombreLinea(String parada) {
        String[][] parejas = {{"75", "A"}, {"71", "C1"}, {"72", "C2"}, {"73", "C3"}, {"76", "C6"}, {"70", "E"}, {"65", "L"}, {"41", "N1"}, {"42", "N2"}, {"43", "N3"}, {"44", "N4"}, {"45", "N5"}};
        String codLinea = parada.toUpperCase();
        for (int x = 0; x < 12; x++) {
            if (codLinea.equals(parejas[x][1])) {
                codLinea = parejas[x][0];
            }
        }
        return codLinea;
    }

}
