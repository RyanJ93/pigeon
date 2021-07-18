package pigeonServer.support;

import java.util.HashSet;

public class FileLocker {
    private static FileLocker instance;

    public static FileLocker getInstance(){
        if ( FileLocker.instance == null ){
            FileLocker.instance = new FileLocker();
        }
        return FileLocker.instance;
    }

    private final HashSet<String> locks = new HashSet<>();

    private FileLocker(){}

    public synchronized void acquire(String path) throws InterruptedException {
        while ( this.locks.contains(path) ){
            this.wait();
        }
        this.locks.add(path);
        this.notifyAll();
    }

    public synchronized void release(String path){
        this.locks.remove(path);
        this.notifyAll();
    }
}
