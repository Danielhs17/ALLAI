/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main.utils;

import java.util.ArrayList;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class WordContextInfo {
    public String word;
    public String prevWord;
    public String nextWord;
    public boolean isFirst = false;
    public boolean isLast = false;
    public WordContextInfo(ArrayList<String> phrase, int position){
        if(position == 0){
            isFirst = true;
            prevWord = null;
        }
        if (position == phrase.size()-1){
            isLast = true;
            nextWord = null;
        }
        word = phrase.get(position).toLowerCase();
        if (!isFirst){
            prevWord = phrase.get(position-1).toLowerCase();
        }
        if (!isLast){
            nextWord = phrase.get(position+1).toLowerCase();
        }
    }
    
    @Override
    public String toString(){
        return "{WordContextInfo Object: [WORD = " + word + "] " + "[prevWord = " + prevWord + "] " + "[nextWord = " + nextWord + "] " + "[IsFirst = " + isFirst + "] " + "[isLast = " + isLast + "]}";
    }
}
