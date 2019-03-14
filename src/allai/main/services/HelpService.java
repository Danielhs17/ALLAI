/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.main.services;

import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class HelpService extends Service {

    private String error = "[X] No he entendido en qué necesitas mi ayuda. Por favor, escribe /ayuda para información general y una lista de los servicios disponibles o /ayuda [servicio] para información específica";

    private enum Service {
        GENERAL, PARKING, BUS, SEARCH, JOKE, YOUTUBE, WIKI, TRANSLATE, LISTS, WEATHER, REMINDER, QUIETMODE, ERROR
    }
    private Service service;

    public HelpService(String[] arg) {
        logInfo("HelpService: Initiated");
        int arguments = arg.length;
        if (arguments != 2 && arguments != 1) {
            service = Service.ERROR;
        } else {
            if (arguments == 1) {
                service = Service.GENERAL;
            } else {
                if (arg[1].equals("bus")) {
                    service = Service.BUS;
                } else if (arg[1].equals("parking")) {
                    service = Service.PARKING;
                } else if (arg[1].equals("google")) {
                    service = Service.SEARCH;
                } else if (arg[1].equals("chiste")) {
                    service = Service.JOKE;
                } else if (arg[1].equals("youtube")) {
                    service = Service.YOUTUBE;
                } else if (arg[1].equals("wiki")) {
                    service = Service.WIKI;
                } else if (arg[1].equals("traducir")) {
                    service = Service.TRANSLATE;
                } else if (arg[1].equals("lista")) {
                    service = Service.LISTS;
                } else if (arg[1].equals("clima")) {
                    service = Service.WEATHER;
                } else if (arg[1].equals("recordar")) {
                    service = Service.REMINDER;
                } else if (arg[1].equals("callar")) {
                    service = Service.QUIETMODE;
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
            case GENERAL:
                response = generalHelp();
                break;
            case PARKING:
                response = parkingHelp();
                break;
            case BUS:
                response = busHelp();
                break;
            case SEARCH:
                response = searchHelp();
                break;
            case JOKE:
                response = jokeHelp();
                break;
            case YOUTUBE:
                response = youtubeHelp();
                break;
            case WIKI:
                response = wikiHelp();
                break;
            case TRANSLATE:
                response = translateHelp();
                break;
            case LISTS:
                response = listHelp();
                break;
            case WEATHER:
                response = weatherHelp();
                break;
            case REMINDER:
                response = reminderHelp();
                break;
            case QUIETMODE:
                response = quietHelp();
                break;
            case ERROR:
                response = error;
                break;
        }
        return response;
    }

    private String generalHelp() {
        return "Soy ALLAI, y puedes usarme de asistente personal, o solo para conversar! \n\n"
                + "Para conversar conmigo puedes escribirme lo que quieras. O puedes enviarme notas de voz, ¡las entiendo sin problema!\n"
                + "Puedes preguntarme cosas como \"qué es un unicornio\", \"quién es Cristiano Ronaldo\" o \"dónde puedo aparcar\", o puedes enviarme comandos para pedirme que haga cosas por tí.\n\n"
                + "Si quieres ayuda con los comandos, o las frases que puedes decir para que te ayude, solo escribe /ayuda [servicio] (ejemplo: /ayuda bus) y te ayudaré!\n\n"
                + "Lista de servicios: \n"
                + "- Búsqueda de Google (/ayuda google)\n"
                + "- EMT Málaga (/ayuda bus)\n"
                + "- Aparcamiento Málaga (/ayuda parking)\n"
                + "- Chistes (/ayuda chiste)\n"
                + "- YouTube (/ayuda youtube)\n"
                + "- Wikipedia (/ayuda wiki)\n"
                + "- Traducir (/ayuda traducir)\n"
                + "- Listas (/ayuda lista)\n"
                + "- Clima (/ayuda clima)\n"
                + "- Recordatorios (/ayuda recordar)\n"
                + "- Quiet Mode (/ayuda callar)";
    }

    private String parkingHelp() {
        return "Con el comando /parking, o con la frase \"dónde puedo aparcar\" podrás conocer la ocupación de todos los aparcamientos municipales de Málaga en tiempo real\n"
                + "Te mostraré una lista de cada parking seguida de dos números (libres/totales)\n\n"
                + "Ejemplo:\n "
                + "Cruz de Humilladero: 53/100\n"
                + "Esto quiere decir que en el parking de Cruz de Humilladero hay 53 plazas libres de 100.";
    }

    private String busHelp() {
        return "Con este servicio podrás disponer de toda la información de los buses de la EMT sin descargar nada.\n\n"
                + "Comandos disponibles:\n"
                + "- /bus parada [#parada]\n"
                + "- /bus linea [#linea]\n"
                + "- Cuánto le falta al bus [#parada]\n"
                + "- Parada [#parada]\n\n"
                + "Ejemplos:\n"
                + "- /bus parada 1533\n"
                + "- /bus linea 15\n"
                + "- Cuánto le falta al bus 1553\n";
    }

    private String searchHelp() {
        return "Utiliza el comando /google [palabras clave], o las frases \"busca en google [palabras clave]\" y \"googlea [palabras clave]\" para buscar lo que quieras en Google.\n\n"
                + "Ejemplos:\n"
                + "- /google Paella\n"
                + "- /google Cómo ser un dragón\n"
                + "- Busca en google cómo adelgazar\n"
                + "- Googlea Cómo aprobar cálculo\n";
    }

    private String jokeHelp() {
        return "Oh, ¡este servicio es para pedirme un chiste! Venga, inténtalo, ¡soy muy gracioso! Prueba el comando /chiste o la frase \"cuéntame un chiste\"";
    }
    
    
    private String youtubeHelp() {
        return "Utiliza el comando /youtube [palabras clave] o las frases \"buscar video [palabras clave]\" y \"busca el video [palabras clave]\" para buscar lo que quieras en YouTube.\n\n"
                + "Ejemplos:\n"
                + "- /youtube Rubius\n"
                + "- /youtube Música de los 80\n"
                + "- Buscar video Somebody to Love\n";
    }
    
    private String wikiHelp() {
        return "Utiliza el comando /wiki [palabras clave] para preguntarme por algo o alguien, o dime frases como \"qué es (un/una) [algo]\" o \"quién es [alguien]\".\n\n"
                + "Ejemplos:\n"
                + "- /wiki microcontrolador\n"
                + "- /wiki Cristiano Ronaldo\n"
                + "- Qué es Roma\n"
                + "- Quién es Napoléon Bonaparte";
    }
    
    private String translateHelp() {
        return ("¡Soy capaz de traducir cualquier texto a 92 idiomas! Solo tienes que usar el comando \"traducir [idioma] [texto]\" o decir \"traduce a [idioma] [texto]\".\n\n"
                + "Ejemplos:\n"
                + "- /traducir ingles Mi nombre es Federico\n"
                + "- /traducir español Hello, I'm terminator... I'll be back\n"
                + "- Traduce a japonés ¿Dónde está el baño?\n"
                + "\n"
                + "Powered by Yandex.Translate"); 
    }
    
    private String listHelp(){
        return "Con este servicio puedes crear, modificar y eliminar listas (como la lista de la compra) para que nunca se te olvide nada!\n\n"
                + ""
                + "Dato importante: Los nombres de las listas no pueden contener espacios!\n"
                + "Dato importante 2: Este servicio solo está disponible si hablas conmigo a través de Telegram.\n\n"
                + ""
                + "Comandos disponibles:\n"
                + "- /lista crear [nombre]\n"
                + "- /lista [nombre] agregar [objeto]\n"
                + "- /lista [nombre] quitar [objeto]\n"
                + "- /lista eliminar [nombre]\n"
                + "- /lista vaciar [nombre]\n"
                + "- /lista mostrar [nombre]\n"
                + "- /lista todas\n\n"
                + ""
                + "También puedes consultar, crear y eliminar listas con estas frases:\n"
                + "- Crear lista [nombre]\n"
                + "- Eliminar lista [nombre]\n"
                + "- Vaciar lista [nombre]\n"
                + "- Mostrar lista [nombre]\n"
                + "- Mostrar listas (para consultar todas tus listas)";
    }
    
    private String weatherHelp() {
        return "Puedes pedirme información sobre el clima de cualquier parte del mundo, aunque como vivo en Málaga, si no me dices una ciudad, te diré la de málaga!\n"
                + "Este servicio solo está disponible si hablas conmigo a través de Telegram\n\n"
                + "Ejemplos:\n"
                + "- /clima (Devuelve el clima en Málaga)\n"
                + "- /clima Fuengirola (Devuelve el clima en Fuengirola)\n"
                + "- Clima (Devuelve el clima en Málaga)\n"
                + "- Clima en Fuengirola (Devuelve el clima en Fuengirola)";
    }
    
    private String reminderHelp() {
        return "Puedo recordarte lo que quieras. Solo tienes que utilizar el comando \"/recordar [DD-MM-AAAA] [hh:mm] [Mensaje]\" y te enviaré el mensaje que me digas cuando llegue ese momento!\n"
                + "Escribe la hora en formato de 24 horas.\n\n"
                + ""
                + "Ejemplos:\n"
                + "- /recordar 15-05-2019 16:00 Buscar justificante médico\n"
                + "- Recuérdame 08/09/2019 00:00 Aniversario";
    }

    private String quietHelp() {
        return "El quiet mode es una forma educada de mandarme a callar. Es perfecto para meterme en grupos con muchas personas, así no estaré contestando a todas ellas sino únicamente a los comandos.\n"
                + "\n"
                + "Para activar el quiet mode lo único que tienes que hacer es usar el comando /callar, o decirme \"Allai, cállate\".\n"
                + "Si quieres que vuelva a hablar utiliza el comando /hablar, o dime \"Allai, habla\".";
    }
}
