/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main;

import allai.main.utils.DefaultResponsesLoader;
import allai.utils.BookReader;
import allai.main.utils.SpanishImportantWords;
import allai.main.utils.WordContextInfo;
import static allai.utils.ALLAILogger.logError;
import java.util.ArrayList;
import static allai.utils.ALLAILogger.logInfo;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Daniel Alejandro Hurtado Simoes Universidad de Málaga TFG - Grado en
 * Ingeniería Telemática
 */
public class LanguageAnalyzer {

    private String book = "files\\book.txt";
    private BookReader reader;
    private ArrayList<WordContextInfo> analyzedPhrase;
    private static String lastResponse;
    private boolean firstLearn;
    private int threadId;

    /**
     * * Instatiates a new LanguageAnalyzer object. This class is used for
     * language learning and responses construction.
     *
     * @param filename: The path to the book ALLAI should learn from, if
     * 'otherBook' parameter is set to true.
     * @param otherBook: Set to true to make ALLAI learn a different book than
     * the default one, or set to false otherwise.
     * @param firstLearn: If true, ALLAI will begin the first learn process
     * (read and store data from the book). This may take several hours. Set to
     * false for default use (responses and random phrases construction)
    **
     */
    public LanguageAnalyzer(String filename, boolean otherBook, boolean firstLearn) {
        if (otherBook) {
            book = filename;
        }
        this.firstLearn = firstLearn;
        if (firstLearn) {
            reader = new BookReader(book);
            Dictionary.keepDBOpen = true;
            Dictionary.initializeDB();
            firstLearning();
            Dictionary.commitAndCloseDB();
        }
        lastResponse = "";
    }

    /**
     * * Starts the first learning operation. For the book given on the
     * constructor (or the default one), ALLAI will proceed to read and store
     * all information it contains. This process may take several hours (up to 8
     * hours or more is possible). **
     */
    private void firstLearning() {
        logInfo("LanguageAnalyzer: [FIRST LEARNING INITIATED]");
        int count = 0;
        ArrayList<String> phrase = reader.nextPhrase();
        while (!phrase.get(0).equals(BookReader.NO_NEXT)) {
            analyzedPhrase = getWordsInfo(phrase);
            storeLearnedInfo(analyzedPhrase);
            phrase = reader.nextPhrase();
            count++;
            if (count%100 == 0) {
                logInfo("LanguageAnalyzer: [FIRST LEARNING UPDATE] " + count + " new phrases have been learned");
            }
        }
    }

    /**
     * * Analyze and retrieve context information for all words in a sentence.
     *
     * @return For a given phrase, returns an ArrayList of the object
     * WordContextInfo, containing all context information for every word in the
     * sentence.
     * @param phrase: The sentence containing the words to analyze and retrieve
     * information from.**
     */
    private ArrayList<WordContextInfo> getWordsInfo(ArrayList<String> phrase) {
        ArrayList<WordContextInfo> phraseInfo = new ArrayList<>();
        for (int x = 0; x < phrase.size(); x++) {
            phraseInfo.add(new WordContextInfo(phrase, x));
        }
        return phraseInfo;
    }

    /**
     * * Store all retrieved information inside the database. **
     */
    private void storeLearnedInfo(ArrayList<WordContextInfo> phrase) {
        if(!firstLearn){
            logInfo("LanguageAnalyzer: Storing learned info");
        }
        if (!Dictionary.isDBOpen) {
            Dictionary.keepDBOpen = true;
            Dictionary.initializeDB();
        }
        phrase.forEach((item) -> {
            Dictionary.updateDictionary(item);
        });
        if (Dictionary.isDBOpen) {
            Dictionary.commitAndCloseDB();
        }
    }

    /**
     * * Call this function each time ALLAI receives a message from a user to
     * keep learning. It will analyze it, and store all retrieved information in
     * the database. **
     */
    private void learnNewPhrase(String phrase) {
        if (!lastResponse.equals(" ")) {
            Dictionary.addAsResponse(SpanishImportantWords.getMostImportantWord(lastResponse), SpanishImportantWords.getMostImportantWord(phrase));
        }
        //String subPhrases[] = phrase.split(".");
        //for (int i = 0; i < subPhrases.length; i++) {
            ArrayList<String> arrayPhrase = convertToArrayList(deleteSpecialChars(phrase));
            analyzedPhrase = getWordsInfo(arrayPhrase);
            storeLearnedInfo(analyzedPhrase);
        //}
    }

    /**
     * * Deletes all special charaters in a sentence, except for áéíóú, their
     * Upper Case equals, and the letter Ñ.
     *
     * @return The transformed sentence. **
     */
    private String deleteSpecialChars(String input) {
        String alphaOnly = input.replaceAll("[^a-zA-ZóéíáúñÁÉÍÓÚÑ ]+", "");
        return alphaOnly;
    }

    /**
     * * Converts a sentence into an ArrayList of Strings.
     *
     * @return An ArrayList of Strings, in wich each element of the array is a
     * word of the sentence.
     * @param phrase: The sentence to be transformed.**
     */
    private ArrayList<String> convertToArrayList(String phrase) {
        ArrayList<String> array = new ArrayList<>();
        String temp[] = phrase.split(" ");
        for (int i = 0; i < temp.length; i++) {
            array.add(temp[i]);
        }
        return array;
    }

