/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main.services;

import java.util.Calendar;
import java.util.GregorianCalendar;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class ReminderService extends Service {

    private enum Service {
        REMIND, WRONGARGUMENTS
    }
    private Service service;

    private String error = "No he podido entender lo que quieres recordar, o cuándo, por favor escríbelo así: \"/recordar [DD-MM-AA] [hh:mm] [Mensaje]\"\n"
            + "Por favor, escribe la hora en formato de 24 horas!";
    private String chatId;
    private String msg = "";
    private int day;
    private int month;
    private int year;
    private int hour;
    private int minute;
    private int second = 0;

    // /recordar [DD-MM-AAAA] [hh:mm] [Mensaje]
    // /recordar eliminar [DD-MM-AAAA] [hh:mm] [Mensaje] NO IMPLEMENTADO AUN
    public ReminderService(String[] arg, long chatIdr) {
        logInfo("ReminderService: Initiated");
        chatId = chatIdr + "";
        int arguments = arg.length;
        if (arguments < 4) {
            service = Service.WRONGARGUMENTS;
        } else if (validateArguments(arg)) {
            arg[1] = arg[1].replace("/", "-");
            service = Service.REMIND;
            day = Integer.parseInt(arg[1].split("-")[0]);
            month = Integer.parseInt(arg[1].split("-")[1]);
            year = Integer.parseInt(arg[1].split("-")[2]);
            hour = Integer.parseInt(arg[2].split(":")[0]);
            minute = Integer.parseInt(arg[2].split(":")[1]);
            for (int x = 3; x < arguments; x++) {
                String temp = arg[x];
                msg += temp.substring(0, 1).toUpperCase() + temp.substring(1) + " ";
            }
            msg = msg.substring(0, msg.length() - 1);
        } else {
            service = Service.WRONGARGUMENTS;
        }
    }

    // Sintaxis correcta: /recordar [DD-MM-AAAA] [hh:mm] [Mensaje]
    private boolean validateArguments(String[] arg) {
        // Fecha:
        arg[1] = arg[1].replace("/", "-");
        String[] date = arg[1].split("-");
        if (date.length != 3) {
            return false;
        }
        try {
            int day = Integer.parseInt(date[0]);
            int month = Integer.parseInt(date[1]);
            int year = Integer.parseInt(date[2]);
            if (day < 1 || day > 31 || month < 1 || month > 12 || year < 2019) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        //Hora:
        String[] time = arg[2].split(":");
        if (time.length != 2) {
            return false;
        }
        try {
            int hour = Integer.parseInt(time[0]);
            int min = Integer.parseInt(time[1]);
            if (hour < 0 || hour > 23 || min < 0 || min > 59) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public String getResponse() {
        String response = error;
        switch (service) {
            case REMIND:
                response = setReminder();
                break;
            case WRONGARGUMENTS:
                response = error;
                break;
        }
        return response;
    }

    private String setReminder() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.ERA, GregorianCalendar.AD);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        // VERIFICAR QUE SEA DESPUES DE HOY
        String[] reminderInfo = {chatId, msg};
        ReminderThread.tasks.add(calendar, reminderInfo);
        return "Perfecto! Te lo recordaré!";
    }
}
