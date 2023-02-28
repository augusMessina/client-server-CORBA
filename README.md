## Introduction

This is a client-server chat application implemented using Java and CORBA.

### Compiling and Testing

From the project root directory, open the terminal and run the following comands:

    mkdir bin
    idlj -td src/ -fall MyInterfaces.idl
    javac src/*.java src/ChatApp/*.java -d bin/
    orbd -ORBInitialPort 1050 -ORBInitialHost localhost

Start the server by running the following command:

    java -cp bin/ CORBAServer -ORBInitialPort 1050 -ORBInitialHost localhost

After the server started successfully, create multiple clients using the following command:

    java -cp bin/ CORBAClient -ORBInitialPort 1050 -ORBInitialHost localhost

Before a client can connect to the web server, the client has to provide a user name to the server. The server then checks to make sure no any other connected client is using the same user name. If the user name exist on the server, an error message is displayed asking the user to choose a different user name. Otherwise, the server register the user to the default 'general' room and inform all the users connected to this room that a new client is connected.

Use the created clients to interact with the server by performing various operations such as:

- Create chat-rooms by issuing the command **'/create roomName'**
- List all existing rooms by issuing the command **'/rooms'**
- Join existing chat-rooms by issuing the command **'/join roomName'**
- Send messages to chat-rooms by simply writing a message into the client text box and pressing the enter key or send button on the client
- Leave a chat-room by issuing the command **'/leave roomName'**
- List all users by issuing the command **'/users'**
- Disconnect from the chat using the command **'/exit'**

When a user connect to a new room, all the previous messages in this room are displayed to the user. In addition, all messages sent by the user are shown to all other clients connected to the same room with 1 second maximum delay. When a user leave a room, the server disconnect the user from the system.

### Added functionality

Now the client has to choose between loogging in or signing up, and provide a user name and a password. Also now you can create private chat-rooms.

The following operations have been added to the client:

- Create private chat-rooms by issuing the command **'/createPrivate roomName roomPassword'**
- List all possible commands the command **'/help'**
- Show the current chat-room by using **'/currentRoom'**

Adittionally, the client's errors managing has been improved, making it less possible get an Exception.

---

## Introducción

Esta es una aplicación de chat que emplea un sistema cliente-servidor mediante Java y CORBA.

## Compilación y ejecución

Desde la ruta del proyecto, introduce los siguientes comandos:

    mkdir bin
    idlj -td src/ -fall MyInterfaces.idl
    javac src/*.java src/ChatApp/*.java -d bin/
    orbd -ORBInitialPort 1050 -ORBInitialHost localhost

Ejecuta el servidor con el siguiente comando:

    java -cp bin/ CORBAServer -ORBInitialPort 1050 -ORBInitialHost localhost

Ahora, si el servidor ha sido ejecutado exitosamente, puedes ejecutar clientes usando el siguiente comando:

    java -cp bin/ CORBAClient -ORBInitialPort 1050 -ORBInitialHost localhost

Lo idóneo es tener múltiples clientes ejecutándose en diferentes consolas, para así poder ver las interacciones entre estos.

Al comenzar, el usuario debe indicar si quiere iniciar sesión o crear una cuenta, para posteriormente dar un nombre de usuario y una contraseña. Si estás iniciando sesión, el servidor comprobará si la combinación usuario-contraseña es correcta. Si estás creando una cuenta nueva, comprobará si el nombre de usuario no está en uso. Por defecto entrarás al chat general de la aplicación, pero puedes crear nuevos chats y desplazarte a estos.

Tienes a tu disposición los siguientes comandos para interactuar con el cliente:

- Crea un nuevo chat-room usando **'/create nombreDelChat'**
- Crea un nuevo chat-room privado usando **'/createPrivate nombreDelChat contraseñaDelChat'**
- Obtén una lista con todos los chats disponibles con **'/rooms'**
- Únete a un chat en concreto con el comando **'/join nombreDelChat'**
- Abandona un chat en concreto usando **'/leave nombreDelChat'**
- Obtén una lista con todos los usuarios activos en todos los chats **'/users'**
- Abandona la aplicación con **'/exit'**
- Si escribes un texto que no empieza por "/", se considerará un mensaje, y será recibido por todos los usuarios que estén en tu mismo chat

Si un usuario manda un mensaje en el mismo chat-room en el que estás, serás capaz de verlo. Además, cada vez que un usuario entra a un chat, podrá ver toda la conversación que tuvo lugar antes de unirse.
