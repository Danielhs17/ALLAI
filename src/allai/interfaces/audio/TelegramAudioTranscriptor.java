package allai.interfaces.audio;

import allai.interfaces.keys.KEYS;
import static allai.utils.ALLAILogger.logError;
import static allai.utils.ALLAILogger.logInfo;
import java.io.BufferedReader;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Voice;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class TelegramAudioTranscriptor {

    private String url;
    private Message message;
    private JSONParser parser;
    private String transcribedText;

    public TelegramAudioTranscriptor(Message msg, String telegramUrl) throws IOException {
        url = telegramUrl;
        message = msg;
        parser = new JSONParser();
        initializeAndTranscript();
    }

    private void initializeAndTranscript() {
        Voice voice = message.getVoice();
        if (voice != null) {
            if (!downloadAndConvert(voice)) {
                return;
            }
            try {
                transcribedText = transcript(voice);
            } catch (IOException e) {
                logError("TelegramAudioTranscriptor: Exception while transcripting: " + e.getMessage());
            }
        }
    }

    private boolean downloadAndConvert(Voice voice) {
        logInfo("TelegramAudioTranscriptor: Downloading and converting audio");
        JSONObject filePath;
        URL fileToGet;
        ReadableByteChannel rbc;
        String file_id = voice.getFileId();
        try {
            filePath = (JSONObject) callJSON(new URL(url + "getFile?file_id=" + file_id)).get("result");
            fileToGet = new URL("https://api.telegram.org/file/bot" + KEYS.telegramBotToken + "/" + filePath.get("file_path"));
            rbc = Channels.newChannel(fileToGet.openStream());
        } catch (MalformedURLException e) {
            logError("TelegramAudioTranscriptor: MalformedURLException when trying to download telegram audio" + e.getMessage());
            return false;
        } catch (IOException e) {
            logError("TelegramAudioTranscriptor: IOException at downloadAndConvert: " + e.getMessage());
            return false;
        }
        FileOutputStream fos;
        try {
            fos = new FileOutputStream("audio/" + file_id + ".oga");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
        } catch (IOException e) {
            logError("TelegramAudioTranscriptor: Error occured while writing saving .oga file: " + e.getMessage());
        }
        try {
            logInfo("TelegramAudioTranscriptor: Converting audio with opusdec..");
            Process p = Runtime.getRuntime().exec("opusdec --rate 16000 " + "audio/" + file_id + ".oga" + " " + "audio/" + file_id + ".wav");
            p.waitFor();
            logInfo("TelegramAudioTranscriptor: Audio converted");
            return true;
        } catch (InterruptedException e) {
            logError("TelegramAudioTranscriptor: Opusdec execution was interrupted " + e.getMessage());
            return false;
        } catch (IOException e) {
            logError("TelegramAudioTranscriptor: Error during opusdec execution" + e.getMessage());
            return false;
        }
    }

    private String transcript(Voice voice) throws IOException {
        String file_id = voice.getFileId();
        WitAiTranscriptor trasc = new WitAiTranscriptor();
        String text = "";
        try {
            logInfo("TelegramAudioTranscriptor: Trying to transcript audio with WIT");
            trasc.transcript("audio/" + file_id + ".wav");
            if (trasc.getText().equals("")) {
                text = "";
            } else {
                text += trasc.getText();
            }
        } catch (NullPointerException e) {
            logError("TelegramAudioTranscriptor: NullPointerException while transcripting audio: " + e.getMessage());
            text = "Error interno del servidor.";
        }
        new File("audio/" + file_id + ".oga").delete();
        new File("audio/" + file_id + ".wav").delete();
        return text;
    }

    private String callString(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String res = "";
        BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
        String nline;
        while ((nline = bf.readLine()) != null) {
            res += nline;
        }
        return res;
    }

    private JSONObject callJSON(URL url) throws IOException {
        try {
            return (JSONObject) parser.parse(callString(url));
        } catch (ParseException e) {
            logError("TelegramAudioTranscriptor: ParseException when retrieving JSON from " + url + ": " + e.getMessage());
            return null;
        }
    }

    public String getTranscribedText() {
        return transcribedText;
    }
}
