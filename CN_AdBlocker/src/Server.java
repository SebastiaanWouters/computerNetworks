import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.StringTokenizer;
import java.nio.file.Paths;
import java.io.*;
import java.net.*;



/**
 * Runnable implements to start a thread
 */
public class Server implements Runnable{
    private Socket connect;

	/**
	 * Contstructor that defines connect to a socket
	 * @param c socket
	 */
	public Server(Socket c) {
		connect = c;
	}


	/**
	 * Opens a server and starts to listen to http requests
	 * @param args takes takes
	 */
	public static void main(String[] args) {
		//try catch needed to catch exceptions
		try {
			//some lines to make server readable
			System.out.println("Starting the server...");
			//creates a server with port 8080
			ServerSocket computerNetworksSocket = new ServerSocket(8080);
			System.out.println("* Server succesfully started, now listening to port 8080 * ");

			// while loop to listen on port 8080
			while (true) {
				//.accept() pauses until it get an active connection on port 8080
				//then a new server instance is made to support multithread
				Server computerNetworksServer = new Server(computerNetworksSocket.accept());

				//A connection was made to our socket
                System.out.println("* Connected to a client *");


				// create a new thread to manage the client connection and support multiple connections
				Thread thread = new Thread(computerNetworksServer);
				thread.start();
			}
			//output the error
		} catch (IOException e) {
			System.err.println("An error occured on the server : " + e.getMessage());
		}
	}

	//When a connection is made thread will start this run-method

    /**
     * Method that will be invokes once a connection has been made
     */
	@Override
	public void run() {
		// Some inits
		BufferedReader clientInput = null; //Reads text from a character-input stream
		PrintWriter serverOuptut = null; //Prints formatted representations of objects to a text-output stream
		OutputStream dataOutput = null; //The class implements an output stream.
		String fileRequested = null; //stores file client requested
		String httpMethod = "";

		//Stays alive-persistent connection
		while(true){

		try {
			// Reading bytes from socket inputstream and convert them into characters
            // we get character output stream to client (for headers)
            // get binary output stream to client (for requested data)
			clientInput = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			serverOuptut = new PrintWriter(connect.getOutputStream());
			dataOutput = connect.getOutputStream();
			
			// store client request line per line
			String input = clientInput.readLine();
            System.out.println(input);
			// split into tokens
			StringTokenizer inputToken = new StringTokenizer(input);

			//Extract httpMethod
			if (inputToken.hasMoreTokens() == true) {
			httpMethod = inputToken.nextToken().toUpperCase();
			}
			//Extract file
			if (inputToken.hasMoreTokens() == true) {
				fileRequested = inputToken.nextToken().toLowerCase();
			}
               //HANDLE GET && HEAD REQUEST
            if(httpMethod.equals("GET") || httpMethod.equals("HEAD")){
                String filePath="";

				//Check if a specific file is requested otherwise return standard homepage
				if (fileRequested.endsWith("/")) {
					fileRequested += "home.html";
				}
                int lengthOfFile = 0;
                File fileServed = null;
				//Get requested file and return to a response
                if(httpMethod.equals("GET")){
                    filePath = "." + fileRequested;
                    fileServed = new File(".", fileRequested);
				    lengthOfFile = (int) fileServed.length();

                }
				String fileContentType = getContentType(fileRequested);
                if(!fileServed.exists()){
                    create_404_response(serverOuptut,dataOutput);
                }
                else if(fileContentType.contains("image")){
				createGetResponseImage(serverOuptut, dataOutput, fileServed, lengthOfFile, fileContentType, filePath, fileRequested);}
                else{
                createGetResponseHtml(serverOuptut,dataOutput,fileServed,lengthOfFile,fileContentType,filePath,fileRequested);
                }
				System.out.println("File " + fileRequested + " of type " + fileContentType + "was succesfully requested and send");
			}
			//HANDLE PUT REQUEST
            else if(httpMethod.equals("PUT")){
                String body = clientInput.readLine();
                while(!body.equals("")){
                body = clientInput.readLine();
                }
                clientInput.readLine();
                body = clientInput.readLine();
                System.out.println(body);
                createFile(body, fileRequested);
                create_200_response(serverOuptut,dataOutput,fileRequested,"200");

            }else if(httpMethod.equals("POST")){
                String body = clientInput.readLine();
                while(!body.equals("")){
                    body = clientInput.readLine();
                }
                clientInput.readLine();
                body = clientInput.readLine();
                System.out.println(body);
                createFile(body, fileRequested);
                create_200_response(serverOuptut,dataOutput,fileRequested,"200");
            }
            //Non implemented method
            else {
                //Serve a error 500
                System.out.println("500 Server ERROR");
                create_500_response(serverOuptut,dataOutput);}
            System.out.println("Waiting for next request ...");
		} catch (FileNotFoundException e) {
			try {
			    //If file is not found return 404 NOT FOUND error message
				create_404_response(serverOuptut,dataOutput);
                System.out.println("Waiting for next request ...");
			} catch (Exception e2) {
				System.err.println("Unkown exception: " + e2.getMessage());
			}

			//If nothing was asked do nothing
		} catch (Exception e) {

                create_404_response(serverOuptut,dataOutput);

        } /**finally {
		    //After request has been handled the socket is been properly closes
			try {
			    //closing of all objects server need
				clientInput.close();
				serverOuptut.close();
				dataOutput.close();
				connect.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Unknown error while closing socket :" + e.getMessage());
			}
			if (output) {
				System.out.println("*The Server has terminated the connection *\n \n");
			}
		}*/}

		
		
	}

