package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket; // Representa la conexión con el servidor.
    private BufferedReader input; //Se usa para recibir datos del servidor.
    private PrintWriter output; // Se usa para enviar datos al servidor.

    //Le pasamos al cliente como parametros el host y el puerto.
    public Client(String host, int port) {
        while (true) {
            try {
                socket = new Socket(host, port); //Una vez conectado, inicializa los flujos de entrada y salida
                input = new BufferedReader(new InputStreamReader(socket.getInputStream())); //BufferedReader para leer datos del servidor
                output = new PrintWriter(socket.getOutputStream(), true); //PrintWriter para enviar datos al servidor
                System.out.println("Conectando al servidor " + host + ":" + port);
                break;
            } catch (IOException e) {
                System.out.println("Error al conectar: " + e.getMessage());
                System.out.println("Reintentando en 5 seconds...");
                try { //Si la conexión falla, imprime el error y espera 5 segundos antes de intentarlo nuevamente
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    System.out.println("Proceso interrumpido.");
                }
            }
        }
    }

    //Creamos un menu para gestionar todo
    public void start() {
        Scanner scanner = new Scanner(System.in); //A través del scanner pedimos la información al cliente de que quiere hacer
        String opcion;

        do {
            System.out.println("\n1. Obtener un libro por ISBN");
            System.out.println("2. Obtener un libro por título");
            System.out.println("3. Obtener libros por autor");
            System.out.println("4. Añadir un libro");
            System.out.println("5. Salir");
            System.out.print("Elige una opción: ");
            opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    getBookByISBN(scanner);
                    break;
                case "2":
                    getBookByTitle(scanner);
                    break;
                case "3":
                    getBooksByAuthor(scanner);
                    break;
                case "4":
                    addBook(scanner);
                    break;
                case "5":
                    System.out.println("Saliendo...");
                    exit();
                    break;
                default:
                    System.out.println("Opción no válida, intentalo de nuevo");
            }
        } while (!opcion.equals("5"));

        closeConnection(); //cerramos la conexión
        scanner.close(); //carramos el scanner
    }

    //Solicita al usuario que introduzca el ISBN del libro.
    private void getBookByISBN(Scanner scanner) {
        System.out.print("Introduce el ISBN del libro: ");
        String isbn = scanner.nextLine(); //Leemos el IBAN
        sendMessage("CHECK_ISBN:" + isbn);
        //Usa el prefijo "CHECK_ISBN:" para indicar al servidor que quiere buscar un libro por su ISBN.
        getResponse(); //Recibimos respuesta
    }

    //Solicita al usuario que introduzca el título del libro.
    private void getBookByTitle(Scanner scanner) {
        System.out.print("Introduce el título del libro: ");
        String title = scanner.nextLine(); //Leemos el título
        sendMessage("CHECK_TITLE:" + title);
        //Usa el prefijo "CHECK_TITLE:" para indicar al servidor que quiere buscar un libro por su título
        getResponse(); //Recibimos respuesta
    }

    //Solicita al usuario el nombre del autor.
    private void getBooksByAuthor(Scanner scanner) {
        System.out.print("Introduce el autor: ");
        String author = scanner.nextLine();
        sendMessage("CHECK_BOOKS_BY_AUTHOR:" + author);
        //Envía el prefijo "CHECK_BOOKS_BY_AUTHOR:" seguido del nombre del autor.
        getResponse(); //Recibimos respuesta
    }

    //Método para añadir un libro
    private void addBook(Scanner scanner) {
        sendMessage("TRY_ADD_BOOK:");
        // Envía un mensaje inicial ("TRY_ADD_BOOK:") para verificar si el servidor está listo para añadir un libro.
        if (!getResponse()) { //Si el servidor responde positivo, solicitamos al usuario los datos del libro
            return;
        }

        System.out.print("Introduce el ISBN: ");
        String isbn = scanner.nextLine();
        System.out.print("Introduce el título: ");
        String titulo = scanner.nextLine();
        System.out.print("Introduce el autor: ");
        String autor = scanner.nextLine();
        System.out.print("Introduce el precio: ");
        String precio = scanner.nextLine();

        sendMessage("ADD_BOOK_REQUEST:" + isbn + "," + titulo + "," + autor + "," + precio); //Construye un mensaje con el formato
        getResponse(); //Espera la respuesta del servidor para confirmar si se añadió el libro.
    }

    private void sendMessage(String message) {
        output.println(message);
        //Este método simplifica el envío de mensajes para que otros métodos
        //no tengan que preocuparse por los detalles de implementación
    }

    private boolean getResponse() {
        String linea;
        boolean bandera = false;

        try {
            while ((linea = input.readLine()) != null) {
                //cada vez que acabamos una petición hay que indicar que hemos acabado.
                if (linea.equals("END_RESPONSE")) {
                    break; //Si la línea leída es "END_RESPONSE", finaliza la lectura.
                }
                System.out.println("Respuesta del servidor: " + linea);
                //Muestra cada línea de la respuesta del servidor en la consola.

                //Si linea es igual al del servidor, bandera se pasa a true y entra
                if (linea.equals("Listo para añadir un nuevo libro")) {
                    bandera = true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error al recibir una respuesta: " + e.getMessage());
        }
        return bandera; //Devuelve true si el servidor confirmó que está listo para alguna operación
    }

    //ierra los recursos utilizados en la conexión con el servidor
    private void closeConnection() {
        try {
            input.close();
            output.close();
            socket.close();
            System.out.println("Conexión cerrada");
        } catch (IOException e) { //Si ocurre un error al cerrar alguno de estos recursos, lo informa en la consola.
            System.out.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    //nforma al servidor que el cliente está cerrando la conexión mediante el comando "SALIR".
    private void exit(){
        sendMessage("SALIR");
    }

}