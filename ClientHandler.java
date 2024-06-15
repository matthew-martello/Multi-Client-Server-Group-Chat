import java.util.ArrayList;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ClientHandler implements Runnable {
  
  //ArrayList to store all the clients currently connected to the server.
  //This is static so the list belongs to the class NOT the object.
  public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

  private String clientUsername;

  //Represents the connection between the Server and ClientHandler
    //Sockets have an input and output stream that is used to send and received messages.
  private Socket socket;

  private BufferedReader bufferedReader;
  private BufferedWriter bufferedWriter;
  
  public ClientHandler(Socket socket) {
    try {
      this.socket = socket;
      
      //Buffered writer makes communication more efficient.
        //OutputStream is a byte stream, so it is wrapped in an OutputStreamWriter, which is a character stream.
          //We're sending chat messages so we want to send characters not bytes.
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      //When a new client is connected, the first message sent is their display name.
      this.clientUsername = bufferedReader.readLine();

      //Add the new client to the ArrayList.
        //clientHandles is a static ArrayList belonging to the class, adding 'this', this instance of this object is added to the list.
      clientHandlers.add(this);

      broadcastMessage("SERVER: " + clientUsername + " has entered the chat.");

    } catch (IOException e) {
      closeEverything(socket, bufferedReader, bufferedWriter);

    }
  }

  @Override
  public void  run() {
    String messageFromClient;

    //While the client is connected, listen for messages from that client.
    while (socket.isConnected()) {
      try {
        //The incoming message from the client
        messageFromClient = bufferedReader.readLine();
        
        //Strips the username from the message
        String messageOnly = messageFromClient.substring(messageFromClient.indexOf(":") + 2, messageFromClient.length());
        
        //Disconnect client from server if they enter '/leave' command
        if (messageOnly.equals("/leave")) {
          closeEverything(socket, bufferedReader, bufferedWriter);
          break;
        } else {
          broadcastMessage(messageFromClient);
        }

      } catch (IOException e) {
        closeEverything(socket, bufferedReader, bufferedWriter);
        break;

      }
    }
  }

  public void broadcastMessage(String messageToSend) {
    //Send message to every client in the ArrayList except for the sender.
    for (ClientHandler c : clientHandlers) {
      try {
        //If the current client in the loop isn't the sender, send them the message.
        if (!c.clientUsername.equals(clientUsername)) {
          c.bufferedWriter.write(messageToSend);
          
          //Clients excepting a new line character with readLine(), send a new line character so the client can receive the message.
          c.bufferedWriter.newLine();

          //Buffer won't be sent down it's output stream unless full.
            //Because messages aren't likely to fill the buffer, we flush it to send the message anyway.
          c.bufferedWriter.flush();

        }
      } catch (IOException e) {
        closeEverything(socket, bufferedReader, bufferedWriter);

      }
    }
  }

  public void removeClientHandler() {
    //Remove the current Client from the ArrayList.
    clientHandlers.remove(this);

    broadcastMessage("SERVER: " + clientUsername + " has left the chat.");
  }

  public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
    //When closeEverything is called, either the user has left of there is an error meaning the user is no longer connected, so we remove them from the ArrayList.
    removeClientHandler();
    
    try {
      //Check that objects are not null before closing to avoid null pointer exception.
        //Underlying streams in bufferedReader & bufferedWriter are closed when these are closed.
      if (bufferedReader != null) {
        bufferedReader.close();
      }

      if (bufferedWriter != null) {
        bufferedWriter.close();
      }

      if (socket != null) {
        socket.close();
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getClientUsername() {
    return this.clientUsername;
  }
}
