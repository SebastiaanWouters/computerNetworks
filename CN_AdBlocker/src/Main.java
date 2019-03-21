import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.ArrayList;
import java.util.List;
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

        //Instantiates a new PrintWriter
            PrintWriter wtr = new PrintWriter(socket.getOutputStream());

            //Prints the request string to the output stream
            wtr.println(request.getMethod() + " " + request.getURL()  + " HTTP/1.1");
            wtr.println("Host: " + request.getHost());
            if (request.getMethod().equals("POST")){
            wtr.println("Content-length: 5");
            }
            if(request.getType().equals("jpg") || request.getType().equals("jpeg") || request.getType().equals("png")){
                wtr.println("Content-Type: image/" + request.getType() + ";charset=UTF-8");
            }
            else{
                wtr.println("Content-Type: html/txt");
            }
            wtr.println("");
            wtr.println("");
            if(!request.getBody().equals("")){
            wtr.println(request.getBody());}
            //send request
            wtr.flush();

            out.println(" \n \n ------------------------------------ \n SERVER RESPONSE \n ------------------------------------ \n \n ");

            //depending on sort of request
            if(request.getType().equals("jpg") || request.getType().equals("jpeg") || request.getType().equals("png") || request.getType().equals("gif")){
                getFile(socket, request.getType(), request.getURL());
                wtr.close();
            }else{
                getFile(socket, request.getType(), request.getURL());
                wtr.close();
                List<String> imageList = scanForImages(request.getURL());
                for(String image: imageList){
                    if(!image.contains("ad")){
                    String[] str= {"GET",request.getHost() + "/" + image, "80"};
                   Request rq =  parseArgs(str);
                    createConnection(rq);}
                }
            }



        }catch (Exception e){
            out.print("\n ERROR" + e);
        }
    }
    public static List<String> scanForImages(String url){
        try{
        File file = new File("./home.html");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String StrFile = "";
        String line;
        List<String> imageList = new ArrayList<>();
        while ((line = br.readLine()) != null){
            StrFile = StrFile + line;}

        int length = StrFile.length();
        int index;
        int index2;
        int index3;
        Boolean stop = false;
        String newImage="";
        while(StrFile.contains("<img")){
           index =  StrFile.indexOf("<img");
           StrFile = StrFile.substring(index);
           index2 = StrFile.indexOf("src=");
           index3 = StrFile.indexOf(">");
           stop = false;
            newImage="";
           for(int i = 0; i< index3;i++){
               if(!stop && StrFile.charAt(index2 + i+5) != '"'){
               newImage = newImage + StrFile.charAt(index2 + i+5);}
               else{
                   stop = true;
               }
           }
           StrFile = StrFile.substring(index2);
           imageList.add(newImage);
        }

        return imageList;}catch (Exception e){

        }

        return null;
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
            //TODO scanner only listens to one line
            Scanner userInput = new Scanner(System.in);
            body = body + userInput.nextLine();
            userInput.close();
            System.out.println(body);
            out.print("\nSuccesfully entered body\n");
        }
        //create new request with parsed arguments
        return new Request(method, webaddress, port, body, host, type);


    }



    //Method to retrieve image and save it
    public static void getFile(Socket socket, String type, String Url){
        try{
        // Indicate start of the data
        boolean startFile = false;

        //Sets size of buffer we will read
        byte[] readBytes = new byte[1000000];
        String path ="";
            //GETS the file type
            String imageName = "";
            if(!type.equals("")){
            int i = Url.lastIndexOf('/');
            if (i > 0) {
                imageName = Url.substring(i+1);
                path = Url.substring(0, i);

            }}
            //default is home.html
            else{
                imageName = "index.html";
                path = "/";
            }
            Boolean success = (new File("." + path).mkdirs());
        // Initialize the streams.
            final FileOutputStream fileOutputStream;
            if(!Url.equals("/")){
                fileOutputStream = new FileOutputStream(  Url.substring(1));}
            else{
               fileOutputStream = new FileOutputStream("." + "index.html");}

        final InputStream inputStream = socket.getInputStream();

        //Specify how many bytes to read from data
        int length;
        int fileLength = 100000 ;
        inputStream.read(readBytes);

        int part =0;
        //As long as inputstream has data, keep saving data
      //  while (length != -1) {
          //  System.out.println("Retrieving file ... part: " + part + startFile);

            //Check whether data-part has started
            /**
            if (startFile) {
                fileOutputStream.write(readBytes, 0, length);
            }*/
            // This locates the end of the header by comparing the current byte as well as the next 3 bytes
            // with the HTTP header end "\r\n\r\n" (which in integer representation would be 13 10 13 10).
            // If the end of the header is reached, the flag is set to true and the remaining data in the
            // currently buffered byte array is written into the file.
           // else {
                //Check if end of header is in current readbuffer

                String header = new String(readBytes);
                if(header.contains("Content-Length:")){
                    int i=  header.indexOf("Content-Length:") + 16;
                    int end = i;
                    boolean testEndLength = true;
                    int j =i;
                    while(testEndLength){
                        System.out.println(header.charAt(i));
                        if (header.charAt(i) != '0' && header.charAt(i) != '1' && header.charAt(i) != '2' && header.charAt(i) != '3' && header.charAt(i) != '4' && header.charAt(i) != '5' && header.charAt(i) != '6' && header.charAt(i) != '7' && header.charAt(i) !=  '8' && header.charAt(i) != '9'){
                            break;
                        }
                        else{}
                        end = end + 1;
                        i++;

                    }
                    fileLength = Integer.parseInt(header.substring(j, end));
                    System.out.println(fileLength);
                    }
                else if(header.contains("chuncked")){
                    //for loop chuncked data
                }

                for (int j = 0; j < 2045; j++) {
                    //end of header are two newlines in bytecode = 13,10,13,10

                    if (readBytes[j] == 13 && readBytes[j + 1] == 10 && readBytes[j + 2] == 13 && readBytes[j + 3] == 10) {
                        //end of header so we indicate end of header
                        startFile = true;
                        //we begin to write the file
                       // fileOutputStream.write(readBytes, j+4 , 1024-j-4);
                        fileOutputStream.write(readBytes, j+4, fileLength);
                        //byte[] readBytesCorrectLength = new byte[fileLength-1024+j+4];
                       // length = inputStream.read(readBytesCorrectLength);
                     //  fileOutputStream.write(readBytesCorrectLength,0,fileLength-1024+j+4);
                        break;
                    }
               // }
            }
            //read a new buffer of bytes
           // length = inputStream.read(readBytes);
           // part ++;
      //  }

        System.out.println("file transfer done");
        //close the inputstream because everything has been read
        inputStream.close();
    }catch (Exception e){

            System.out.println("ERROR CODE = " + e);
        }
    }

}
