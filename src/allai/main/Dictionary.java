/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main;

import static allai.utils.ALLAILogger.logError;
import static allai.utils.ALLAILogger.logInfo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.h2.tools.Server;

/**
 * @author Daniel Alejandro Hurtado Simoes Universidad de Málaga TFG - Grado en
 * Ingeniería Telemática
 */
public class Dictionary {

    private static String OS = System.getProperty("os.name").toLowerCase();
//    private static String urlJDBC = "jdbc:h2:tcp://localhost/C:/Users/dahs/Documents/NetBeansProjects/ALLAI 2/db/ALLAI";
//    private static String urlJDBCresponses = "jdbc:h2:tcp://localhost/C:/Users/dahs/Documents/NetBeansProjects/ALLAI 2/db/ALLAIresponses";
    private static String urlJDBC = "jdbc:h2:tcp://localhost/~/ALLAI/db/ALLAI";
    private static String urlJDBCresponses = "jdbc:h2:tcp://localhost/~/ALLAI/db/ALLAIresponses";

    private static Connection dbConn;
    private static Connection dbRespConn;
    private static Server server;

    private static String vetoedLastWords = "de en y a que con los la el las con mientras";

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    /**
     * * Starts H2 server and initializes DB Connections.
     */
    public static void start() {
        if (isWindows()) {
            urlJDBC = "jdbc:h2:tcp://localhost/C:/Users/dahs/Documents/NetBeansProjects/ALLAI 2/db/ALLAI";
            urlJDBCresponses = "jdbc:h2:tcp://localhost/C:/Users/dahs/Documents/NetBeansProjects/ALLAI 2/db/ALLAIresponses";
        }
        startServer();
        initConnections();
    }

    /**
     * * Stops H2 server and closes DB Connections.
     */
    public static void stop() {
        closeConnections();
        stopServer();
    }

    /**
     * * Start H2 server.
     */
    private static void startServer() {
        try {
            Class.forName("org.h2.Driver");
            server = Server.createTcpServer("-tcpAllowOthers").start();
            logInfo("Dictionary: H2 Server started");
        } catch (Exception e) {
            logError("Dictionary: H2 Server could not be started: " + e.getMessage());
        }
    }

    /**
     * * Stop H2 server.
     */
    private static void stopServer() {
        server.stop();
        logInfo("Dictionary: H2 Server stopped");
    }

    /**
     * * Initializes dbConn and dbRespConn.
     */
    private static void initConnections() {
        try {
            dbConn = DriverManager.getConnection(urlJDBC, "sa", "");
            dbRespConn = DriverManager.getConnection(urlJDBCresponses, "sa", "");
        } catch (SQLException ex) {
            logError("Dictionary: An error occured while trying to initialize connections: " + ex.getMessage());
        }
    }

