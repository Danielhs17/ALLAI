/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main.services;

import allai.utils.CSVReader;
import static allai.utils.ALLAILogger.logInfo;


/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class ParkingService extends Service {
    
    private String CSVAparcamientos = "http://datosabiertos.malaga.eu/recursos/aparcamientos/ocupappublicosmun/ocupappublicosmun.csv";
    private String error = "[X] Lo siento, no he podido procesar tu pedido. Quizás has añadido palabras de más. Intenta con /parking";
    
    private enum Service {
        PARKING, ERROR
    }
    private Service service;
    
    public ParkingService(String[] arg) {
        logInfo("ParkingService: Initiated");
        int arguments = arg.length;
        if (arguments != 1) {
            service = Service.ERROR;
        } else {
            service = Service.PARKING;
        }
    }
    
    private String getParkingLots(){
        CSVReader parkingReader = new CSVReader(CSVAparcamientos, ",");
        String[] line, header;
        String response = "Esta es la ocupación actual de los aparcamientos públicos de Málaga:\n";
        header = parkingReader.getNextLine();
        line = parkingReader.getNextLine();
        while (line != null) {
            if (line[11].equals("-1")) {
                line[11] = "Desconocido";
            }
            if (line[8].equals("-1")) {
                line[8] = "Desconocido";
            }
            response += line[1] + ": " + line[11] + "/" + line[8] + "\n";
            line = parkingReader.getNextLine();
        }
        parkingReader.closeFile();
        return response;
    }

    @Override
    public String getResponse() {
        String response = error;
        switch (service) {
            case PARKING:
                response = getParkingLots();
                break;
            case ERROR:
                response = error;
                break;
        }
        return response;
    }
}
