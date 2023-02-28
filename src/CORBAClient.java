//AUTHOR: HABIB ADO SABIU

import ChatApp.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.Scanner;
import java.io.*;
import java.util.regex.Pattern;

public class CORBAClient {

    static ServerInterface serverInterfaceImpl;

    public static String lastMessage = "";
    public static String userName = "";
    public static String connectedRoom = "";
    public static String strResponse = "";

    public static void main(String args[]) {

        try {
            // crea e inicializa un objeto ORB, que se conectará con el servidor
            ORB orb = ORB.init(args, null);

            // se extrae la raiz del ORB y se guarda en una referencia mediante
            // NamingContext

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // se extrae la implementación del ORB usando el mismo nombre
            // que aquel con el que se guardó en el servidor ("Hello")
            String name = "ServerInterface";
            serverInterfaceImpl = ServerInterfaceHelper.narrow(ncRef.resolve_str(name));

            // muestra el objeto recibido por terminal
            System.out.println("Obtained a handle on server object: " + serverInterfaceImpl);

            // se crea un BufferedReader en el que se irá guardando lo que el usuario
            // escriba por consola
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            // pide al usuario que introduzca "login" o "signup".
            System.out.println("");
            System.out.print("Enter 'login' for logging in or 'signup' for signing up: ");
            String optInput = br.readLine();

            String nameInput = "";
            String passwordInput = "";

            // --------------------------------------------------
            // Si introduce login, pide al usuario un nombre y una contraseña, para
            // posteriormente llamar al método connection()
            // --------------------------------------------------
            if (optInput.equals("login")) {
                System.out.print("Enter your name : ");
                nameInput = br.readLine();
                System.out.print("Enter your password : ");
                passwordInput = br.readLine();
                System.out.println("");

                // --------------------------------------------------
                // Si introduce login, pide al usuario un nombre y una contraseña, para
                // posteriormente llamar al método signUp() y seguidamente a connection()
                // --------------------------------------------------
            } else if (optInput.equals("signup")) {
                System.out.print("Enter a name : ");
                nameInput = br.readLine();
                System.out.print("Enter a password : ");
                passwordInput = br.readLine();
                System.out.println("");

                String signupResponse = serverInterfaceImpl.signUp(nameInput, passwordInput);
                if (signupResponse.equals("failure")) {
                    System.out.println("Please choose a different name\n");
                    System.exit(0);
                } else if (signupResponse.equals("invalid")) {
                    System.out.println("Error: names and passwords cant contain '|'\n");
                    System.exit(0);
                }

            } else {
                System.out.println("Invalid input");
                System.exit(0);
            }

            // se gestionan todas las posibles respuestas del serivdor ("failure",
            // "invalid",...)
            String connectionResponse = serverInterfaceImpl.connection(nameInput, passwordInput);
            if (connectionResponse.equals("failure")) {
                System.out.println("Incorrect user name or password\n");
            } else {
                connectedRoom = "general";
                strResponse = connectionResponse;
                userName = nameInput;

                String TimeStamp = new java.util.Date().toString();
                String connectedTime = "Connected on " + TimeStamp;

                System.out.println(connectedTime);
                System.out.println("");

                // si el login es exitoso, muestra todos los mensajes enviados en el chat
                // general
                String[] strResponseParts = strResponse.split(Pattern.quote("|"));
                for (int i = 1; i < strResponseParts.length - 1; i++) {
                    String[] strArr = strResponseParts[i].split(" ", 2);
                    System.out.println(strArr[1] + "\n");
                }

                // thread para recibir el último mensaje enviado, se ejecuta cada 500ms y solo
                // se muestra y se guarda el mensaje recibido solo si el mensaje no es el mismo
                // que el último recibido y si el mensaje no ha sido enviado por el propio
                // usuario
                Thread receivingMessages = new Thread(new Runnable() {
                    public void run() {
                        while (true) {
                            String serverResponse = serverInterfaceImpl.getMessages(connectedRoom);
                            if (!(serverResponse.equals(lastMessage)) && !(serverResponse.equals(""))
                                    && !(serverResponse.startsWith("@" + userName))) {
                                lastMessage = serverResponse;
                                System.out.println(serverResponse + "\n");
                            }

                            try {
                                Thread.sleep(500);
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        }
                    }
                });

                receivingMessages.start();

                // aqui comienza un while(true) en el que el cliente ejecuta distintos métodos
                // en función de lo que el usuario introduzca por consola
                while (true) {

                    String input = br.readLine();
                    System.out.println("");

                    // si el usuario introdue "/exit", llama al método disconnect() y finaliza la
                    // ejecución del código
                    if (input.equals("/exit")) {
                        serverInterfaceImpl.disconnect(userName, connectedRoom);
                        System.exit(0);

                        // si el usuario introduce "/join {roomName}" separa la línea en un array
                        // ["/join", "roomName"] y llama al método joinRoom(), pasando por parámetro el
                        // nombre del chat room introducido y el nombre del usuario.

                    } else if (input.startsWith("/join")) {

                        String[] inputParts = input.split(Pattern.quote(" "));
                        if (inputParts.length == 1) {
                            System.out.println("Invalid command. You must include a room name\n");
                        } else {
                            if (inputParts[1].equalsIgnoreCase(connectedRoom)) {
                                System.out.println("You are already in this room\n");
                            } else {
                                String response = serverInterfaceImpl.joinRoom(inputParts[1], userName);
                                String[] responseParts = response.split(Pattern.quote("|"));

                                // --------------------------------------------------
                                // Si la respuesta es "joined", muestra todos los mensajes que se han enviado en
                                // el nuevo chat room y se actualiza el valor de connectedRoom
                                // --------------------------------------------------

                                if (response.startsWith("joined")) {

                                    String leave = serverInterfaceImpl.leaveRoom(connectedRoom, userName);

                                    connectedRoom = inputParts[1];
                                    System.out.println("You have joined " + connectedRoom + " room\n");

                                    for (int i = 1; i < responseParts.length - 1; i++) {
                                        String[] arr = responseParts[i].split(" ", 2);
                                        System.out.println(arr[1] + "\n");
                                    }

                                    // --------------------------------------------------
                                    // Si la respuesta es "no-room", muestra un mensaje de error
                                    // --------------------------------------------------

                                } else if (response.equals("no-room")) {
                                    System.out.println(inputParts[1] + " room not found\n");

                                    // --------------------------------------------------
                                    // Si la respuesta es "is-private", pide una contraseña y llama a
                                    // joinPrivateRoom()
                                    // --------------------------------------------------

                                } else if (response.equals("is-private")) {
                                    System.out.println("This room is private, enter the password to join : ");
                                    String roomPasswordInput = br.readLine();
                                    String privateResponse = serverInterfaceImpl.joinPrivateRoom(inputParts[1],
                                            roomPasswordInput, userName);
                                    String[] privateResponseParts = response.split(Pattern.quote("|"));

                                    if (privateResponse.startsWith("joined")) {

                                        String leave = serverInterfaceImpl.leaveRoom(connectedRoom, userName);

                                        connectedRoom = inputParts[1];
                                        System.out.println("You have joined " + connectedRoom + " room\n");

                                        for (int i = 1; i < privateResponseParts.length - 1; i++) {
                                            String[] arr = privateResponseParts[i].split(" ", 2);
                                            System.out.println(arr[1] + "\n");
                                        }

                                    } else if (privateResponse.equals("no-room")) {
                                        System.out.println(inputParts[1] + " room not found or incorrect password\n");

                                    }

                                } else {
                                    System.out.println("Invalid command room\n");
                                }
                            }

                        }

                        // si el usuario introduce "/leave {roomName}" separa la línea en un array
                        // ["/join", "roomName"] y llama al método leaveRoom(), pasando por parámetro el
                        // nombre del chat room introducido y el nombre del usuario.

                    } else if (input.startsWith("/leave")) {
                        String[] inputParts = input.split(Pattern.quote(" "));
                        if (inputParts.length == 1) {
                            System.out.println("Invalid command. You must include a room name\n");
                        } else {
                            if (inputParts[1].equals("general")) {
                                System.out.println("You cant leave general room\n");
                            } else {
                                String response = serverInterfaceImpl.leaveRoom(inputParts[1], userName);
                                if (response.equals("no-room")) {
                                    System.out.println(inputParts[1] + " room not found\n");
                                } else if (response.equals("no-user")) {
                                    System.out.println("You are not in " + inputParts[1] + " room\n");
                                } else if (response.equals("leave-success")) {
                                    connectedRoom = "general";
                                    System.out.println("You have left " + inputParts[1] + " room\n");
                                } else {
                                    System.out.println("Invalid command room\n");
                                }
                            }

                        }

                        // si el usuario introduce "/users" llama al método listUsers() y muestra la
                        // respuesta

                    } else if (input.startsWith("/users")) {
                        String returnValue = serverInterfaceImpl.listUsers(connectedRoom);
                        String[] returnValueParts = returnValue.split(Pattern.quote(" "));

                        System.out.println("*** All Active Users ***\n");
                        for (int i = 0; i < returnValueParts.length; i++) {
                            System.out.println((i + 1) + ". " + returnValueParts[i] + "\n");
                        }
                        System.out.println("*******************\n");

                        // si el usuario introduce "/rooms" llama al método listRooms() y muestra la
                        // respuesta

                    } else if (input.startsWith("/rooms")) {
                        String returnValue = serverInterfaceImpl.listRooms();
                        String[] returnValueParts = returnValue.split(Pattern.quote(" "));

                        System.out.println("*** Rooms ***\n");
                        for (int i = 0; i < returnValueParts.length; i++) {
                            System.out.println((i + 1) + ". " + returnValueParts[i] + "\n");
                        }
                        System.out.println("*******************\n");

                        // si el usuario introduce "/currentRoom" muestra por pantalla el contenido de
                        // connectedRoom

                    } else if (input.equals("/currentRoom")) {
                        System.out.println("You are currently in " + connectedRoom + "\n");

                        // si el usuario introduce "/help" muestra por pantalla una lista con todos los
                        // posibles comandos funcionales

                    } else if (input.startsWith("/help")) {
                        System.out.println("* This is a list of the commands you can use:");
                        System.out.println(
                                "*    /users -------------------------------------- get a list of the current logged in users       *");
                        System.out.println(
                                "*    /rooms -------------------------------------- get a list of the existing rooms                *");
                        System.out.println(
                                "*    /create {room-name} ------------------------- create a public chatroom                        *");
                        System.out.println(
                                "*    /createPrivate {room-name} {room-password} -- create a private chatroom                       *");
                        System.out.println(
                                "*    /join {room-name} --------------------------- join a chatroom                                 *");
                        System.out.println(
                                "*    /leave {room-name} -------------------------- leave a chatroom and join the general chatroom  *");
                        System.out.println(
                                "*    /exit --------------------------------------- disconnect from the chat (log out)              *\n");

                        // si el usuario introduce "/createPrivate {roomName} {roomPassword}" llama al
                        // método createPrivateRoom() y pasa como parámetros el nombre del chat y la
                        // contraseña introducidos.

                    } else if (input.startsWith("/createPrivate")) {
                        String[] inputParts = input.split(Pattern.quote(" "));
                        if (inputParts.length < 3) {
                            System.out.println("Invalid command. You must include a room name and a password\n");
                        } else {
                            String response = serverInterfaceImpl.createNewPrivateRooms(inputParts[1], inputParts[2]);
                            if (response.equals("invalid")) {
                                System.out.println("Error: rooms name cant contain '|'\n");
                            } else if (response.equals("exist")) {
                                System.out.println(inputParts[1] + " room already exists\n");
                            } else if (response.equals("created")) {
                                System.out.println(inputParts[1] + " private room was created\n");
                                System.out.println("Room password: " + inputParts[2] + "\n");
                            }
                        }

                        // si el usuario introduce "/createPrivate {roomName}" llama al
                        // método createRoom() y pasa como parámetro el nombre del chat

                    } else if (input.startsWith("/create")) {
                        String[] inputParts = input.split(Pattern.quote(" "));
                        if (inputParts.length == 1) {
                            System.out.println("Invalid command. You must include a room name\n");
                        } else {
                            String response = serverInterfaceImpl.createNewRooms(inputParts[1]);
                            if (response.equals("invalid")) {
                                System.out.println("Error: rooms name cant contain '|'\n");
                            } else if (response.equals("exist")) {
                                System.out.println(inputParts[1] + " room already exists\n");
                            } else if (response.equals("created")) {
                                System.out.println(inputParts[1] + " room was created\n");
                            }
                        }

                        // si el usuario introduce "/" y el comando no corresponde con ninguno de los
                        // mencionados hasta ahora, muestra un mensaje de ayuda

                    } else if (input.startsWith("/")) {
                        System.out.println("Command does not exist. You can see the list of commands with /help\n");

                        // para cualquier otro input, el cliente considerará que es un mensaje a enviar
                        // y llamará al método newMessages(), pasándo como parámetros connectedRoom y el
                        // input introducido junto con el nombre del usuario guardado

                    } else {
                        serverInterfaceImpl.newMessages(connectedRoom, "@" + userName + ":" + input);

                    }
                }
            }

            // si ocurre algún error, el cliente se desconecta y se finaliza la ejecución
            // del código

        } catch (

        Exception e) {
            System.out.println("ERROR : " + e);
            e.printStackTrace(System.out);
            serverInterfaceImpl.disconnect(userName, connectedRoom);
            System.exit(0);
        }
    }
}
