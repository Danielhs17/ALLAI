package allai.utils;

import static allai.utils.ALLAILogger.logError;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class HTTPUtils {
    URL url;
    Map<String,String> header;
    byte[] postData;

    int responseCode;

    public HTTPUtils (String url, String urlParameter, Map<String,String> header, byte[] postData){
        try{
            //Anche se <url> finisce già con un "?" non importa, un doppio "??" viene considerato come uno solo
            this.url= new URL(url+"?"+urlParameter);
        }catch (MalformedURLException ex){
            logError("HTTPUtils: An error occured while initializating the URL: " + ex.getMessage());
        }
        this.header = header;
        this.postData = postData;
    }

    public String doPost(){
        try{
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setDoOutput( true );
            con.setInstanceFollowRedirects( false );
            con.setRequestMethod("POST");
            for (Map.Entry<String, String> entry : header.entrySet())
                con.setRequestProperty(entry.getKey(),entry.getValue());
            con.setUseCaches( false );
            try( DataOutputStream wr = new DataOutputStream( con.getOutputStream())) {
                wr.write(postData);
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            String res = "";
            while ((line = rd.readLine()) != null)
                res += line;
            responseCode = con.getResponseCode();
            return res;

        }catch(IOException ex){
            logError("HTTPUtils: An error occured during a POST request: " + ex.getMessage());
            responseCode= -1;
            return "";
        }
    }

    public int getResponseCode(){
        return responseCode;
    }
    
    public static String doGet(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "charset=utf-8");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }
}
