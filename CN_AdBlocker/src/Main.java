import java.util.Scanner;

public class Main {

    // Arguments are [HTTPCommand, URL, Port]
    public static void main(String[] args) {

        //Parse arguments
        Request request = parseArgs(args);

        //TODO: make the actual request
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


        String host;
        host = webaddress;
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
        return new Request(method, host, port, body);


    }

}
