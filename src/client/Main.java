package client;

public class Main {

    public static void main(String[] args) {
        //Instanciamos el cliente y le decimos que conecte con el puerto 8080 y localhost
        Client client = new Client("localhost", 9000);
        client.start(); //Iniciamos el cliente
    }

}
