package allai.main.utils;

//Edmond Duke
public class SpanishSyllablesSeparator {

    private String sentence;

    public SpanishSyllablesSeparator(){
        sentence = "";
    }

    public void setString(String newSentence){
        sentence = newSentence;
    }
    
    private static int letter(char c){
        int i = -1;
        int ascii;
        ascii = (int)c;
        if(ascii!=-1){
            switch(ascii){
                case 97: //a
                    i = 1;
                    break;
                case 101: //e
                    i = 2;
                    break;
                case 104: //h
                    i = 6;
                    break;
                case 105: //i
                    i = 4;
                    break;
                case 111: //o
                    i = 3;
                    break;
                case 117: //u
                    i = 5;
                    break;
                case 225: //á
                    i = 1;
                    break;
                case 233: //é
                    i = 2;
                    break;
                case 237: //í
                    i = 4;
                    break;
                case  243: //ó
                    i = 3;
                    break;
                case 250: //ú
                    i = 5;
                    break;
                case 252: //ü
                    i = 5;
                    break;
                default:
                    i = 19;
                    break;
            }
        }
        return i;
    }

    private static String spanishSyllable(String str){
        String temp = "";
        String s="";
        char x,y,z;
        if(str.length()<3){
            if(str.length()==2){
                x = str.charAt(0);
                y = str.charAt(1);
                if ( letter(x)<6 && letter(y)<6){
                    if ( hiato(x,y))
                        s = str.substring(0,1);
                    else
                        s = str;
                }else
                    s = str;
            }else
                s = str;
        }else{
            x = str.charAt(0);
            y = str.charAt(1);
            z = str.charAt(2);
            if(letter(x)<6){ //V ? ?
                if(letter(y)<6){  //V V ?
                    if(letter(z)<6){  //V V V
                        if(hiato(x,y)){
                            s = str.substring(0, 1);
                        }else{
                            if(hiato(y,z)){
                                s = str.substring(0,2);
                            }else{
                                s = str.substring(0,3);
                            }
                        }
                    }else{ // V V C
                        if(hiato(x,y)){
                            s = str.substring(0,1);
                        }else{
                            s = str.substring(0,2);
                        }
                    }
                }else{ // V C ?
                    if(letter(z)<6){  //V C V
                        if(letter(y)==6){ // V H C
                            if(hiato(x,z)){
                                s = str.substring(0,1);
                            }else{
                                s = str.substring(0,3);
                            }
                        }else{
                            s = str.substring(0,1);
                        }
                    }else{ // V C C
                            if(consonants(y,z)){
                                s = str.substring(0,1);
                            }else{
                                s = str.substring(0,2);
                            }
                    }
                }
            }else{ // C ??
                if(letter(y)<6){ //C V ?
                    if(letter(z)<6){ // C V V
                        temp = str.substring(0,3);
                        if(temp.equals("que") || temp.equals("qui") || temp.equals("gue") || temp.equals("gui")){
                            s = str.substring(0, 3);
                        }else{
                            if(hiato(y,z)){
                                s = str.substring(0, 2);
                            }else{
                                s = str.substring(0, 3);
                            }
                        }
                    }else{ // C V C
                        s = str.substring(0,2);
                    }
                }else{ // C C ?
                    if(letter(z)<6){ // C C V
                        if(consonants(x,y)){
                            s = str.substring(0,3);
                        }else{
                            s = str.substring(0,1);
                        }
                    }else{ // C C C
                        if(consonants(y,z)){
                            s = str.substring(0,1);
                        }else{
                            s = str.substring(0,1);
                        }
                    }
                }
            }
        }
        return s;
    }

    private static String silabaRest(String str){
        String s2;
        s2 = spanishSyllable(str);
        return str.substring(s2.length());
    }

    private static boolean hiato(char v,char v2){
        boolean cer=false;
        if(letter(v)<4){ // VA + ?
            if(letter(v2)<4) //VA + VA
                cer = true;
            else{ //VA+ VC
                if(v2=='í' || v2=='ú'){
                    cer = true;
                }else{
                    cer = false;
                }
            }
        }else{ // VC + ?
            if(letter(v2)<4){ // VC + VA
                if(v=='í' || v=='ú'){
                    cer = true;
                }else{
                    cer = false;
                }
            }else{//VC + VC
               if(v==v2){
                   cer = true;
               }else
                   cer = false;
            }
        }
        return cer;
    }

    private static boolean consonants(char a, char b){
        boolean cer;
        cer = false;
        if (a == 'b' || a == 'c' || a == 'd' || a == 'f' || a == 'g' || a == 'p' || a == 'r' || a == 't'){
            if(b=='r'){
                cer = true;
            }
        }
        if (a == 'b' || a == 'c' || a == 'f' || a == 'g' || a == 'p' || a == 't' || a =='l'){
            if(b=='l'){
                cer = true;
            }
        }
        if(b=='h'){
            if(a=='c'){
                cer = true;
            }
        }
        return cer;
    }

    private static boolean consonantsString(String str){
        boolean cer;
        int i,k;
        char c[];
        cer = false;
        k=0;
        c = str.toCharArray();
        for(i=0;i<str.length();i++){
            if(letter(c[i])>5){
                k = k + 1;
            }
        }
        if(k==str.length()){
            cer = true;
        }
        return cer;
    }

    private static boolean strVVstr(String s1, String s2){
        boolean cer;
        char c1,c2;
        c1 = s1.charAt(s1.length()-1);
        c2 = s2.charAt(0);
        cer = false;
        if(letter(c1)<6 && letter(c2)<6){
            if(hiato(c1,c2)){
                cer = false;
            }else{
                cer = true;
            }
        }
        return cer;
    }

    public String splitBySyllables(){
        String temp;
        String s = "";
        int i,k;
        k = sentence.length();
        for(i=0;i<k;i++){
            temp = spanishSyllable(sentence);
            if(i==0){
                s = s + temp;
            }else{
                if(consonantsString(temp)){
                    s = s +temp;
                }else{
                    if(strVVstr(s,temp)){
                        s = s + temp;
                    }else{
                        if(consonantsString(s)){
                            s = s + temp;
                        }else{
                            s = s + "-" +temp;
                        }
                    }
                }
            }
            i = i + temp.length()-1;
            sentence = silabaRest(sentence);
        }
        return s;
    }
}
