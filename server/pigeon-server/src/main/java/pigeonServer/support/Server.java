package pigeonServer.support;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    private boolean running = true;
    private ServerSocket serverSocket;

    private void initServer(){
        try{
            this.serverSocket = new ServerSocket(2898);
        }catch(IOException ex){
            ex.printStackTrace();
            Logger.log("An error occurred while starting up the server.");
        }
    }

    public synchronized Server setRunning(boolean running){
        this.running = running;
        return this;
    }

    public synchronized boolean getRunning(){
        return this.running;
    }

    @Override
    public void run(){
        this.initServer();
        if ( this.serverSocket != null ){
            while ( this.getRunning() ){
                try{
                    Socket incoming = serverSocket.accept();
                    ClientRequestHandler clientRequestHandler = new ClientRequestHandler(incoming);
                    clientRequestHandler.start();
                }catch(IOException ex){
                    Logger.log("Failed to establish a connection with a client");
                }
            }
        }
    }
}
