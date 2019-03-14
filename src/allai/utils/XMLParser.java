/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.utils;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class XMLParser {
    //private String notFound = "NOTFOUND";
    private String xml;
    
    public XMLParser(String xml){
        this.xml = xml;
    }
    
    public String getFirst(String param) throws Exception {
        return getAll(param)[0];
    }
    
    public String[] getAll(String param) throws Exception {
        String[] results1 = xml.split("<" + param + ">");
        String[] results2;
        if (results1.length <= 1){
            throw new Exception("Parameter not found in this xml");
        }else{
            results2 = new String[results1.length-1];
            for (int x=1; x<results1.length; x++){
                results2[x-1] = results1[x].split("</" + param + ">")[0];
            }
        }
        return results2;
    }
}
