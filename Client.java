import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Scanner;

public class Client {

  private Socket socket;
  private BufferedReader bufferedReader;
  private BufferedWriter bufferedWriter;
  private String username;

  public Client(Socket s, String username) {
    try {
      this.socket = s;
      //Buffered writer makes communication more efficient.
        //OutputStream is a byte stream, so it is wrapped in an OutputStreamWriter, which is a character stream.
          //We're sending chat messages so we want to send characters not bytes.
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
      this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
      this.username = username;

    } catch (IOException e) {
      closeEverything(socket, bufferedReader, bufferedWriter);

    }
  }

  //Sends message to clientHandler.
  public void sendMessage() {
    try {
      bufferedWriter.write(username);   //Write the username to buffered writer.
      bufferedWriter.newLine();         //Add newline character to end the username.
      bufferedWriter.flush();           //Flush the buffer to send the username to the ClientHandler.

      Scanner scnr = new Scanner(System.in);

      while (this.socket.isConnected()) {
        String messageToSend = scnr.nextLine();

        bufferedWriter.write(username + ": " + messageToSend);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        if (messageToSend.equals("/leave")) {
          closeEverything(socket, bufferedReader, bufferedWriter);
        }
      }

    } catch (IOException e) {
      closeEverything(socket, bufferedReader, bufferedWriter);

    }
  }

  public void listenForMessage() {
    //Create a new thread to listen for messages from the group chat.
    new Thread(new Runnable() {

      @Override
      public void run() {
        String messageFromGroupChat;

        //While connected, read messages from the bufferedReader and output them to the console.
        while (socket.isConnected()) {
          try {
            messageFromGroupChat = bufferedReader.readLine();
            System.out.println(messageFromGroupChat);

          } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
            return;
          }
        }
      }
    }).start(); //Start the new thread

  }

  public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
    try {
      //Check that objects are not null before closing to avoid null pointer exception.
        //Underlying streams in bufferedReader & bufferedWriter are closed when these are closed.
      if (socket != null) {
        socket.close();
      }

      if (bufferedReader != null) {
        bufferedReader.close();
      }

      if (bufferedWriter != null) {
        bufferedWriter.close();
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    Scanner scnr = new Scanner(System.in);

    System.out.println("Type '/leave' to exit the chat.");
    System.out.println("Enter your display name for the chat:");
    String username = scnr.nextLine();

    Socket socket = new Socket("localhost", 4567);
    Client client = new Client(socket, username);

    client.listenForMessage();
    client.sendMessage();
  }
}
