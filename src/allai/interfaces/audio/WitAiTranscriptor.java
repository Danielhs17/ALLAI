package allai.interfaces.audio;

import allai.interfaces.keys.KEYS;
import static allai.utils.ALLAILogger.logError;
import allai.utils.HTTPUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class WitAiTranscriptor {
    JSONObject transcription;

    /*** Receives a String with the name of an audio file and transcribes it to text using Wit.Ai transcription services.
     * @param fileName: The name of the audio file to be transcribed.
     * @return The response received from Wit.Ai server. ***/
    public String transcript(String fileName) throws IOException {
        try {
            String charset = StandardCharsets.UTF_8.name();
            String url = "https://api.wit.ai/speech";
            String version = "20141022";
            String params = String.format("v=%s", URLEncoder.encode(version, charset));
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + KEYS.WitAPIKey);
            headers.put("Content-Type", "audio/wav");
            HTTPUtils post = new HTTPUtils(url, params, headers, Files.readAllBytes(Paths.get(fileName)));
            String res = post.doPost();
            JSONParser p = new JSONParser();
            if (!res.equals(""))
                this.transcription = (JSONObject) p.parse(res);
            return res;
        } catch (ParseException ex) {
            logError("WitAiTranscriptor: Error while parsing JSON Object" + ex.getMessage());
            return "";
        }
    }

    /*** Recover the transcribed text after using transcript() method.
     * @return The transcribed text from the audio file. ***/
    public String getText() {
        if(transcription == null){
            return "";
        }else{
            String text = (String) this.transcription.get("_text");
            if(text == null)
                 return "";
            return text;
        }
    }
}
