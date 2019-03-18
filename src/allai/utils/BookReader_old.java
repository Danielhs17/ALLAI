/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.utils;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class BookReader_old {
    
    public static final String NO_NEXT = "NO_NEXT";
    private String file;
    private Scanner reader;
    private BufferedReader bufReader;
    
    public BookReader_old(String filename){
        file = filename;
        read();
    }
    
    //Read file
    private void read(){
        bufReader = FileManager.readFromFile(file);
        reader = new Scanner(bufReader);
        reader.useDelimiter("\\.");
    }
    
    /*** Reads a phrase from the book until it finds a dot.
     * @return An ArrayList containing the words of the read phrase. If there is no next phrase, the first member of the array will equal NO_NEXT ***/
    public ArrayList<String> nextPhrase(){
        ArrayList<String> wordsArray = new ArrayList<String>();
        String phrase = readUntilDot();
        if (!phrase.equals(NO_NEXT)){
            if (phrase.startsWith(" ")){
                phrase = phrase.substring(1);
            }
            phrase = deleteSpecialChars(phrase);
            String words[] = phrase.split(" ");
            for (int i=0; i<words.length; i++){
                wordsArray.add(words[i]);
            }
        }else{
            wordsArray.add(NO_NEXT);
        }
        return wordsArray;
    }
    
    private String deleteSpecialChars(String input){
        String alphaOnly = input.replaceAll("[^a-zA-ZóéíáúñÁÉÍÓÚÑ ]+","");
        return alphaOnly;
    }
    
    private String readUntilDot(){
        String line = null;
        if (reader.hasNext()){
            line = reader.next();
            
        }else{
            line = NO_NEXT;
        }
        
        return line;
    }
}
