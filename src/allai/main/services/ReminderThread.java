/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main.services;

import allai.interfaces.keys.KEYS;
import static allai.utils.ALLAILogger.logError;
import allai.utils.DoubleArrayList;
import allai.utils.HTTPUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class ReminderThread extends Thread {

    public static DoubleArrayList<Calendar, String[]> tasks = new DoubleArrayList<>();
    private DoubleArrayList<Calendar, String[]> completedTasks;

    @Override
    public void run() {
        completedTasks = new DoubleArrayList<>();
        while (true) {
            Calendar currentCalendar = Calendar.getInstance();
            long timeNow = currentCalendar.getTimeInMillis();
            for (int x = 0; x <tasks.size(); x++) {
                Calendar taskCalendar = tasks.getX(x);
                long taskTime = taskCalendar.getTimeInMillis();
                if (timeNow >= taskTime){
                    sendReminder(tasks.getY(x));
                    completedTasks.add(taskCalendar, tasks.getY(x));
                }
            }
            for (int x=0; x<completedTasks.size(); x++){
                tasks.remove(completedTasks.getX(x), completedTasks.getY(x));
            }
            completedTasks.clear();
            try {
                Thread.sleep(1000*30);
            } catch (InterruptedException ex) {
                logError("ReminderThread: Interrupted: " + ex.getMessage());
            }
        }
    }
    
    private void sendReminder(String[] reminderInfo) {
        String token = KEYS.telegramBotToken;
        String chatId = reminderInfo[0];
        String text = "Hola! He venido a recordarte esto, como me pediste: " + reminderInfo[1];
        String encoded = "";
        logInfo("ReminderThread: Sending a reminder for user " + chatId + " with content: " + text);
        try {
            encoded = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logError("ReminderThread: UnsuportedEncodingException? It is impossible that this exception raises: " + ex.getMessage());
        }
        String urlStr = "https://api.telegram.org/bot" + token + "/sendMessage?chat_id=" + chatId + "&amp&text=" + encoded;
        try {
            HTTPUtils.doGet(urlStr);
        } catch (Exception ex) {
            logError("ReminderThread: An error occurred while trying to send a reminder message: " + ex.getMessage());
        }
    }

}
