package br.edu.ifpb.gugawag.so.sockets;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor2 {

    public static void main(String[] args) throws IOException {
        System.out.println("== Servidor ==");

        // Configurando o socket
        ServerSocket serverSocket = new ServerSocket(7001);
        Socket socket = serverSocket.accept();

        // pegando uma referência do canal de saída do socket. Ao escrever nesse canal, está se enviando dados para o
        // servidor
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        // pegando uma referência do canal de entrada do socket. Ao ler deste canal, está se recebendo os dados
        // enviados pelo servidor
        DataInputStream dis = new DataInputStream(socket.getInputStream());

        // laço infinito do servidor
        while (true) {
            System.out.println("Cliente: " + socket.getInetAddress());

            String mensagem = dis.readUTF();
            System.out.println(mensagem);

            String[] arr = mensagem.split(" ");

            switch (arr[0]) {
                case "readdir":
                    if (arr.length > 1){
                        executeCommand(new String[]{"ls", arr[1]}, dos);
                    } else {
                        executeCommand(new String[]{"ls"}, dos);
                    }
                    break;

                case "rename":
                    executeCommand(new String[]{"mv", arr[1], arr[2]}, dos);
                    break;
                case "create":
                    executeCommand(new String[]{"touch", arr[1]}, dos);
                    break;
                case "remove":
                    executeCommand(new String[]{"rm", arr[1]}, dos);
                    break;
                    
                default:
                    dos.writeUTF("Li sua mensagem: " + mensagem);
                    break;
            }
        }
        /*
         * Observe o while acima. Perceba que primeiro se lê a mensagem vinda do cliente (linha 29, depois se escreve
         * (linha 32) no canal de saída do socket. Isso ocorre da forma inversa do que ocorre no while do Cliente2,
         * pois, de outra forma, daria deadlock (se ambos quiserem ler da entrada ao mesmo tempo, por exemplo,
         * ninguém evoluiria, já que todos estariam aguardando.
         */
    }

    private static void executeCommand(String[] command, DataOutputStream dos){
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String response;
            switch (command[0]) {
                case "ls":
                    response = "Listagem de diretório:\n";
                    break;
                case "mv":
                    response = "Renomeiar arquivo: \n";
                    break;
                case "touch":
                    response = "Criação de arquivo: \n";
                    break;
                case "rm":
                    response = "Remover arquivo: \n";
                    break;
                default:
                    response = "";
                    break;
            }
            while ((line = reader.readLine()) != null){
                System.out.println(line);
                response +=  line + "\n";
            }
            dos.writeUTF(response);

            int exitCode = process.waitFor();
            System.out.println("Exited with error code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
