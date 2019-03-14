/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main;

import allai.main.utils.WordContextInfo;
import static allai.utils.ALLAILogger.logError;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import org.mapdb.*;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class Dictionary {

    private static final String MAIN_DB_FILE = "allai.db";
    private static final String WORDS_DB_FILE = "allaiWords.db";
    private static final String RESPONSES_DB_FILE = "allaiResponses.db";
    private static final String ISLASTWORD = "ISLASTWORD";
    private static final String ISFIRSTWORD = "ISFIRSTWORD";
    public static boolean keepDBOpen = false;
    private static DB db;
    private static DB wordsDB;
    private static DB responsesDB;
    private static int count = 0;
    public static boolean isDBOpen = false;

    /*** For a given database map, where keys are Strings and values are Integers, 
     * adds a new key-value pair with the given word as key, or sums one to the value if the word was already a key in the map.
     * @param map: The map where the word key will be added or updated. This map's keys must be Strings, and values must be Integers.
     * @param word: The word to add as key, or to add one to it's value.***/
    private static void addOrUpdateParameter(ConcurrentMap map, String word) {
        try {
            if (map.get(word) == null) {
                map.put(word, 1);
            } else {
                int result = (int) map.get(word);
                result++;
                map.put(word, result);
            }
        } catch (Exception e) {
            logError("Dictionary: Exception at addOrUpdateParameter. Word: " + word + " ." + e.getMessage());
        }
    }

    /*** Initializes all databases. This function is used when 'keepDBOpen'
     * parameter is set to true. Not closing the databases when several accesses
     * to it are needed avoids a lot of time wasted by opening and closing it
     * repeadetly. Remember to call function 'commitAndClose()' when you are
     * done using the database to avoid data corruption.***/
    public static void initializeDB() {
        try {
            db = DBMaker.fileDB(MAIN_DB_FILE).closeOnJvmShutdown().fileMmapEnableIfSupported().fileMmapPreclearDisable().make();
            wordsDB = DBMaker.fileDB(WORDS_DB_FILE).closeOnJvmShutdown().fileMmapEnableIfSupported().fileMmapPreclearDisable().make();
            responsesDB = DBMaker.fileDB(RESPONSES_DB_FILE).closeOnJvmShutdown().fileMmapEnableIfSupported().fileMmapPreclearDisable().make();
        } catch (Exception e) {
            db = DBMaker.fileDB(MAIN_DB_FILE).closeOnJvmShutdown().fileMmapEnableIfSupported().fileMmapPreclearDisable().checksumHeaderBypass().make();
            wordsDB = DBMaker.fileDB(WORDS_DB_FILE).closeOnJvmShutdown().fileMmapEnableIfSupported().fileMmapPreclearDisable().checksumHeaderBypass().make();
            responsesDB = DBMaker.fileDB(RESPONSES_DB_FILE).closeOnJvmShutdown().fileMmapEnableIfSupported().fileMmapPreclearDisable().checksumHeaderBypass().make();
        }
        db.getStore().fileLoad();
        wordsDB.getStore().fileLoad();
        responsesDB.getStore().fileLoad();
        isDBOpen = true;
        keepDBOpen = true;
    }

    /*** Perform a commit and a close operation on all databases. 
     * Call this function if you have called 'initializeDB()' previously.***/
    public static void commitAndCloseDB() {
        db.commit();
        db.close();
        wordsDB.commit();
        wordsDB.close();
        responsesDB.commit();
        responsesDB.close();
        keepDBOpen = false;
        isDBOpen = false;
    }

    /*** The dictionary stores information NOT by probability, but with counters.
     * For example, if a word has been found 3 times to the right of the other, the number 3 will be stored.**
     * @param word: A WordContextInfo object for the word to learn. ***/
    public static void updateDictionary(WordContextInfo word) {
        if (!word.word.equals("")) {
            if (!keepDBOpen) {
                db = DBMaker.fileDB(MAIN_DB_FILE).closeOnJvmShutdown().make();
                wordsDB = DBMaker.fileDB(WORDS_DB_FILE).closeOnJvmShutdown().make();
            }
            ConcurrentMap wordMap = wordsDB.hashMap("knownWords").keySerializer(Serializer.STRING).valueSerializer(Serializer.INTEGER).createOrOpen();
            addOrUpdateParameter(wordMap, word.word);
            ConcurrentMap map = db.hashMap(word.word).keySerializer(Serializer.STRING).valueSerializer(Serializer.INTEGER).createOrOpen();
            if (word.isFirst) {
                addOrUpdateParameter(map, ISFIRSTWORD);
                ConcurrentMap mapFW = wordsDB.hashMap("firstWordsMap").keySerializer(Serializer.STRING).valueSerializer(Serializer.INTEGER).createOrOpen();
                addOrUpdateParameter(mapFW, word.word);
                if (!keepDBOpen) {
                    wordsDB.commit();
                    wordsDB.close();
                }
            } else {
                ConcurrentMap mapPrev = db.hashMap(word.word + "prev").keySerializer(Serializer.STRING).valueSerializer(Serializer.INTEGER).createOrOpen();
                addOrUpdateParameter(mapPrev, word.prevWord);
            }
            if (word.isLast) {
                addOrUpdateParameter(map, ISLASTWORD);
            }
            if (!keepDBOpen) {
                db.commit();
                db.close();
            }
        }
    }

    /*** Adds the response to the given question to the database
     * @param question: The question to link the response to. This should be the last phrase given by ALLAI
     * @param response: The response given by the user to the last phrase ALLAI said. ***/
    public static void addAsResponse(String question, String response) {
        if (!keepDBOpen) {
            responsesDB = DBMaker.fileDB(RESPONSES_DB_FILE).closeOnJvmShutdown().fileMmapEnableIfSupported().fileMmapPreclearDisable().make();
            responsesDB.getStore().fileLoad();
        }
        ConcurrentMap responseMap = responsesDB.hashMap(question).keySerializer(Serializer.STRING).valueSerializer(Serializer.INTEGER).createOrOpen();
        addOrUpdateParameter(responseMap, response);
        if (!keepDBOpen) {
            responsesDB.commit();
            responsesDB.close();
        }
    }

    /*** @return A default response for the given question***/
    public static String getResponse(String question) {
        if (!keepDBOpen) {
            responsesDB = DBMaker.fileDB(RESPONSES_DB_FILE).closeOnJvmShutdown().fileMmapEnableIfSupported().fileMmapPreclearDisable().make();
            responsesDB.getStore().fileLoad();
        }
        ConcurrentMap responseMap = responsesDB.hashMap(question).keySerializer(Serializer.STRING).valueSerializer(Serializer.INTEGER).createOrOpen();
        Set<String> optionsSet = (Set<String>) responseMap.keySet();
        String[] optionsArray = optionsSet.stream().toArray(String[]::new);
        if (!keepDBOpen) {
            responsesDB.commit();
            responsesDB.close();
        }
        if (optionsArray.length <= 0) {
            return "";
        } else {
            int randomNum = ThreadLocalRandom.current().nextInt(0, optionsArray.length);
            return optionsArray[randomNum];
        }
    }

    /*** @return A String array of all words that have been used at least once as first word in a phrase***/
    private static String[] getFirstWords() {
        if (!keepDBOpen) {
            wordsDB = DBMaker.fileDB(WORDS_DB_FILE).closeOnJvmShutdown().make();
        }
        ConcurrentMap mapFW = wordsDB.hashMap("firstWordsMap").keySerializer(Serializer.STRING).valueSerializer(Serializer.INTEGER).createOrOpen();
        Set<String> optionsSet = (Set<String>) mapFW.keySet();
        String[] optionsArray = optionsSet.stream().toArray(String[]::new);
        if (!keepDBOpen) {
            wordsDB.close();
        }
        return optionsArray;
    }

    /*** @return A random word from the list of possible first words for a phrase.***/
    public static String getRandomFirstWord() {
        String[] optionsArray = getFirstWords();
        int randomNum = ThreadLocalRandom.current().nextInt(0, optionsArray.length);
        return optionsArray[randomNum];
    }

    /*** @return A possible next word for the given word to construct a phrase.
     * In case there is no possible next word, or the word is the end of a
     * phrase, returns "".***/
    public static String getNextWord(String word) {
        word = word.toLowerCase();
        if (!keepDBOpen) {
            db = DBMaker.fileDB(MAIN_DB_FILE).closeOnJvmShutdown().fileMmapEnableIfSupported().fileMmapPreclearDisable().make();
            db.getStore().fileLoad();
        }
        ConcurrentMap map = db.hashMap(word).keySerializer(Serializer.STRING).valueSerializer(Serializer.INTEGER).createOrOpen();
        Set<String> optionsSet = (Set<String>) map.keySet();
        Collection<Integer> values = map.values();
        String[] optionsArray = optionsSet.stream().toArray(String[]::new);
        int[] valuesArray = values.stream().mapToInt(i -> i).toArray();
        if (optionsArray.length <= 0) {
            return "";
        }
        int position = getGoodValue(optionsArray, valuesArray);
        boolean last = false;
        if (map.get(ISLASTWORD) != null && (int) map.get(ISLASTWORD) > (int) map.get(optionsArray[position])) {
            last = true;
        }
        if (optionsArray[position].equals(ISFIRSTWORD)) {
            last = true;
        }
        if (!keepDBOpen) {
            db.close();
        }
        if (optionsArray.length > 0 && !last) {
            return !optionsArray[position].equals(ISLASTWORD) ? optionsArray[position] : "";
        } else {
            return "";
        }
    }

    /*** @return A possible previous word for the given word to construct a
     * phrase. In case there is no possible previous word, or the word is the
     * begginning of a phrase, returns "".***/
    public static String getPreviousWord(String word) {
        return getNextWord(word + "prev");
    }

    /*** @return Selected randomly, one of the four best matches for a next word ***/
    private static int getGoodValue(String[] optionsArray, int[] valuesArray) {
        int[] bestFour = {0, 0, 0, 0};
        int temp;
        for (int x = 0; x < valuesArray.length; x++) {
            if (valuesArray[x] > bestFour[3]) {
                bestFour[3] = valuesArray[x];
            }
            if (bestFour[3] > bestFour[2]) {
                temp = bestFour[2];
                bestFour[2] = bestFour[3];
                bestFour[3] = temp;
            }
            if (bestFour[2] > bestFour[1]) {
                temp = bestFour[1];
                bestFour[1] = bestFour[2];
                bestFour[2] = temp;
            }
            if (bestFour[1] > bestFour[0]) {
                temp = bestFour[0];
                bestFour[0] = bestFour[1];
                bestFour[1] = temp;
            }
        }
        int randomNum = ThreadLocalRandom.current().nextInt(0, bestFour.length);
        int pos = findPosition(bestFour[randomNum], valuesArray);
        int count = 0; //To prevent infinite stalling if ISFIRSTWORD is the only option
        while (optionsArray[pos].equals("ISFIRSTWORD") && count < 3) {
            randomNum = ThreadLocalRandom.current().nextInt(0, bestFour.length);
            pos = findPosition(bestFour[randomNum], valuesArray);
            count++;
        }
        return pos;
    }

    /*** @return The position of the given number among the given array of values.
     * @param number: The number to look for among the array of values
     * @param values: An array of integers, possibly containing the desired number***/
    private static int findPosition(int number, int[] values) {
        int pos = 0;
        while (pos < values.length) {
            if (values[pos] == number) {
                break;
            } else {
                pos++;
            }
        }
        return pos < values.length ? pos : 0;
    }

    /*** @return True if the word can be used as a first word according to the database. False otherwise. ***/
    public static boolean isAFirstWord(String word) {
        boolean isFirst = false;
        String[] options = getFirstWords();
        for (int x = 0; x < options.length; x++) {
            if (word.equals(options[x])) {
                isFirst = true;
            }
        }
        return isFirst;
    }

    /*** Add a new default question-response pair to the database.
     * @param question: The question or sentence received from the user.
     * @param response: The default response ALLAI should answer to the given question.***/
    public static void loadDefaultResponse(String question, String response) {
        if (!keepDBOpen) {
            responsesDB = DBMaker.fileDB(RESPONSES_DB_FILE).closeOnJvmShutdown().fileMmapEnableIfSupported().fileMmapPreclearDisable().make();
            responsesDB.getStore().fileLoad();
        }
        ConcurrentMap responseMap = responsesDB.hashMap("defaultResponses").keySerializer(Serializer.STRING).valueSerializer(Serializer.STRING).createOrOpen();
        try {
            if (responseMap.get(question) == null) {
                responseMap.put(question, response);
            }
        } catch (Exception e) {
            logError("Dictionary: Exception at addOrUpdateParameter. Word received: " + response + ". " + e.getMessage());
        }
        if (!keepDBOpen) {
            responsesDB.commit();
            responsesDB.close();
        }
    }

    /*** Get a default answer for the given phrase or question
     * @param phrase: The entire question or sentence received from the user.
     * @return A default response for the given phrase, if any, or "" otherwise.***/
    public static String getDefaultAnswer(String phrase) {
        ArrayList<String> responses = new ArrayList<>();
        if (!keepDBOpen) {
            responsesDB = DBMaker.fileDB(RESPONSES_DB_FILE).closeOnJvmShutdown().fileMmapEnableIfSupported().fileMmapPreclearDisable().make();
            responsesDB.getStore().fileLoad();
        }
        ConcurrentMap responseMap = responsesDB.hashMap("defaultResponses").keySerializer(Serializer.STRING).valueSerializer(Serializer.STRING).createOrOpen();
        Set<String> questionsSet = (Set<String>) responseMap.keySet();
        String[] questions = questionsSet.stream().toArray(String[]::new);
        for (int x = 0; x < questions.length; x++) {
            if (phrase.contains(questions[x])) {
                responses.add((String) responseMap.get(questions[x]));
            }
        }
        if (!keepDBOpen) {
            responsesDB.commit();
            responsesDB.close();
        }
        if (responses.size() > 0) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, responses.size());
            return responses.get(randomNum);
        } else {
            return "";
        }
    }
}
