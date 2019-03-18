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
 * @author Daniel Alejandro Hurtado Simoes Universidad de Málaga TFG - Grado en
 * Ingeniería Telemática
 */
public class BookReader {

    public static final String NO_NEXT = "NO_NEXT";
    public static final String SKIP = "_SKIP";
    private String file;
    private Scanner reader;
    private BufferedReader bufReader;

    public BookReader(String filename) {
        file = filename;
        read();
    }

    //Read file
    private void read() {
        bufReader = FileManager.readFromFile(file);
        reader = new Scanner(bufReader);
        reader.useDelimiter("\\.|\\n|\\?|\\!|—");
    }

    /**
     * * Reads a phrase from the book until it finds a dot.
     *
     * @return An ArrayList containing the words of the read phrase. If there is
     * no next phrase, the first member of the array will equal NO_NEXT **
     */
    public ArrayList<String> nextPhrase() {
        ArrayList<String> wordsArray = new ArrayList<String>();
        String phrase = readUntilDot();
        if (!phrase.equals(NO_NEXT)) {
            if (phrase.startsWith(" ")){
                phrase = phrase.substring(1);
            }
            //System.out.println("Phrase: " + phrase);
            phrase = deleteSpecialChars(phrase);
//            System.out.println("Phrase: " + phrase);
            String words[] = phrase.split(" ");
            for (int i = 0; i < words.length; i++) {
                if (!words[i].isEmpty()) {
                    wordsArray.add(words[i]);
                }
            }
        } else {
            wordsArray.add(NO_NEXT);
        }
        if (wordsArray.isEmpty()){
            wordsArray.add(SKIP);
        }
//        System.out.print("Phrase: ");
//        for (String word : wordsArray){
//            System.out.print(word + " ");
//        }
//        System.out.println("");
         return wordsArray;
    }

    private String deleteSpecialChars(String input) {
        String alphaOnly = input.replaceAll("[^a-zA-ZóéíáúñÁÉÍÓÚÑ ]+", "");
        return alphaOnly;
    }

    private String readUntilDot() {
        String line = null;
        if (reader.hasNext()) {
            line = reader.next();
            while ((line.isEmpty() || line.equals("")) && reader.hasNext()){
                //System.out.println("PILLAO");
                line = reader.next();
            }
        } else {
            line = NO_NEXT;
        }

        return line;
    }
}
