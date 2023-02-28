
//AUTHOR: HABIB ADO SABIU
//EDITTED BY: AUGUSTO MESSINA

import ChatApp.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.io.Console;
import java.util.*;
import java.util.regex.Pattern;

class ServerInterfaceImpl extends ServerInterfacePOA {

	// array en el que se almacenan los mensajes
	static List<String> messageLogs = new ArrayList<>();

	// array en el que se almacenan los nombres de los usuarios junto con el chat
	// room en el que están actualmente
	static List<String> roomUsers = new ArrayList<>();

	// array en el que se almacenan los usuarios activos
	private static List<String> activeUsers = new ArrayList<>();

	// array en el que se almacenan todos los usuarios junto con sus contraseñas
	private static List<String> registeredUsers = new ArrayList<>();

	// array en el que se almacenan los nombres de los chat room, estando por
	// defecto el chat general
	private static List<String> rooms = new ArrayList<String>() {
		{
			add("general");
		}
	};

	// array en el que se almacenan todos los chat rooms privados junto con sus
	// contraseñas
	private static List<String> privateRooms = new ArrayList<String>();

	// se crea el objeto ORB y se inicializa
	private ORB orb;

	public void setORB(ORB orb_val) {
		orb = orb_val;
	}

	// primero comprueba registeredUsers en busca de (userName|password), y en caso
	// de no encontrarlo devuelve "failure". Si lo encuentra, añade userName a
	// activeUsers y crea un mensaje informativo en el chat general. Finalmente
	// decuelve todos los mensajes de dicho chat
	public String connection(String userName, String password) {

		StringBuilder sb = new StringBuilder();

		if (!registeredUsers.contains(userName.toLowerCase() + "|" + password)) {
			sb.append("failure");
		} else {
			String TimeStamp = new java.util.Date().toString();
			String connectedTime = "Connected on " + TimeStamp;
			activeUsers.add(userName.toLowerCase());
			roomUsers.add("general " + userName);
			messageLogs.add("general @" + userName + " " + connectedTime);

			sb.append("joined");

			for (int i = 0; i < messageLogs.size(); i++) {
				if (messageLogs.get(i).startsWith("general")) {
					sb.append("|" + messageLogs.get(i));
				}
			}
		}

		return sb.toString();
	}

	// *****************************************
	// Método añadido por el alumno
	// *****************************************

	// primero comprueba si el nombre de usuario y la contraseña contienen "|". Esta
	// comprobación se hace porque dicho carácter está reservado para la separación
	// entre usuario y contraseña. Si contienen el carácter, devuelve "invalid". En
	// caso contrario, comprueba si el nombre de usuario ya está en uso, y si esto
	// se cumple devuelve "failure". Finalmente, si pasa los filtros, añade el
	// usuario y la contraseña a registeredUsers y devuelve success
	public String signUp(String userName, String password) {

		StringBuilder sb = new StringBuilder();

		if (userName.contains("|") || password.contains("|")) {
			sb.append("invalid");
		} else {
			for (int i = 0; i < registeredUsers.size(); i++) {
				String[] splitReg = registeredUsers.get(i).split("|");
				if (splitReg[0].equals(userName.toLowerCase())) {
					sb.append("failure");
				}
			}

			if (sb.length() == 0) {
				registeredUsers.add(userName.toLowerCase() + "|" + password);
				sb.append("success");
			}
		}

		return sb.toString();
	}

	// añade a messageLogs un nuevo mensaje
	public void newMessages(String roomName, String message) {
		messageLogs.add(roomName + " " + message);
	}

	// devuelve el último mensaje que se haya introducido en messageLogs. Si este
	// mensaje no corresponde con el chat room pasado por parámetro, devulve un
	// string vacío
	public String getMessages(String roomName) {
		String valueToReturn = "";

		String message = messageLogs.get(messageLogs.size() - 1);

		if (message.startsWith(roomName)) {
			valueToReturn = message.substring(message.indexOf(" ") + 1);
		}

		return valueToReturn;
	}

	// devuelve todos los usuarios de activeUsers separados por un espacio
	public String listUsers(String roomName) {

		StringBuilder sb = new StringBuilder();

		for (String s : activeUsers) {
			sb.append(s);
			sb.append(" ");
		}

		return sb.toString();
	}

	// devuelve todos los chat rooms de rooms separados por un espacio. Además, si
	// el chat room es privado añade "(privado)"
	public String listRooms() {
		StringBuilder sb = new StringBuilder();

		for (String s : rooms) {
			sb.append(s);
			for (int i = 0; i < privateRooms.size(); i++) {
				if (privateRooms.get(i).startsWith(s)) {
					sb.append("(private)");
				}
			}
			sb.append(" ");
		}

		return sb.toString();
	}

