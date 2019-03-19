/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main;

import allai.main.utils.DefaultResponsesLoader;
import allai.main.utils.SpanishImportantWords;
import static allai.utils.ALLAILogger.logInfo;
import allai.utils.BookReader;
import java.util.ArrayList;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class LanguageAnalyzer {

    private static BookReader reader;
    private static String book = "files\\book.txt";
    private static String lastResponse = "";
    private int threadId = 0;

    /**
     * * Instatiates a new LanguageAnalyzer object. This class is used for
     * language learning and responses construction.
     *
     * @param filename: The path to the book ALLAI should learn from, if
     * 'otherBook' parameter is set to true.
     * @param otherBook: Set to true to make ALLAI learn a different book than
     * the default one, or set to false otherwise.
     * @param learnBook: If true, ALLAI will begin the first learn process
     * (read and store data from the book). This may take several hours. Set to
     * false for default use (responses and random phrases construction).
     * @param threadId: The ID for the current thread (for logging purposes).
    **
     */
    public LanguageAnalyzer(String filename, boolean otherBook, boolean learnBook, int threadId) {
        this.threadId = threadId;
        if (otherBook) {
            book = filename;
        }
        if (learnBook) {
            reader = new BookReader(book);
        }
        lastResponse = "";
    }

    /*** Reads, analyzes and storages the default book, or the one given by the user on the constructor. ***/
    public void learnBook() {
        System.out.println("LanguageAnalyzer: [FIRST LEARNING INITIATED]");
        int count = 0;
        ArrayList<String> phrase = reader.nextPhrase();
        while (!phrase.get(0).equals(BookReader.NO_NEXT)) {
            if (!phrase.get(0).equals(BookReader.SKIP)) {
                Dictionary.learnNewPhrase(phrase);
                count++;
                if (count % 100 == 0) {
                    System.out.println("LanguageAnalyzer: [FIRST LEARNING UPDATE] " + count + " new phrases have been learned");
                }
            }
            phrase = reader.nextPhrase();
        }
        System.out.println("Book learned");
    }

    /**
     * * Call this function to get a random phrase from ALLAI.
     *
     * @return A random phrase. **
     */
    public String getRandomPhrase() {
        String rootWord = Dictionary.getRandomWord();
        String output = Dictionary.getPhraseWithRootWord(rootWord);
        System.out.println("PHRASE: " + output + "\n");
        return output;
    }

    /**
     * * Call this function to get a response from ALLAI to a given sentence.
     *
     * @param phrase: The phrase entered by the user, that ALLAI should respond
     * to.
     * @return A response for the given phrase **
     */
    public String getResponse(String phrase) {
        phrase = deleteSpecialChars(phrase);
        String defaultQuestion = new DefaultResponsesLoader(threadId).getDefaultQuestion(phrase);
        String phraseRootWord = "";
        if (!defaultQuestion.equals("")){
            logInfo("LanguageAnalyzer " + threadId + ": Identified as default question.");
            phraseRootWord = defaultQuestion;
        }else{
            logInfo("LanguageAnalyzer " + threadId + ": Identified as random phrase.");
            phraseRootWord = SpanishImportantWords.getMostImportantWord(phrase);
        }
        logInfo("LanguageAnalyzer " + threadId + ": Most important word: " + phraseRootWord);
        String rootWord = Dictionary.getResponse(phraseRootWord);
        if (rootWord.equals("")) {
            rootWord = phraseRootWord;
            logInfo("LanguageAnalyzer " + threadId + ": No answer found to root word, using " + rootWord + " as root word");
        } else {
            rootWord = rootWord.replaceAll("_", " ");
        }
        String response;
        if (rootWord.split(" ").length > 1) {
            response = rootWord;
        } else {
            logInfo("LanguageAnalyzer " + threadId + ": Retrieving phrase with root word: " + rootWord);
            response = Dictionary.getPhraseWithRootWord(rootWord);
            if (deleteGhostSpaces(response).toLowerCase().equals(deleteGhostSpaces(phraseRootWord.toLowerCase()))){
                logInfo("LanguageAnalyzer " + threadId + ": Response equal to question, responding random phrase");
                response = getRandomPhrase();
            }
        }
        Dictionary.learnNewPhrase(phrase);
        if (!lastResponse.equals("")) {
            String lastResponseRootWord = SpanishImportantWords.getMostImportantWord(lastResponse);
            Dictionary.addResponse(lastResponseRootWord, phraseRootWord);
        }
        lastResponse = deleteGhostSpaces(deleteSpecialChars(response));
        return deleteGhostSpaces(response);
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
     * * Deletes spaces added to the start and end of a sentence during its generation in the Dictionary.
     *
     * @return The transformed sentence. **
     */
    private String deleteGhostSpaces(String input) {
        while (input.startsWith(" ")) {
            input = input.substring(1);
        }
        while (input.endsWith(" ")) {
            input = input.substring(0, input.length() - 1);
        }
        return input;
    }

}
