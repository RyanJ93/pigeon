package pigeonServer.services;

import pigeonServer.support.Server;

public class ServerService {
    Server server = null;

    public ServerService start(){
        if ( this.server == null ){
            this.server = new Server();
            this.server.start();
        }
        this.server.setRunning(true);
        return this;
    }

    public ServerService stop(){
        if ( this.server != null ){
            this.server.setRunning(false);
        }
        return this;
    }
}
