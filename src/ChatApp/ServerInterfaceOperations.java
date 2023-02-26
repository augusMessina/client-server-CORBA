package ChatApp;


/**
* ChatApp/ServerInterfaceOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from MyInterfaces.idl
* Sunday, February 26, 2023 2:47:41 AM CET
*/

public interface ServerInterfaceOperations 
{
  String connection (String userName, String password);
  String signUp (String userName, String password);
  void newMessages (String roomName, String message);
  String getMessages (String roomName);
  String listUsers (String roomName);
  String listRooms ();
  String createNewRooms (String roomName);
  String joinRoom (String roomToJoin, String name);
  String leaveRoom (String roomToLeave, String name);
  void disconnect (String userName, String roomName);
} // interface ServerInterfaceOperations
