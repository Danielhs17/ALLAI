/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main.services;

import static allai.utils.ALLAILogger.logError;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class ListService extends Service {

    private enum Service {
        ENUMERARLISTAS, CREARLISTA, VACIARLISTA, MOSTRARLISTA, ELIMINARLISTA, AGREGARELEM, QUITARELEM, ERROR
    }
    private Service service;
    
    private static boolean fileInUse = false;

    // /lista crear [nombre]
    // /lista nombre agregar [objeto]
    // /lista nombre quitar [objeto]
    // /lista eliminar [nombre]
    // /lista vaciar [nombre]
    // /lista mostrar [nombre]
    // /lista todas
    private String listName;
    private String elemName = "";
    private String chatId;

    private final String UsersListsFile = "UsersLists.json";
    private final String numberOfElements = "numberOfElements";
    private final String elem = "elem";

    private String error = "Lo siento, no he entendido lo que quieres hacer. Por favor, utiliza \"/ayuda lista\" para ver ejemplos de cómo utilizar este servicio.";

    public ListService(String[] arg, long chatId) {
        logInfo("ListService: Initiated");
        this.chatId = "" + chatId;
        int arguments = arg.length;
        if (arguments < 2) {
            service = Service.ERROR;
        } else {
            if (arguments == 2) {
                if (arg[1].equals("todas")) {
                    service = Service.ENUMERARLISTAS;
                } else {
                    service = Service.ERROR;
                }
            } else if (arguments == 3) {
                if (arg[1].equals("crear")) {
                    listName = arg[2];
                    service = Service.CREARLISTA;
                } else if (arg[1].equals("eliminar")) {
                    listName = arg[2];
                    service = Service.ELIMINARLISTA;
                } else if (arg[1].equals("vaciar")) {
                    listName = arg[2];
                    service = Service.VACIARLISTA;
                } else if (arg[1].equals("mostrar")) {
                    listName = arg[2];
                    service = Service.MOSTRARLISTA;
                } else {
                    service = Service.ERROR;
                }
            } else if (arguments >= 4) {
                listName = arg[1];
                for (int x = 3; x < arguments; x++) {
                    elemName += arg[x] + " ";
                }
                elemName = elemName.substring(0, elemName.length() - 1);
                if (arg[2].equals("agregar")) {
                    service = Service.AGREGARELEM;
                } else if (arg[2].equals("quitar")) {
                    service = Service.QUITARELEM;
                } else {
                    service = Service.ERROR;
                }
            }
        }
    }

    @Override
    public String getResponse() {
        String response = error;
        switch (service) {
            case CREARLISTA:
                response = createList();
                break;
            case VACIARLISTA:
                response = emptyList();
                break;
            case MOSTRARLISTA:
                response = showList();
                break;
            case ELIMINARLISTA:
                response = eliminateList();
                break;
            case ENUMERARLISTAS:
                response = showAllLists();
                break;
            case AGREGARELEM:
                response = addElement();
                break;
            case QUITARELEM:
                response = deleteElement();
                break;
            case ERROR:
                response = error;
                break;
        }
        return response;
    }

    private String createList() {
        String alreadyExists = "Oh, parece que ya tienes una lista con ese nombre! Si deseas ver todas las listas que tienes, lo que contiene alguna, o borrar alguna, utiliza el comando \"/ayuda listas\" para averiguar cómo hacerlo!";
        String response;
        while (fileInUse);
        fileInUse = true;
        JSONObject users = getFileContent();
        if (!users.containsKey(chatId)) {
            users.put(chatId, new JSONObject());
        }
        JSONObject lists = (JSONObject) users.get(chatId);
        if (lists.containsKey(listName)) {
            response = alreadyExists;
        } else {
            lists.put(listName, new JSONObject());
            JSONObject userList = (JSONObject) lists.get(listName);
            userList.put(numberOfElements, 0);
            response = "Listo! Tu lista ya está creada. Para añadir elementos a la lista utiliza el comando \"/lista " + listName + " agregar [Elemento]\".";
            users.put(chatId, lists);
            saveToFile(users);
        }
        fileInUse = false;
        return response;
    }

    private String eliminateList() {
        String response;
        while (fileInUse);
        fileInUse = true;
        JSONObject users = getFileContent();
        if (!users.containsKey(chatId)) {
            users.put(chatId, new JSONObject());
        }
        JSONObject lists = (JSONObject) users.get(chatId);
        if (lists.containsKey(listName)) {
            lists.remove(listName);
            if (lists.isEmpty()) {
                users.remove(chatId);
            }
            saveToFile(users);
            response = "Lista eliminada!";
        } else {
            response = "Oh, lo siento, no he encontrado ninguna lista con ese nombre!";
        }
        fileInUse = false;
        return response;
    }

    private String showAllLists() {
        String response;
        while (fileInUse);
        fileInUse = true;
        JSONObject users = getFileContent();
        fileInUse = false;
        if (!users.containsKey(chatId)) {
            response = "No tienes ninguna lista!";
        } else {
            JSONObject lists = (JSONObject) users.get(chatId);
            response = "Estas son las listas que has creado:\n";
            Set<String> createdLists = lists.keySet();
            for (String list : createdLists) {
                response += "- " + list + "\n";
            }
        }
        return response;
    }

    private String emptyList() {
        String response;
        while(fileInUse);
        fileInUse = true;
        JSONObject users = getFileContent();
        if (!users.containsKey(chatId)) {
            users.put(chatId, new JSONObject());
        }
        JSONObject lists = (JSONObject) users.get(chatId);
        if (lists.containsKey(listName)) {
            JSONObject userList = (JSONObject) lists.get(listName);
            userList.clear();
            userList.put(numberOfElements, 0);
            lists.put(listName, userList);
            users.put(chatId, lists);
            saveToFile(users);
            response = "Lista vaciada!";
        } else {
            response = "Oh, lo siento, no he encontrado ninguna lista con ese nombre!";
        }
        fileInUse = false;
        return response;
    }

    private String showList() {
        String response;
        while (fileInUse);
        fileInUse = true;
        JSONObject users = getFileContent();
        fileInUse = false;
        if (!users.containsKey(chatId)) {
            response = "No tienes ninguna lista!";
        } else {
            JSONObject lists = (JSONObject) users.get(chatId);
            if (lists.containsKey(listName)) {
                JSONObject userList = (JSONObject) lists.get(listName);
                response = "Este es el contenido de la lista " + listName + ":\n";
                Set<String> listValues = userList.keySet();
                for (String key : listValues) {
                    if (!key.equals(numberOfElements)) {
                        response += "- " + userList.get(key) + "\n";
                    }
                }
            } else {
                response = "No tienes ninguna lista con ese nombre!";
            }
        }
        return response;
    }

    private String addElement() {
        String response;
        while (fileInUse);
        fileInUse = true;
        JSONObject users = getFileContent();
        if (!users.containsKey(chatId)) {
            response = "No tienes ninguna lista!";
        } else {
            JSONObject lists = (JSONObject) users.get(chatId);
            if (lists.containsKey(listName)) {
                JSONObject userList = (JSONObject) lists.get(listName);
                long elements = (long) userList.get(numberOfElements);
                userList.put(elem + elements, elemName);
                elements++;
                userList.put(numberOfElements, elements);
                lists.put(listName, userList);
                users.put(chatId, lists);
                saveToFile(users);
                response = "Elemento agregado a la lista " + listName;
            } else {
                response = "No tienes ninguna lista con ese nombre!";
            }
        }
        fileInUse = false;
        return response;
    }

    private String deleteElement() {
        String response;
        while (fileInUse);
        fileInUse = true;
        JSONObject users = getFileContent();
        if (!users.containsKey(chatId)) {
            response = "No tienes ninguna lista!";
        } else {
            JSONObject lists = (JSONObject) users.get(chatId);
            if (lists.containsKey(listName)) {
                JSONObject userList = (JSONObject) lists.get(listName);
                if (listContainsElement(userList)) {
                    Set<String> keys = userList.keySet();
                    ArrayList<String> values = new ArrayList<>();
                    for (String key : keys) {
                        if (!key.equals(numberOfElements)) {
                            values.add((String) userList.get(key));
                        }
                    }
                    long elements = (long) userList.get(numberOfElements);
                    elements--;
                    userList.clear();
                    userList.put(numberOfElements, elements);
                    int x = 0;
                    for (String value : values) {
                        if (!value.equals(elemName)) {
                            userList.put(elem + x, value);
                            x++;
                        }
                    }
                    lists.put(listName, userList);
                    users.put(chatId, lists);
                    saveToFile(users);
                    response = "Elemento eliminado de la lista " + listName;
                } else {
                    response = "No hay ningún elemento \"" + elemName + "\" en la lista " + listName + "!";
                }
            } else {
                response = "No tienes ninguna lista con ese nombre!";
            }
        }
        fileInUse = false;
        return response;
    }

    private JSONObject getFileContent() {
        createFileIfNotExisting();
        String content = "";
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(UsersListsFile));
            String line = reader.readLine();
            while (line != null) {
                content += line;
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            logError("ListService: An error occured while reading the users lists file: " + e.getMessage());
        }
        JSONParser parser = new JSONParser();
        JSONObject object = null;
        try {
            object = (JSONObject) parser.parse(content);
        } catch (ParseException ex) {
            object = new JSONObject();
            logError("ListService: An error occured while parsing the users lists file: " + ex.getMessage());
        }
        return object;
    }

    private void saveToFile(JSONObject users) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(UsersListsFile);
            out.println(users.toJSONString());
        } catch (FileNotFoundException ex) {
            logError("ListService: UsersList file not found: " + ex.getMessage());
        } finally {
            out.close();
        }
    }

    private void createFileIfNotExisting() {
        File file = new File(UsersListsFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                logError("ListService: Could not create UsersLists file: " + ex.getMessage());
            }
        }
    }

    private boolean listContainsElement(JSONObject userList) {
        boolean isInList = false;
        Set<String> keys = userList.keySet();
        for (String key : keys) {
            if (userList.get(key).equals(elemName)) {
                isInList = true;
            }
        }
        return isInList;
    }
}
