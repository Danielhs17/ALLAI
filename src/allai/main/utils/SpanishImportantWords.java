/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main.utils;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class SpanishImportantWords {
    
    //Receives the number of syllables of a word, and return it's importance index
    private static double getImportance(int number){
        double a = (double)number/3;
        double importance;
        if (a > 1){
            importance = 1/a;
        }else{
            importance = a;
        }
        return importance-1;
    }
    
    //Receives a sentence and returns the number of syllables in each word
    private static int[] getSyllablesCount(String sentence){
         SpanishSyllablesSeparator splitter = new SpanishSyllablesSeparator();
        //String separated = separa.silabear();
        String[] words = sentence.split(" ");
        int[] syllables = new int[words.length];
        for (int x=0; x<words.length; x++){
            splitter.setString(words[x].toLowerCase());
            String separated = splitter.splitBySyllables();
            syllables[x] = separated.split("-").length;
        }
        
        return syllables;
    }
    
    //Returns the most important word of a sentence (by syllable count)
    //Returns "" in case of error
    public static String getMostImportantWord(String sentence){
        int[] syllables = getSyllablesCount(sentence);
        double[] importance = new double[syllables.length];
        for (int x=0; x<syllables.length; x++){
            importance[x] = getImportance(syllables[x]);
        }
        int higherIndex = getHigherIndex(importance);
        if (higherIndex != -1){
            return sentence.split(" ")[higherIndex];
        }else{
            return "";
        }
    }

    //Returns the index of the higher numner in a doubles array
    private static int getHigherIndex(double[] numbers) {
        if (numbers.length == 0 ){
            return -1;
        }
        double higher = numbers[0];
        int index = 0;
        for (int x=1; x<numbers.length; x++){
            if (numbers[x] > higher){
                higher = numbers[x];
                index = x;
            }
        }
        return index;
    }
    
}