    private void create_200_response(PrintWriter serverOuptut, OutputStream dataOutput, String fileRequested, String s) {
        File fileServed = new File(".", "/responses/200.html");
        long length = fileServed.length();
        int i = (int)length;
        byte[] fileData = new byte[0];
        try {
            fileData = readFileData(fileServed, i);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // send HTTP Headers
        serverOuptut.println( "HTTP/1.1 200 OK");
        serverOuptut.println("Server: HTTP Server from Wout en Bassie");
        serverOuptut.println("Date: " + new Date());
        serverOuptut.println("Content-type: " + "text/html");
        serverOuptut.println("Content-length: " + i);
        serverOuptut.println("");
        serverOuptut.println("");
        serverOuptut.flush();
        try {
            dataOutput.write(fileData, 0, i);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createGetResponseImage(PrintWriter serverOuptut, OutputStream dataOutput, File fileServed, int lengthOfFile , String fileContentType, String filePath, String fileRequested) throws IOException{

        dataOutput.write("HTTP/1.1 200 OK".getBytes());
        dataOutput.write("Server: HTTP Server from Wout en Bassie".getBytes());
        dataOutput.write(("Date: " + new Date()).getBytes());
        dataOutput.write(("Content-type: " + fileContentType).getBytes());
        dataOutput.write(("Content-length: " + lengthOfFile).getBytes());
        byte[] bytes = {13,10,13,10}; //needed to show end header
        dataOutput.write(bytes);

        File file = new File(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage image = ImageIO.read(file); //convert file to bufferedImage
            String type;
            if (fileRequested.endsWith("jpg") || fileRequested.endsWith("jpeg")){
                type = "jpg";
            }else if (fileRequested.endsWith("png")){
                type = "png";
            } else if (fileRequested.endsWith("txt")) {
                type = "txt";
            }else if (fileRequested.endsWith("html")){
                type = "html";
            }else{
                type="unknonw";
            }
            ImageIO.write(image,type,baos);//convert file to buffered array
            dataOutput.write(baos.toByteArray());
            dataOutput.flush();


    }
    private void createGetResponseHtml(PrintWriter serverOutput, OutputStream dataOutput, File fileServed, int lengthOfFile , String fileContentType, String filePath, String fileRequested) throws IOException{

        File file = new File(filePath);
        int fileLength = (int)file.length();
        byte[] fileData = new byte[0];
        try {
            fileData = readFileData(file, fileLength);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // we send HTTP Headers with data to client
        serverOutput.println("HTTP/1.1 200 OK");
        serverOutput.println("Server: HTTP Server from Wout en Bassie");
        serverOutput.println("Date: " + new Date());
        serverOutput.println("Content-type: " + "text/html");
        serverOutput.println("Content-length: " + fileLength);
        serverOutput.println(); // blank line between headers and content, very important !
        serverOutput.flush(); // flush character output stream buffer
        // file
        try {
            dataOutput.write(fileData, 0, fileLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void create_404_response(PrintWriter serverOutput, OutputStream dataOutput){
        File file = new File("./responses/404.html");
        int fileLength = (int)file.length();
        byte[] fileData = new byte[0];
        try {
            fileData = readFileData(file, fileLength);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // we send HTTP Headers with data to client
        serverOutput.println("HTTP/1.1 404 Not Found");
        serverOutput.println("Server: HTTP Server from Wout en Bassie");
        serverOutput.println("Date: " + new Date());
        serverOutput.println("Content-type: " + "text/html");
        serverOutput.println("Content-length: " + fileLength);
        serverOutput.println(); // blank line between headers and content, very important !
        serverOutput.flush(); // flush character output stream buffer
        // file
        try {
            dataOutput.write(fileData, 0, fileLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void create_500_response(PrintWriter serverOutput, OutputStream dataOutput) throws IOException{

        File file = new File("./responses/500.html");
        int fileLength = (int)file.length();
        byte[] fileData = new byte[0];
        try {
            fileData = readFileData(file, fileLength);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // we send HTTP Headers with data to client
        serverOutput.println("HTTP/1.1 500 Not Implemented");
        serverOutput.println("Server: HTTP Server from Wout en Bassie");
        serverOutput.println("Date: " + new Date());
        serverOutput.println("Content-type: " + "text/html");
        serverOutput.println("Content-length: " + fileLength);
        serverOutput.println(); // blank line between headers and content, very important !
        serverOutput.flush(); // flush character output stream buffer
        // file
        try {
            dataOutput.write(fileData, 0, fileLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    /**
     * Method to read byte data from a file
     * @param file file
     * @param fileLength length of file to detremine buffersize
     * @return returns file in byte array representation
     * @throws IOException
     */
    private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}
		
		return fileData;
	}



    /**
     * Method that determines content type of a file
     * @param fileRequested file
     * @return extension
     */
	private String getContentType(String fileRequested) {
	    //check if file is a html file
		if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html")){
			return "text/html";}
		//check if file is an image
        else if( fileRequested.endsWith(".png")){
            return "image/png";
        }
        else if(fileRequested.endsWith(".jpg")  ||  fileRequested.endsWith(".jpeg")){
            return "image/jpg";
        }
		else{
			return "text/plain";}
	}




	    /**
		int lengthOfFile =  fileServed.length();
		String fileContentType = "text/html";
        BufferedOutputStream dataOutputBuffered = new BufferedOutputStream(dataOutput);
		createResponse(serverOutPut, dataOutputBuffered,fileServed,lengthOfFile, fileContentType,firstString,"Server: HTTP server"  );
		System.out.println("File " + fileRequested + " not found");*/

    /**
     * Creates a file from body and file location + name
     * @param body
     * @param locationName
     */
	private void createFile(String body, String locationName){

        PrintWriter writer = null;
        try {
            writer = new PrintWriter("." + locationName +".txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writer.println(body);
        writer.close();}


	
}
