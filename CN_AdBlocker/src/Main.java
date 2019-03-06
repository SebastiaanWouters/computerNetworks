import java.util.Scanner;
import java.io.*;
import java.net.*;
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
            wtr.println("");
            wtr.flush();


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
            bufRead.close();
            wtr.close();

        }catch (Exception e){
            System.out.print("\n ERROR" + e);
        }
    }
    //method to parse and analyse arguments
    public static Request parseArgs(String[] arguments){
        //Check if all arguments are given
        if(arguments.length != 3){
            System.out.print("You have given too many or to less arguments");
        }

        //Check if method is an existing method
        String method = arguments[0];
        if (method.equals("GET") || method.equals("PUT") || method.equals("POST") || method.equals("HEAD") ){
            System.out.print("Method is " + method);
        }else{
            System.out.print("Method is not supported fot HTTP/1.1");
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

        int port = Integer.parseInt(arguments[2]);
        String body = "";

        //checks if method is POST ot PUT,
        //if it is POST or PUT ask user for body
        if (method.equals("POST") || method.equals("PUT")){
            System.out.print("\n\nGive the body of your request: \n");
            Scanner userInput = new Scanner(System.in);
            body = userInput.next();
            userInput.close();
            System.out.print("\nSuccesfully entered body\n");
        }
        //create new request with parsed arguments
        return new Request(method, webaddress, port, body, host);


    }

}
