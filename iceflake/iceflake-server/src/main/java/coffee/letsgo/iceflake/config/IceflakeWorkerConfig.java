package coffee.letsgo.iceflake.config;

public class IceflakeWorkerConfig {
    private String worker;
    private int port;
    private int id;

    public IceflakeWorkerConfig(String worker,
                                int port,
                                int id) {
        this.worker = worker;
        this.port = port;
        this.id = id;
    }

    public String getWorker() {
        return worker;
    }

    public int getPort() {
        return port;
    }

    public int getId() {
        return id;
    }
}