    /**
     * * Call this function to get a response from ALLAI to a given sentence.
     *
     * @param phrase: The phrase entered by the user, that ALLAI should respond
     * to.
     * @return A response for the given phrase **
     */
    public String getResponse(String phrase, int threadId) {
        this.threadId = threadId;
        phrase = deleteSpecialChars(phrase);
        while (Dictionary.dataBasesInUse){
            logInfo("LanguageAnalyzer " + threadId + ": Dictionary in use, waiting for release.");
            try {
                Thread.sleep(1000*5);
            } catch (InterruptedException ex) {
                logError("LanguageAnalyzer " + threadId + ": ERROR, thread was interrupted while waiting to use the Dictionary.");
            }
        };
        Dictionary.dataBasesInUse = true;
        if (!Dictionary.isDBOpen) {
            Dictionary.keepDBOpen = true;
            Dictionary.initializeDB();
        }
        String defaultResponse = Dictionary.getDefaultAnswer(phrase);
        if (!defaultResponse.equals("")) {
            logInfo("LanguageAnalyzer " + threadId + ": Default response found: " + defaultResponse);
            // If the default response is a whole phrase by itself, return it. If it is just one word, construct a phrase with it
            if (defaultResponse.split(" ").length > 1) {
                if (Dictionary.isDBOpen) {
                    Dictionary.commitAndCloseDB();
                }
                Dictionary.dataBasesInUse = false;
                return defaultResponse;
            } else {
                String response = getPhraseWithRootWord(defaultResponse);
                learnNewPhrase(phrase);
                if (Dictionary.isDBOpen) {
                    Dictionary.commitAndCloseDB();
                }
                Dictionary.dataBasesInUse = false;
                return response;
            }
        } else {
            String response = "";
            String phraseRootWord = SpanishImportantWords.getMostImportantWord(phrase);
            logInfo("LanguageAnalyzer " + threadId + ": Most important word: " + phraseRootWord);
            String rootWord = Dictionary.getResponse(phraseRootWord);
            if (rootWord.equals("")) {
                rootWord = phraseRootWord;
                logInfo("LanguageAnalyzer " + threadId + ": No answer found to root word, using " + rootWord + " as root word");
            }
            logInfo("LanguageAnalyzer " + threadId + ": Retrieving phrase with root word: " + rootWord);
            response = getPhraseWithRootWord(rootWord);
            learnNewPhrase(phrase);
            if (Dictionary.isDBOpen) {
                Dictionary.commitAndCloseDB();
            }
            Dictionary.dataBasesInUse = false;
            lastResponse = response;
            return response;
        }
    }

    /**
     * * Read the default question-response pairs stored in the files
     * questions.txt and responses.txt (located at files folder), and store them
     * in the database. **
     */
    public void learnDefaultResponses() {
        DefaultResponsesLoader loader = new DefaultResponsesLoader();
        loader.createResponsesDB();
    }

    /**
     * * Constructs a sentence from a important word. This word shoud be the
     * most important word of the phrase, but not the first one. To construct a
     * sentence with a given fisrt word, call 'getPhraseWithFirstWord()'.
     *
     * @return The constructed sentence. In case ALLAI was not able to construct
     * the sentence, returns a random phrase.**
     */
    private String getPhraseWithRootWord(String rootWord) {
        String response = "";
        String phraseRoot = "";
        if (Dictionary.isAFirstWord(rootWord)) {
            response = getPhraseWithFirstWord(rootWord);
        } else {
            String prev = Dictionary.getPreviousWord(rootWord);
            phraseRoot = prev + " ";
            int count = 0;
            if (!prev.equals("")) {
                while (!prev.equals("") && count < 15 && !Dictionary.isAFirstWord(prev)) {
                    prev = Dictionary.getPreviousWord(prev);
                    phraseRoot = prev + " " + phraseRoot;
                    count++;
                }
                response = phraseRoot + getPhraseWithFirstWord(rootWord);
            } else {
                logInfo("LanguageAnalyzer: Not able to construct phrase with root word. Responding random phrase");
                response = getRandomPhrase();
            }
        }
        logInfo("LanguageAnalyzer: Root Word response: " + response);
        return response;
    }

    /**
     * * Construct a sentence begining with the given first word.
     *
     * @return The constructed sentence. In case ALLAI doesn't know the given
     * word, returns "".**
     */
    private String getPhraseWithFirstWord(String firstWord) {
        String response = "";
        int count = 0;
        count++;
        String next = Dictionary.getNextWord(firstWord);
        count++;
        response = firstWord;
        while (!next.equals("") && count < 15) {
            response = response + " " + next;
            next = Dictionary.getNextWord(next);
            count++;
        }
        count = 0;
        return response;
    }

    /**
     * * Get a random phrase from ALLAI.
     *
     * @return A String containing the phrase.**
     */
    public String getRandomPhrase() {
        String response = "";
        if (!Dictionary.isDBOpen) {
            Dictionary.keepDBOpen = true;
            Dictionary.initializeDB();
        }
        String firstWord = Dictionary.getRandomFirstWord();
        response = getPhraseWithFirstWord(firstWord);
        Dictionary.commitAndCloseDB();
        return response;
    }
}
