/*
Simple Web Server in Java which allows you to call 
localhost:9000/ and show you the root.html webpage from the www/root.html folder
You can also do some other simple GET requests:
1) /random shows you a random picture (well random from the set defined)
2) json shows you the response as JSON for /random instead the html page
3) /file/filename shows you the raw file (not as HTML)
4) /multiply?num1=3&num2=4 multiplies the two inputs and responses with the result
5) /github?query=users/amehlhase316/repos (or other GitHub repo owners) will lead to receiving
   JSON which will for now only be printed in the console. See the todo below
6) You can enter profile? as an option, copy paste the below example
/profile?Name=asdf&Birthday=01/01/1900
7) You can enter repeat? as an option, copy paste the below example
/repeat?string=Test&num=50
The reading of the request is done "manually", meaning no library that helps making things a 
little easier is used. This is done so you see exactly how to pars the request and 
write a response back
 */

package funHttpServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Map;
import java.util.LinkedHashMap;
import java.nio.charset.Charset;

/**
 * @author user
 * Name: WebServer
 * Desc: Creates a new web server on port 8888
 */
class WebServer {
  public static void main(String args[]) {
    WebServer server = new WebServer(8888);
  }

  /**
   * Main thread
   * @param port to listen on
   */
  public WebServer(int port) {
    ServerSocket server = null;
    Socket sock = null;
    InputStream in = null;
    OutputStream out = null;

    try {
      server = new ServerSocket(port);
      while (true) {
        sock = server.accept();
        out = sock.getOutputStream(); in = sock.getInputStream();
        byte[] response = createResponse( in );
        out.write(response);
        out.flush(); in .close();
        out.close();
        sock.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (sock != null) {
        try {
          server.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Used in the "/random" endpoint
   */
  private final static HashMap < String, String > _images = new HashMap < > () {
    {
      put("streets", "https://iili.io/JV1pSV.jpg");
      put("bread", "https://iili.io/Jj9MWG.jpg");
    }
  };

  private Random random = new Random();

  /**
   * Reads in socket stream and generates a response
   * @param inStream HTTP input stream from socket
   * @return the byte encoded HTTP response
   */
  public byte[] createResponse(InputStream inStream) {

    byte[] response = null;
    BufferedReader in = null;

    try {

      // Read from socket's input stream. Must use an
      // InputStreamReader to bridge from streams to a reader
      in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));

      // Get header and save the request from the GET line:
      // example GET format: GET /index.html HTTP/1.1

      String request = null;

      boolean done = false;
      while (!done) {
        String line = in .readLine();

        System.out.println("Received: " + line);

        // find end of header("\n\n")
        if (line == null || line.equals(""))
          done = true;
        // parse GET format ("GET <path> HTTP/1.1")
        else if (line.startsWith("GET")) {
          int firstSpace = line.indexOf(" ");
          int secondSpace = line.indexOf(" ", firstSpace + 1);

          // extract the request, basically everything after the GET up to HTTP/1.1
          request = line.substring(firstSpace + 2, secondSpace);
        }

      }
      System.out.println("FINISHED PARSING HEADER\n");

      // Generate an appropriate response to the user
      if (request == null) {
        response = "<html>Illegal request: no GET</html>".getBytes();
      } else {
        // create output buffer
        StringBuilder builder = new StringBuilder();
        // NOTE: output from buffer is at the end

        if (request.length() == 0) {
          // shows the default directory page

          // opens the root.html file
          String page = new String(readFileInBytes(new File("www/root.html")));
          // performs a template replacement in the page
          page = page.replace("${links}", buildFileList());

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append(page);

          builder.append("Listed below are additional options you can choose from");

          builder.append("\n");
          builder.append("You can enter profile? as an option, copy paste the below example");
          builder.append("\n");
          builder.append("/profile?Name=JohnDoe&Birthday=09/09/1999");

          builder.append("\n");
          builder.append("You can enter repeat? as an option, copy paste the below example");
          builder.append("\n");
          builder.append("/repeat?string=ThisIsAString&num=30");

          builder.append("\n");
          builder.append("You can enter github?query=users/amehlhase316/repos (or other GitHub repo owners) will lead to receiving JSON which will for now only be printed in the console. See the todo below");
          builder.append("\n");

        } else if (request.equalsIgnoreCase("json")) {
          // shows the JSON of a random image and sets the header name for that image

          // pick a index from the map
          int index = random.nextInt(_images.size());

          // pull out the information
          String header = (String) _images.keySet().toArray()[index];
          String url = _images.get(header);

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: application/json; charset=utf-8\n");
          builder.append("\n");
          builder.append("{");
          builder.append("\"header\":\"").append(header).append("\",");
          builder.append("\"image\":\"").append(url).append("\"");
          builder.append("}");

        } else if (request.equalsIgnoreCase("random")) {
          // opens the random image page

          // open the index.html
          File file = new File("www/index.html");

          // Generate response
          builder.append("HTTP/1.1 200 OK\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append(new String(readFileInBytes(file)));

        } else if (request.contains("file/")) {
          // tries to find the specified file and shows it or shows an error

          // take the path and clean it. try to open the file
          File file = new File(request.replace("file/", ""));

          // Generate response
          if (file.exists()) { // success
            builder.append("HTTP/1.1 200 OK\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append(new String(readFileInBytes(file)));
          } else { // failure
            builder.append("HTTP/1.1 404 Not Found\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("File not found: " + file);
          }
        }

        //The condition where the user enters repeat as the query
        else if (request.contains("repeat?")) {

          //Try block to ensure server does not crash
          try {

            //Create a new query pair Map
            Map < String, String > query_pairs = new LinkedHashMap < String, String > ();
            // extract path parameters

            query_pairs = splitQuery(request.replace("repeat?", ""));

            //Create a string named s to hold user entered string
            String string = "";
            //Create an int to hold user entered int
            int num = 0;

            //try block to catch string input
            try {
              string = query_pairs.get("string");
            } catch (Exception e) {
              //Notify user with tailored response message showing they entered an incorrect string
              builder.append("400 Bad Request The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).");
              builder.append("\n");
              builder.append("Please enter a valid input for string");
            }

            //Try block for parsing user input as integer
            try {
              num = Integer.parseInt(query_pairs.get("num"));
            }
            //Catch block with tailored user feedback showing they entered an incorrect integer
            catch (Exception e) {
              builder.append("400 Bad Request The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).");
              builder.append("\n");
              builder.append("Please enter a valid input for num");
              System.out.println("Please enter a valid input for num");
            }

            //Show the data received from the user in command line for debug purposes
            System.out.println("String: " + string);
            System.out.println("Num: " + num);

            //Conditional statement to ensure that neither of the user inputs are null or empty
            if ((string == null) || (string == "")) {
              //User feedback with a 400 response code because the user entered incorrect data  
              builder.append("\n");
              builder.append("400 Bad Request The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).");
              builder.append("Please enter a valid string");
            }

            //Conditional statement to filter looping 0 or negative times
            if (num <= 0) {

              builder.append("\n");
              builder.append("400 Bad Request The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).");
              builder.append("Please enter a valid number. Num times must be greater than zero. ");
            }

            //Finally, ensure the string is not null and that we are looping more than 0 times. 
            //We do allow for empty string but it will not show anything. 
            if ((string != null) && (num >= 0)) {
              //Provide 200 level response code showing it was a success
              builder.append("200 OK The request succeeded. The result meaning of success depends on the HTTP method: GET: The resource has been fetched and transmitted in the message body.");
              builder.append("\n");

              //Loop through the number of times user entered and print their string
              for (int i = 0; i < num; i++) {
                System.out.println(string + " ");
                builder.append(" " + string);
                builder.append(" ");
              }

            }

          }
          //Catch block to ensure that server does not fail
          catch (Exception e) {
            builder.append("\n");
            builder.append("Client Error. Error Code 400\n");
            builder.append("Please enter a valid input. Format /repeat?word=anyWord&num=anyNumber");

            System.out.println("Caught an error");

          }

        } else if (request.contains("multiply?")) {
          // This multiplies two numbers, there is NO error handling, so when
          // wrong data is given this just crashes

          //Generic try block to ensure server does not crash
          try {
            Map < String, String > query_pairs = new LinkedHashMap < String, String > ();
            // extract path parameters
            query_pairs = splitQuery(request.replace("multiply?", ""));

            //Create two ints to hold user data
            //Instantiate them to zero in the event the user does not
            int num1 = 0;
            int num2 = 0;

            //Set the nums to the user entered data

            num1 = Integer.parseInt(query_pairs.get("num1"));

            num2 = Integer.parseInt(query_pairs.get("num2"));

            //Create result int and perform operation
            Integer result = num1 * num2;

            // Generate response
            //Use 200 level response code to show success
            builder.append("\n");
            builder.append("200 OK The request succeeded. The result meaning of success depends on the HTTP method: GET: The resource has been fetched and transmitted in the message body.");
            builder.append("\n");
            builder.append("Result is: " + result);
          }

          //Catch block to catch errors in user input
          catch (Exception e) {
            builder.append("400 Bad Request The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).");
            builder.append("\n");
            builder.append("Please enter valid input in the future");
          }

        }

        //Condition where user entered profile query
        else if (request.contains("profile?")) {

          //Try block to ensure server does not fail
          try {

            //Create new map
            Map < String, String > query_pairs = new LinkedHashMap < String, String > ();
            // extract path parameters
            query_pairs = splitQuery(request.replace("profile?", ""));

            //Create and instantiate Name String and Birthday String
            String Name = "";
            String Birthday = "";

            //Set name and birthday to user entered data
            Name = query_pairs.get("Name");
            Birthday = query_pairs.get("Birthday");

            //Condition to filter for blank input
            if ((Name.equals("") || (Birthday.equals("")))) {
              //400 code error for incorrect user input
              builder.append("400 error. Please enter a valid data");
            }

            //else condition to show correct data
            else {
              // Generate 200 response code for success
              builder.append("HTTP/1.1 200 OK\n");
              builder.append("Content-Type: text/html; charset=utf-8\n");
              builder.append("\n");

              //Print the data to the screen
              builder.append("\n");
              builder.append("Your Name is: " + Name);
              builder.append("\n");
              builder.append("Your Birthday is: " + Birthday);
              builder.append("\n");
            }

          }
          //Catch exception thrown above to ensure server does not fail
          catch (Exception e) {
            builder.append("\n");
            builder.append("Client Error. Error Code 400\n");
            builder.append("\n");
            builder.append("Please enter a valid input. Format: profile?Name=X&Birthday=Y \n");

            //Print that we caught an error to console for debugging
            System.out.println("Caught a 400 error");

          }

        }

        //condition user requests github as query
        else if (request.contains("github?")) {

          //Try block to ensure server does not crash
          try {

            //Create new map
            Map < String, String > query_pairs = new LinkedHashMap < String, String > ();

            query_pairs = splitQuery(request.replace("github?", ""));

            //Create string to hold JSON
            String json = fetchURL("https://api.github.com/" + query_pairs.get("query"));

            //Print string to console for debugging
            System.out.println(json);

            //Create string for repo name
            String fullRepoName = "";

            for (int i = 0; i < json.length(); i++) {
              if ((json.charAt(i) == 'f') && (json.charAt(i + 1) == 'u') && (json.charAt(i + 2) == 'l') && (json.charAt(i + 3) == 'l') && (json.charAt(i + 4) == '_') &&
                (json.charAt(i + 5) == 'n') && (json.charAt(i + 6) == 'a') && (json.charAt(i + 7) == 'm') && (json.charAt(i + 8) == 'e') && (json.charAt(i + 9) == '"') &&
                (json.charAt(i + 10) == ':') && (json.charAt(i + 11) == '"')) {

                i = i + 12;
                while (json.charAt(i) != '"') {
                  fullRepoName += json.charAt(i);
                  i++;
                }
                fullRepoName += ' ';

              }

            }

            System.out.println("should be printing fullRepoName " + fullRepoName);

            String loginOwnerOfEachRepo = "";
            for (int i = 0; i < json.length(); i++) {
              if ((json.charAt(i) == '"') && (json.charAt(i + 1) == 'i') && (json.charAt(i + 2) == 'd') && (json.charAt(i + 3) == '"') && (json.charAt(i + 4) == ':')) {
                i = i + 5;
                while (json.charAt(i) != ',') {
                  loginOwnerOfEachRepo += json.charAt(i);
                  i++;

                }
                loginOwnerOfEachRepo += ' ';

              }

            }

            System.out.println("");
            System.out.println("should be printing loginOwnerOfEachRepo " + loginOwnerOfEachRepo);

            String repoIDS = "";
            for (int i = 0; i < json.length(); i++) {
              if ((json.charAt(i) == '"') && (json.charAt(i + 1) == 'l') && (json.charAt(i + 2) == 'o') && (json.charAt(i + 3) == 'g') && (json.charAt(i + 4) == 'i') &&
                (json.charAt(i + 5) == 'n') && (json.charAt(i + 6) == '"') && (json.charAt(i + 7) == ':') && (json.charAt(i + 8) == '"')) {
                i = i + 9;
                while ((json.charAt(i) != '"') && (json.charAt(i) != ',')) {
                  repoIDS += json.charAt(i);
                  i++;
                }
                repoIDS += ' ';
              }

            }

            //Print data to console for debugging
            System.out.println("Print debug data");
            System.out.println("repoIDS " + repoIDS);

            //Print 200 code for success
            builder.append("HTTP/1.1 200 OK\n");
            builder.append("\n");

            builder.append("\n");

            builder.append("\n");
            builder.append("Full Repo Names: ");
            builder.append(fullRepoName);

            builder.append("\n");
            builder.append("Repo IDS: ");
            builder.append(repoIDS);

            builder.append("\n");
            builder.append("Login Owner Of Each Repo: ");
            builder.append(loginOwnerOfEachRepo);

          }

          //Catch user input exceptions
          //Print 400 level error codes
          catch (Exception e) {
            builder.append("400 error incorrect query");
            builder.append("Please enter proper query in the future");
          }

        } else {
          // if the request is not recognized at all
          builder.append("HTTP/1.1 400 Bad Request\n");
          builder.append("Content-Type: text/html; charset=utf-8\n");
          builder.append("\n");
          builder.append("I am not sure what you want me to do...");
        }

        // Output
        response = builder.toString().getBytes();
      }
    } catch (IOException e) {
      e.printStackTrace();
      response = ("<html>ERROR: " + e.getMessage() + "</html>").getBytes();
    }

    return response;
  }

  /**
   * Method to read in a query and split it up correctly
   * @param query parameters on path
   * @return Map of all parameters and their specific values
   * @throws UnsupportedEncodingException If the URLs aren't encoded with UTF-8
   */
  public static Map < String, String > splitQuery(String query) throws UnsupportedEncodingException {
    Map < String, String > query_pairs = new LinkedHashMap < String, String > ();
    // "q=hello+world%2Fme&bob=5"
    String[] pairs = query.split("&");
    // ["q=hello+world%2Fme", "bob=5"]
    for (String pair: pairs) {
      int idx = pair.indexOf("=");
      query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
        URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
    }
    // {{"q", "hello world/me"}, {"bob","5"}}
    return query_pairs;
  }

  /**
   * Builds an HTML file list from the www directory
   * @return HTML string output of file list
   */
  public static String buildFileList() {
    ArrayList < String > filenames = new ArrayList < > ();

    // Creating a File object for directory
    File directoryPath = new File("www/");
    filenames.addAll(Arrays.asList(directoryPath.list()));

    if (filenames.size() > 0) {
      StringBuilder builder = new StringBuilder();
      builder.append("<ul>\n");
      for (var filename: filenames) {
        builder.append("<li>" + filename + "</li>");
      }
      builder.append("</ul>\n");
      return builder.toString();
    } else {
      return "No files in directory";
    }
  }

  /**
   * Read bytes from a file and return them in the byte array. We read in blocks
   * of 512 bytes for efficiency.
   */
  public static byte[] readFileInBytes(File f) throws IOException {

    FileInputStream file = new FileInputStream(f);
    ByteArrayOutputStream data = new ByteArrayOutputStream(file.available());

    byte buffer[] = new byte[512];
    int numRead = file.read(buffer);
    while (numRead > 0) {
      data.write(buffer, 0, numRead);
      numRead = file.read(buffer);
    }
    file.close();

    byte[] result = data.toByteArray();
    data.close();

    return result;
  }

  /**
   *
   * a method to make a web request. Note that this method will block execution
   * for up to 20 seconds while the request is being satisfied. Better to use a
   * non-blocking request.
   * 
   * @param aUrl the String indicating the query url for the OMDb api search
   * @return the String result of the http request.
   *
   **/
  public String fetchURL(String aUrl) {
    StringBuilder sb = new StringBuilder();
    URLConnection conn = null;
    InputStreamReader in = null;
    try {
      URL url = new URL(aUrl);
      conn = url.openConnection();
      if (conn != null)
        conn.setReadTimeout(20 * 1000); // timeout in 20 seconds
      if (conn != null && conn.getInputStream() != null) {
        in = new InputStreamReader(conn.getInputStream(), Charset.defaultCharset());
        BufferedReader br = new BufferedReader( in );
        if (br != null) {
          int ch;
          // read the next character until end of reader
          while ((ch = br.read()) != -1) {
            sb.append((char) ch);
          }
          br.close();
        }
      } in .close();
    } catch (Exception ex) {
      System.out.println("Exception in url request:" + ex.getMessage());
    }
    return sb.toString();
  }
}