	// añade un nuevo chat room a rooms, comprobando si dicho chat room no existe
	// previamente
	public String createNewRooms(String roomName) {
		String response = "";

		if (roomName.contains("|")) {
			response = "invalid";
		} else if (rooms.contains(roomName)) {
			response = "exist";
		} else {
			rooms.add(roomName);
			response = "created";
		}

		return response;
	}

	// *****************************************
	// Método añadido por el alumno
	// *****************************************

	// crea un nuevo chat room privado, comprobando antes si dicho chat room no
	// existe previamente. Añade el nombre del chat room a rooms, y el nombre junto
	// con la contrasela a privateRooms
	public String createNewPrivateRooms(String roomName, String password) {
		String response = "";

		if (roomName.contains("|") || password.contains("|")) {
			response = "invalid";
		} else if (rooms.contains(roomName)) {
			response = "exist";
		} else {
			rooms.add(roomName);
			privateRooms.add(roomName + "|" + password);
			response = "created";
		}

		return response;
	}

	// permite al usuario unirse a un chat room. Si el chat room no existe devuelve
	// "no-room", si existe pero es privado devuelve "is-private", y si pasa los
	// filtros añade a roomUsers el chat room junto con el usuario y manda un
	// mensaje informativo, además de devolver todos los mensajes de dicho chat
	public String joinRoom(String roomToJoin, String name) {
		StringBuilder response = new StringBuilder();

		Boolean is_private = false;
		for (int i = 0; i < privateRooms.size(); i++) {
			if (privateRooms.get(i).startsWith(roomToJoin)) {
				is_private = true;
			}
		}

		if (!rooms.contains(roomToJoin)) {
			response.append("no-room");
		} else if (is_private) {
			response.append("is-private");
		} else {
			roomUsers.add(roomToJoin + " " + name);
			messageLogs.add(roomToJoin + " " + name + " has joined");
			response.append("joined");

			for (int i = 0; i < messageLogs.size(); i++) {
				if (messageLogs.get(i).startsWith(roomToJoin)) {
					response.append("|" + messageLogs.get(i));
				}
			}
		}

		return response.toString();
	}

	// *****************************************
	// Método añadido por el alumno
	// *****************************************

	// permite al usuario unirse a un chat privado. Si la combinación de chat room y
	// contraseña no existe, devuelve "no-rooms". En caso contrario, añade a
	// roomUsers el chat room junto con el usuario y manda un
	// mensaje informativo, además de devolver todos los mensajes de dicho chat
	public String joinPrivateRoom(String roomToJoin, String password, String name) {
		StringBuilder response = new StringBuilder();

		Boolean no_room = true;
		for (int i = 0; i < privateRooms.size(); i++) {
			if (privateRooms.get(i).contains(roomToJoin + "|" + password)) {
				no_room = false;
			}
		}

		if (no_room) {
			response.append("no-room");
		} else {
			roomUsers.add(roomToJoin + " " + name);
			messageLogs.add(roomToJoin + " " + name + " has joined");
			response.append("joined");

			for (int i = 0; i < messageLogs.size(); i++) {
				if (messageLogs.get(i).startsWith(roomToJoin)) {
					response.append("|" + messageLogs.get(i));
				}
			}
		}

		return response.toString();
	}

	// permite al usuario abandonar un chat room. Si el chat introducido no existe
	// devuelve "no-room". Si el chat existe pero el usuario no está en dicho chat,
	// devuelve "no-user". Si pasa los filtros, se elimina el chat room y el usuario
	// de roomUsers, se manda un mensaje informativo y devuelve "leave-success"
	public String leaveRoom(String roomToLeave, String name) {
		String response = "";

		if (!rooms.contains(roomToLeave)) {
			response = "no-room";
		} else if (!roomUsers.contains(roomToLeave + " " + name)) {
			response = "no-user";
		} else {
			roomUsers.remove(roomToLeave + " " + name);
			messageLogs.add(roomToLeave + " " + name + " has left");
			response = "leave-success";
		}

		return response;
	}

	// permite al usuario desconectarse de la aplicación. Se elimina de los usuarios
	// activos pero se mantiene en registeredUsers por si quiere volver bajo el
	// mismo nombre
	public void disconnect(String userName, String roomName) {

		activeUsers.remove(userName);

		messageLogs.add(roomName + " " + userName + " has disconnected");
	}

}

public class CORBAServer {

	public static void main(String args[]) {

		try {
			// setup del servidor
			ORB orb = ORB.init(args, null);

			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			ServerInterfaceImpl serverInterfaceImpl = new ServerInterfaceImpl();
			serverInterfaceImpl.setORB(orb);

			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(serverInterfaceImpl);
			ServerInterface href = ServerInterfaceHelper.narrow(ref);

			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");

			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			String name = "ServerInterface";
			NameComponent path[] = ncRef.to_name(name);
			ncRef.rebind(path, href);

			System.out.println("Server running, accepting client connection...");

			orb.run();
		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
	}
}