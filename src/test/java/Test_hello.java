import com.silver.nettyrpc.server.ServerRPC;

public class Test_hello {
    public static void main(String[] args) {
        ServerRPC server = new ServerRPC("7080");
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