    /**
     * * Close dbConn and dbRespConn.
     */
    private static void closeConnections() {
        try {
            dbConn.close();
            dbRespConn.close();
        } catch (SQLException ex) {
            Logger.getLogger(Dictionary.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * * Checks if the connection is still active. If it is, it returns it. If
     * not, it initializes it and returns it.
     *
     * @return: A valid dbConn connection.
     */
    private static Connection getDBConnection() {
        try {
            if (dbConn.isValid(0)) {
                return dbConn;
            } else {
                initConnections();
                return dbConn;
            }
        } catch (SQLException ex) {
            logError("Dictionary: An error occurred while checking if dbConn was valid: " + ex.getMessage());
            initConnections();
            return dbConn;
        }
    }

    /**
     * * Checks if the connection is still active. If it is, it returns it. If
     * not, it initializes it and returns it.
     *
     * @return: A valid dbRespConn connection.
     */
    private static Connection getDBRespConnection() {
        try {
            if (dbRespConn.isValid(0)) {
                return dbRespConn;
            } else {
                initConnections();
                return dbRespConn;
            }
        } catch (SQLException ex) {
            logError("Dictionary: An error occurred while checking if dbRespConn was valid: " + ex.getMessage());
            initConnections();
            return dbRespConn;
        }
    }

    /**
     * Creates a table in the DB for the given word if it doesn't exist already.
     */
    private static void createTableForWord(String word, Connection conn) {
        try {
            Statement st = conn.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS " + word + "(word VARCHAR(30), prev1 INT, prev2 INT, prev3 INT, prev4 INT, prev5 INT, prev6 INT, prev7 INT, prev8 INT, prev9 INT, next1 INT, next2 INT, next3 INT, next4 INT, next5 INT, next6 INT, next7 INT, next8 INT, next9 INT, first BIT, last BIT);");
        } catch (SQLException ex) {
            if (!ex.getMessage().contains("Syntax")) {
                logError("Dictionary: Error while creating a table: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Creates a table in the DB responses for the last responses if it doesn't exist already.
     */
    private static void createTableForLastResponse(Connection conn) {
        try {
            Statement st = conn.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS LAST_RESPONSE(chatId BIGINT, response VARCHAR(30));");
        } catch (SQLException ex) {
            if (!ex.getMessage().contains("Syntax")) {
                logError("Dictionary: Error while creating a table: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Stores the last response ALLAI gave to a given user
     */
    public static void storeLastResponse(String response, long chatId){
        dbRespConn = getDBRespConnection();
        createTableForLastResponse(dbRespConn);
        try {
            Statement st = dbRespConn.createStatement();
            ResultSet result = st.executeQuery("SELECT * FROM LAST_RESPONSE WHERE chatId=" + chatId + ";");
                    if (!result.isBeforeFirst()) {
                        st.execute("INSERT INTO LAST_RESPONSE(chatId, response) VALUES(" + chatId + ", '" + response + "');");
                    }else{
                        st.execute("UPDATE LAST_RESPONSE SET response='" + response + "' WHERE chatId=" + chatId + ";");
                    }
        } catch (SQLException ex) {
            logError("Dictionary: An error occured while storing a last response: " + ex.getMessage());
        }
    }
    
    /**
     * Returns the last response ALLAI gave to a given user
     */
    public static String getLastResponse(long chatId){
        String output = "";
        dbRespConn = getDBRespConnection();
        try {
            Statement st = dbRespConn.createStatement();
            try{
                ResultSet result = st.executeQuery("SELECT response FROM LAST_RESPONSE WHERE chatId=" + chatId + ";");
                if (result.isBeforeFirst()) {
                    result.next();
                    output = result.getString("response");
                }
            } catch (SQLException ex) {
                return output;
            }
        } catch (SQLException ex) {
            logError("Dictionary: An error occured while getting a last response: " + ex.getMessage());
        }
        return output;
    }

    /**
     * Storages all new info from the given phrase
     *
     * @param phrase: A String containing the phrase to be storaged.
     */
    public static void learnNewPhrase(String phrase) {
        ArrayList<String> preparedPhrase = new ArrayList<>();
        String[] divided = phrase.split(" ");
        preparedPhrase.addAll(Arrays.asList(divided));
        learnNewPhrase(preparedPhrase);
    }

    /**
     * Storages all new info from the given phrase
     *
     * @param phrase: An ArrayList containing the phrase to be storaged.
     */
    public static void learnNewPhrase(ArrayList<String> phrase) {
        dbConn = getDBConnection();
        for (String word : phrase) {
            if (!word.isEmpty() && !word.equals("") && !word.equals("all") && !word.contains(" ")) {
                createTableForWord(word, dbConn);
                try {
                    addInfo(word, phrase, dbConn);
                } catch (SQLException ex) {
                    if (!ex.getMessage().contains("Syntax")) {
                        logError("Dictionary: An error occured while learning a new phrase: " + ex.getMessage());
                    }
                }
            }
        }
    }

    /**
     * For an already existing table associated to a word, storage all
     * contextual info related to that word in the given phrase.
     *
     * @param word: The word on which's table the info is going to be storaged.
     * @param phrase: The phrase from which the word came out.
     * @param conn: The connection to the database.
     */
    private static void addInfo(String word, ArrayList<String> phrase, Connection conn) throws SQLException {
        int wordIndex = phrase.indexOf(word);
        int size = phrase.size();
        int columnId = 1;
        String column = "prev" + columnId;
        Statement st = conn.createStatement();
        for (int x = wordIndex - 1; x >= 0; x--) {
            if (columnId <= 9) {
                ResultSet result = st.executeQuery("SELECT * FROM " + word + " WHERE word='" + phrase.get(x).toLowerCase() + "';");
                if (!result.isBeforeFirst()) {
                    st.execute("INSERT INTO " + word + "(word, prev1, prev2, prev3, prev4, prev5, prev6, prev7, prev8, prev9, next1, next2, next3, next4, next5, next6, next7, next8, next9, first, last) VALUES('" + phrase.get(x).toLowerCase() + "', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);");
                }
                st.execute("UPDATE " + word + " SET " + column + "=" + column + "+1 WHERE word='" + phrase.get(x).toLowerCase() + "';");
                columnId++;
                column = "prev" + columnId;
            }
        }
        columnId = 1;
        column = "next" + columnId;
        for (int x = wordIndex + 1; x < size; x++) {
            if (columnId <= 9) {
                ResultSet result = st.executeQuery("SELECT * FROM " + word + " WHERE word='" + phrase.get(x).toLowerCase() + "';");
                if (!result.isBeforeFirst()) {
                    st.execute("INSERT INTO " + word + "(word, prev1, prev2, prev3, prev4, prev5, prev6, prev7, prev8, prev9, next1, next2, next3, next4, next5, next6, next7, next8, next9, first, last) VALUES('" + phrase.get(x).toLowerCase() + "', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);");
                }
                st.execute("UPDATE " + word + " SET " + column + "=" + column + "+1 WHERE word='" + phrase.get(x).toLowerCase() + "';");
                columnId++;
                column = "next" + columnId;
            }
        }
        boolean isFirst = phrase.indexOf(word) == 0;
        boolean isLast = phrase.indexOf(word) == phrase.size() - 1;
        if (isFirst || isLast) {
            ResultSet result = st.executeQuery("SELECT * FROM " + word + " WHERE word='" + word.toLowerCase() + "';");
            if (!result.isBeforeFirst()) {
                st.execute("INSERT INTO " + word + "(word, prev1, prev2, prev3, prev4, prev5, prev6, prev7, prev8, prev9, next1, next2, next3, next4, next5, next6, next7, next8, next9, first, last) VALUES('" + word.toLowerCase() + "', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);");
            }
            if (isFirst) {
                st.execute("UPDATE " + word + " SET first=1 WHERE word='" + word.toLowerCase() + "';");
            } else {
                st.execute("UPDATE " + word + " SET last=1 WHERE word='" + word.toLowerCase() + "';");
            }
        }
    }

    /**
     * Returns a random word from the data base.
     *
     * @return A random word.
     */
    public static String getRandomWord() {
        String randomWord = "";
        try {
            dbConn = getDBConnection();
            Statement st = dbConn.createStatement();
            ResultSet result = st.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC' ORDER BY RAND() LIMIT 1;");
            result.next();
            randomWord = result.getString("TABLE_NAME");
        } catch (SQLException ex) {
            logError("Dictionary: An error occured while retrieving a random word: " + ex.getMessage());
        }
        return randomWord;
    }

    /**
     * Analyzing the word's table info, retrieve the next or previous
     * numberOfWords words, with sintactic sense between them.
     *
     * @param prev: Determines if this method returns next words or previous
     * words.
     */
    private static String getNextWords(Statement st, String word, int numberOfWords, boolean prev) throws SQLException {
        String constructed = "";
        String lastOption = word;
        String possibleAnswer = "";
        int x = 1;
        if (isLastWord(st, word, prev)) {
            possibleAnswer = "";
        }
        while (x <= numberOfWords) {
            int count = 0;
            String option = getNextWordOfIndex(st, word, x, prev, count);
            boolean possible = false;
            count++;
            while (!possible && count < 50 && !option.equals("NO_NEXT")) {
                possible = isANextWord(st, lastOption, option, prev);
                if (!possible) {
                    option = getNextWordOfIndex(st, word, x, prev, count);
                }
                count++;
            }
            if (!possible) {
                break;
            }
            if (option.equals("NO_NEXT")) {
                break;
            }
            constructed += option + " ";
            lastOption = option;
            if (isLastWord(st, option, prev)) {
                possibleAnswer = constructed;
            }
            x++;
        }
        String response = possibleAnswer.equals("") ? constructed : possibleAnswer;
        if (endsWithVetoedLastWord(response)) {
            response = removeLastWord(response);
        }
        if (response.endsWith(" ")) {
            response = response.substring(0, response.length() - 1);;
        }
        return response;
    }

    private static String getNextWordOfIndex(Statement st, String word, int index, boolean prev, int count) throws SQLException {
        String column = null;
        if (prev) {
            column = "prev";
        } else {
            column = "next";
        }
        if (index > 9) {
            return "NO_NEXT";
        }
        int limit = count + 1;
        ResultSet result;
        try {
            result = st.executeQuery("SELECT WORD FROM " + word + " WHERE " + column + index + ">0 ORDER BY " + column + index + " DESC LIMIT " + limit + ";");
        } catch (Exception e) {
            return "NO_NEXT";
        }
        int x = 0;
        while (x < limit) {
            if (!result.next()) {
                return "NO_NEXT";
            }
            x++;
        }
        return result.getString("WORD");
    }

    private static boolean isANextWord(Statement st, String prevWord, String nextWord, boolean prev) throws SQLException {
        String column;
        if (prev) {
            column = "prev";
        } else {
            column = "next";
        }
        boolean isNext = false;
        ResultSet result = st.executeQuery("SELECT WORD FROM " + prevWord + " WHERE " + column + "1>0;");
        while (result.next()) {
            String tested = result.getString("WORD");
            if (tested.equals(nextWord)) {
                isNext = true;
            }
        }
        return isNext;
    }

    private static boolean isLastWord(Statement st, String option, boolean prev) throws SQLException {
        String column;
        if (prev) {
            column = "first";
        } else {
            column = "last";
        }
        boolean isLast = false;
        if (vetoedLastWords.contains(option) && !prev) {
            return false;
        }
        ResultSet result;
        try {
            result = st.executeQuery("SELECT " + column + " FROM " + option + " WHERE WORD='" + option.toLowerCase() + "';");
        } catch (Exception e) {
            return false;
        }
        if (!result.next()) {
            return false;
        }
        isLast = result.getBoolean(column);
        return isLast;
    }

    private static String getPrevWords(Statement st, String rootWord, int i) throws SQLException {
        String prevWords = getNextWords(st, rootWord, i, true);
        String[] words = prevWords.split(" ");
        String output = "";
        for (int x = words.length - 1; x >= 0; x--) {
            output += words[x] + " ";
        }
        return output;
    }

    private static String getNextWords(Statement st, String rootWord, int i) throws SQLException {
        return getNextWords(st, rootWord, i, false);
    }

    /**
     * Generate a phrase using the given word as root.
     *
     * @param rootWord: Root or seed from where to generate a phrase.
     * @return A phrase using the given word.
     */
    public static String getPhraseWithRootWord(String rootWord) {
        String firstHalf = "";
        String secondHalf = "";
        try {
            dbConn = getDBConnection();
            Statement st = dbConn.createStatement();
            secondHalf = rootWord + " " + getNextWords(st, rootWord, 9);
            firstHalf = getPrevWords(st, rootWord, 9);
        } catch (SQLException ex) {
            Logger.getLogger(Dictionary.class.getName()).log(Level.SEVERE, null, ex);
        }
        return firstHalf + secondHalf.toLowerCase();
    }

    /**
     * Add a response to the given question in the database.
     *
     * @param question: The phrase to be answered.
     * @param response: The response to the question.
     */
    public static void addResponse(String question, String response) {
        try {
            dbRespConn = getDBRespConnection();
            Statement st = dbRespConn.createStatement();
            createTableForResponse(question, st);
            ResultSet result = st.executeQuery("SELECT * FROM " + question + " WHERE response='" + response.toLowerCase() + "';");
            if (!result.isBeforeFirst()) {
                st.execute("INSERT INTO " + question + "(response, count) VALUES('" + response.toLowerCase() + "', 0);");
            }
            st.execute("UPDATE " + question + " SET count=count+1 WHERE response='" + response.toLowerCase() + "';");
        } catch (SQLException ex) {
        }
    }

    /**
     * Consults the responses data base and returns a response, if any, to the
     * given question.
     *
     * @param question: The word to respond to.
     * @return A response to the question, if any.
     */
    public static String getResponse(String question) {
        String output = "";
        try {
            dbRespConn = getDBRespConnection();
            Statement st = dbRespConn.createStatement();
            ResultSet result;
            try {
                result = st.executeQuery("SELECT RESPONSE FROM " + question + " ORDER BY COUNT DESC;");
            } catch (SQLException ex) {
                return output;
            }
            if (!result.next()) {
                output = "";
            } else {
                output = result.getString("RESPONSE");
                Random r = new Random();
                int prob = r.nextInt(101);
                while (prob > 50) {
                    prob = r.nextInt(101);
                    try {
                        result.next();
                        output = result.getString("RESPONSE");
                    } catch (SQLException ex) {
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            logError("Dictionary: Error while getting a response: " + ex.getMessage());
        }
        return output;
    }

    private static void createTableForResponse(String word, Statement st) {
        try {
            st.execute("CREATE TABLE IF NOT EXISTS " + word + "(response VARCHAR(50), count INT);");
        } catch (SQLException ex) {
            if (!ex.getMessage().contains("Syntax")) {
                logError("Dictionary: Error while creating a response table: " + ex.getMessage());
            }
        }
    }

    private static boolean endsWithVetoedLastWord(String response) {
        String[] split = response.split(" ");
        String lastWord = split[split.length - 1];
        return vetoedLastWords.contains(lastWord);
    }

    private static String removeLastWord(String response) {
        String[] split = response.split(" ");
        String output = "";
        for (int x = 0; x < split.length - 1; x++) {
            output += split[x] + " ";
        }
        return output;
    }

}
