import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
  private ServerSocket serverSocket;

  public Server(ServerSocket serverSocket) {
    this.serverSocket = serverSocket;
  }

  public void startServer() {
    try {
      while (!serverSocket.isClosed()) {

        //.accept is a blocking method, program will halt here waiting for a new connection.
        Socket socket = serverSocket.accept();
        
        // Creates a new instance of the ClientHandler object
        ClientHandler clientHandler = new ClientHandler(socket);

        System.out.println("New client: " + clientHandler.getClientUsername() + " connected");

        //New thread for the new client connected. This handles that clients messages sent and returns all other messages from the server.
        Thread thread = new Thread(clientHandler);
        thread.start();

      }

    } catch (IOException e) {}
  }

  //Method to close server socket, so we can avoid nested try catch blocks.
  public void closeServerSocket() {
    try {
      //Check for null pointer exception before closing serverSocket
      if (serverSocket != null) {
        serverSocket.close();

      }

    } catch (IOException e) {
      e.printStackTrace();

    }
  }

  public static void main(String[] args) throws IOException {
    ServerSocket serverSocket = new ServerSocket(4567);
    Server server = new Server(serverSocket);
    server.startServer();
    
  }
}