import java.util.Scanner;
import java.io.*;
import java.net.*;

import static java.lang.System.out;

public class Main {

    // Arguments are [HTTPCommand, URL, Port]
    public static void main(String[] args) {

        //Parse arguments
        Request request = parseArgs(args);

        createConnection(request);

        //TODO: make the actual request
    }

    public static void createConnection(Request request){

        try{
        Socket socket = new Socket(request.getHost(), request.getPortA());

        //Instantiates a new PrintWriter passing in the sockets output stream
            PrintWriter wtr = new PrintWriter(socket.getOutputStream());

            //Prints the request string to the output stream
            wtr.println(request.getMethod() + " " + request.getURL()  + " HTTP/1.1");
            wtr.println("Host: " + request.getHost());

            if (request.getMethod().equals("POST")){
            wtr.println("Content-length: 5");}
            wtr.println("Content-Type: image/png;charset=UTF-8");
            wtr.println("");
            wtr.flush();

            out.println(" \n \n ------------------------------------ \n SERVER RESPONSE \n ------------------------------------ \n \n ");

            if(request.getType().equals("jpg") || request.getType().equals("jpeg") || request.getType().equals("png")){

                getImage(socket, request.getType(), request.getURL());
            }else{
                getHtml(socket);
            }

            wtr.close();

        }catch (Exception e){
            out.print("\n ERROR" + e);
        }
    }
    //method to parse and analyse arguments
    public static Request parseArgs(String[] arguments){
        //Check if all arguments are given
        if(arguments.length != 3){
            out.print("You have given too many or to less arguments");
        }

        //Check if method is an existing method
        String method = arguments[0];
        if (method.equals("GET") || method.equals("PUT") || method.equals("POST") || method.equals("HEAD") ){
            out.print("Method is " + method);
        }else{
            out.print("Method is not supported fot HTTP/1.1");
        }

        //Check if there is a http header and if there is remove header
        String webaddress = arguments[1];
        if (webaddress.startsWith("http://")) {
            webaddress = webaddress.substring("http://".length());
        } else if (webaddress.startsWith("https://")) {
            webaddress = webaddress.substring("https://".length());
        }

        String address = webaddress;
        for (int i =0 ; i < webaddress.length(); i++) {
            if (webaddress.charAt(i) == ("/").charAt(0)) {
                webaddress = webaddress.substring(i);
                break;
            } else if (i == webaddress.length() - 1) {
                webaddress = "/";
                break;
            }
        }

        //get the host out of the webaddress
        String host = "";
        for (int t =0 ; t < address.length(); t++) {
            if (address.charAt(t) == ("/").charAt(0)) {
                break;
            }
            else host += address.charAt(t);

        }

        //GETS the file type
        String type = "";
        int i = webaddress.lastIndexOf('.');
        if (i > 0) {
            type = webaddress.substring(i+1);
        }

        int port = Integer.parseInt(arguments[2]);
        String body = "";

        //checks if method is POST ot PUT,
        //if it is POST or PUT ask user for body
        if (method.equals("POST") || method.equals("PUT")){
            out.print("\n\nGive the body of your request: \n");
            Scanner userInput = new Scanner(System.in);
            body = userInput.next();
            userInput.close();
            out.print("\nSuccesfully entered body\n");
        }
        //create new request with parsed arguments
        return new Request(method, webaddress, port, body, host, type);


    }

    public static void getHtml(Socket socket){

        try{
        //Creates a BufferedReader that contains the server response
        BufferedReader bufRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String outStr;

        System.out.println(" ------------------------------------ \n SERVER RESPONSE \n ------------------------------------ \n \n ");
        //Prints each line of the response
        PrintWriter text = new PrintWriter("doggie.txt");
        while((outStr = bufRead.readLine()) != null){
            System.out.println(outStr);
            text.println(outStr);
        }

        //Closes out buffer and writer
        bufRead.close();}catch (Exception e){System.out.println("ERROR OCCURRED = " + e);}
    }
    //Method to retrieve image and save it
    public static void getImage(Socket socket, String type, String Url){
        try{
        // Indicate start of the data
        boolean startFile = false;

        //Sets size of buffer we will read
        byte[] readBytes = new byte[2048];

            //GETS the file type
            String imageName = "";
            int i = Url.lastIndexOf('/');
            if (i > 0) {
                imageName = Url.substring(i+1);
            }

        // Initialize the streams.
        final FileOutputStream fileOutputStream = new FileOutputStream(imageName);
        final InputStream inputStream = socket.getInputStream();

        //Specify how many bytes to read from data
        int length;
        length = inputStream.read(readBytes);


        //As long as inputstream has data, keep saving data
        while (length != -1) {
            //Check whether data-part has started
            if (startFile) {
                fileOutputStream.write(readBytes, 0, length);
            }
            // This locates the end of the header by comparing the current byte as well as the next 3 bytes
            // with the HTTP header end "\r\n\r\n" (which in integer representation would be 13 10 13 10).
            // If the end of the header is reached, the flag is set to true and the remaining data in the
            // currently buffered byte array is written into the file.
            else {
                //Check if end of header is in current readbuffer

                for (int j = 0; j < 2045; j++) {
                    //end of header are two newlines in bytecode = 13,10,13,10
                    if (readBytes[j] == 13 && readBytes[j + 1] == 10 && readBytes[j + 2] == 13 && readBytes[j + 3] == 10) {
                        //end of header so we indicate end of header
                       startFile = true;
                        //we begin to write the file
                        fileOutputStream.write(readBytes, j+4 , 2048-j-4);
                        break;
                    }
                }
            }
            //read a new buffer of bytes
            length = inputStream.read(readBytes);
        }
        //close the inputstream because everything has been read
        inputStream.close();
    }catch (Exception e){
            System.out.println("ERROR CODE = " + e);
        }
    }

}
