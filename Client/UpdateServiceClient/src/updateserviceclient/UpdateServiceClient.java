/**
 * @author Deepanshu
 * @email deepanshumehndiratta[at]gmail.com
 */

package updateserviceclient;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.security.*;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
 * Main Class
 */
public class UpdateServiceClient {
    
    // Set server, port, etc
    private static final String SERVER = "pepper.nayidishastudios.com";
    private static final int PORT = 9999;
    private static PrintWriter writer;
    private static SQLiteConnection connection;
    public static String gameDir;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException, IOException {
        
        // Create/Open log file, flush it if it is over 4MB
        File fileHandle= new File("update.log");
        if(fileHandle.length() > (5*1024*1024))
            fileHandle.delete();

        writer = new PrintWriter(new FileWriter("update.log", true));

        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf =
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(dt);

        log("\n\nStarted update process at " + currentTime + "\n");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Close log file
                writer.close();
            }
        });
                    
        try {
            
            // Look for games directory
            File file = new File("../games");
            
            if (!file.exists()) {
                log("Game directory not found."
                        + "\nExiting update service.");
                System.exit(0);
            }
                
            File[] subdirs = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory();
                }
            });
            
            // Iterate through all directories inside the game directory
            for (File dir: subdirs) {
                
                File db = new File(dir + "/config.db");
                
                // Check if game configuration file exists inside the directory
                // If it does, it is a valid game
                if (db.exists()) {
                    
                    gameDir = "../games/" + dir;
                
                    connection = new SQLiteConnection(db);
                    connection.open();
                    
                    String machineId = getMachineId();
            
                    // Fetch ID of game that the service is initialized for
                    String gameId = getGameId();
                    String version = getVersion();
                    
                    log("\n\nStarting update of game: " + gameId + "\n");

                    // Initialize client communicator object and connect to the server
                    Communicator client = new Communicator(machineId, gameId, version, connection);
                    client.begin(SERVER, PORT);
                    
                }
            
            }
            
        }
        catch (SQLiteException ex) {
            Logger.getLogger(UpdateServiceClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /*
     * Get ID of game from the SQLite database
     */
    private static String getGameId() throws SQLiteException {
        SQLiteStatement loadAllRecordSt = connection.prepare("SELECT"
            + " * FROM settings");
        String gameId = null;
        while (loadAllRecordSt.step()) {
            gameId = loadAllRecordSt.columnString(3);
        }
        return gameId;
    }
    
    /*
     * Get version of the game for the SQLite database
     */
    private static String getVersion() throws SQLiteException {
        SQLiteStatement loadAllRecordSt = connection.prepare("SELECT"
            + " * FROM settings");
        String version = null;
        while (loadAllRecordSt.step()) {
            version = loadAllRecordSt.columnString(4);
        }
        return version;
    }
    
    /*
     * Retrieve Machine ID by MD5 hashing (Drive ID + MotherBoard ID)
     */
    private static String getMachineId() throws SQLiteException {
        String hashtext = null;
        
        String plaintext = MachineIdentifier.getSerialNumber("C")
            + MachineIdentifier.getMotherboardSN();
        
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(plaintext.getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1,digest);
            hashtext = bigInt.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32 ) {
                hashtext = "0" + hashtext;
            }
        }
        catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UpdateServiceClient.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        
        // Update Machine ID on SQLite database
        // (it is never retrieved from there)
        SQLiteStatement loadAllRecordSt = connection.prepare("SELECT"
            + " * FROM settings");
        int id = 0;
        while (loadAllRecordSt.step()) {
            id = loadAllRecordSt.columnInt(2);
        }
        SQLiteStatement query = connection.prepare("UPDATE settings SET"
                + " machine_id='" + hashtext + "' WHERE id=" + id);
        query.step();
        
        return hashtext;
    }
    
    /*
     * Log all output in the log file
     */
    public static void log(String message) {
        writer.println(message);
        System.out.println(message);
    }
}

/*
 * Class to communicate with the server
 * New instance is created for all the games and the are not run in parallel
 */
class Communicator {
    
    // Socket Object
    private SSLSocket socket;
    // SQLite connection object inherited from the Main Class
    private SQLiteConnection connection;
    // Server URL and port from Main Class
    private String server;
    private int port;
    // Sentinel for authentication
    private Boolean authenticated = false;
    //Input/Output and Bufferred streams for the socket
    private InputStream inputstream;
    private OutputStream outputstream;
    private BufferedReader bufferedreader;
    private BufferedWriter bufferedwriter;
    // Machine ID, retrived from parent class getMachineId()
    private String machineId;
    // Game ID(Repository name) and Version retrieved from SQLite Database
    private String gameId;
    private String version;
    // New version, sent by the server if available
    private String newversion = null;
    // Reconnect count. Reset to 0 after a successful connection.
    private int reconnectCounts = 0;
    // Connect counts for initial re-connection counts
    private int connectCounts = 0;
    // Sentinel for updated (Sets to false in case of failed file transfers, etc)
    private Boolean updated = true;
    
    /*
     * Initialize with variables sent by Main class for the game configuration
     */
    public Communicator(String machineId, String gameId, String version,
            SQLiteConnection connection) {
        this.machineId = machineId;
        this.gameId = gameId;
        this.version = version;
        this.connection = connection;
    }
        
    private SSLSocketFactory loadClientStores(String keyStoreFile,
            String keyStorePassword, String trustStoreFile,
            String trustStorePassword)
                throws NoSuchAlgorithmException,
                    KeyStoreException, FileNotFoundException, IOException,
                    UnrecoverableKeyException, KeyManagementException,
                    CertificateException
    {
            
        KeyManagerFactory kmf =  
            KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream readStream = new FileInputStream(keyStoreFile);
        keyStore.load(readStream, keyStorePassword.toCharArray());
        readStream.close();

        kmf.init(keyStore, keyStorePassword.toCharArray());

        TrustManagerFactory tmf =
            TrustManagerFactory.getInstance(TrustManagerFactory
                .getDefaultAlgorithm());
        KeyStore trustStore = KeyStore.getInstance("JKS");
        readStream = new FileInputStream(trustStoreFile);
        trustStore.load(readStream, trustStorePassword.toCharArray());
        readStream.close();
        tmf.init(trustStore);
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ctx.getSocketFactory();
    }
    
    /*
     * Establish socket connection with server
     */
    public void begin(String server, int port) throws ParseException,
            SQLiteException, 
            IOException {
        
        this.server = server;
        this.port = port;
        
        try {

            // Set keyStore and trustStore Paths and Passwords            
            SSLSocketFactory sslsocketfactory = loadClientStores("MyKey",
                    "vjd31hK48405qnS751kug4u173c97248813Z4551xBGF9p6HLQvhg3s63t"
                    + "Jf81s9pO6V6vP178803l1114Ks215S74O4Wx34631u", "TrustStore",
                    "Ip63a3T1q0857143PT3c8h3w328Pu2454332fvO15243Y437f4j3RMLY9V"
                    + "e3a76Ye4M36Mw8830X67w281b76i726UU5122P2638");
            socket = (SSLSocket) sslsocketfactory.createSocket(server, port);
            
            inputstream = socket.getInputStream();
            InputStreamReader inputstreamreader =
                    new InputStreamReader(inputstream);
            bufferedreader = new BufferedReader(inputstreamreader);

            outputstream = socket.getOutputStream();
            OutputStreamWriter outputstreamwriter =
                    new OutputStreamWriter(outputstream);
            bufferedwriter = new BufferedWriter(outputstreamwriter);
            socket.setSoTimeout(5000000);
        }
        catch (SocketException se) {
            UpdateServiceClient.log("Disconnected from server.");
            // If already tried to reconnect 11 times, exit
            if (connectCounts > 10)
                return;
            // Re-connect if not tried 11 times yet
            connectCounts++;
            // Sleep for 5 seconds and then try
            try {
                Thread.sleep(5000);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            begin(server, port);
            return;
        }
        catch (SSLHandshakeException ssl) {
            UpdateServiceClient.log("Disconnected from server.");
            // If already tried to reconnect 11 times, exit
            if (connectCounts > 10)
                return;
            // Re-connect if not tried 11 times yet
            connectCounts++;
            // Sleep for 5 seconds and then try
            try {
                Thread.sleep(5000);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            begin(server, port);
            return;
        }
        catch (Exception exception) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            exception.printStackTrace(new PrintStream(out));
            UpdateServiceClient.log(new String(out.toByteArray()));
            // If already tried to reconnect 11 times, exit
            if (connectCounts > 10)
                return;
            // Re-connect if not tried 11 times yet
            connectCounts++;
            // Sleep for 5 seconds and then try
            try {
                Thread.sleep(5000);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            begin(server, port);
            return;
        }
        // Reset reconnect and connect counts once the connection has been
        // established
        reconnectCounts = 0;
        connectCounts = 0;
        
        SQLiteStatement loadAllRecordSt1 = connection.prepare("SELECT"
            + " * FROM settings");
        String nversion = null;
        while (loadAllRecordSt1.step()) {
            nversion = loadAllRecordSt1.columnString(0);
        }
        
        // Check if there is is pending update (Update that was interrupted)
        // If it exists, resume it. Otherwise, check for a new update.
        
        if (nversion == null || nversion.equals(""))
            listen();
        else {
            
            authenticate();
            
            this.newversion = nversion;
            // Load all files which were not Downloaded/Partially downloaded/
            // Not replaced or not deleted as required by the update.
            SQLiteStatement loadAllRecordSt = connection.prepare("SELECT * FROM"
                    + " files as f WHERE  f.transmitted < f.size AND"
                    + " f.version='" + newversion + "' AND f.status < 3");
            // Iterate through all these files and perform the required actions
            while (loadAllRecordSt.step()) {
                
                int status = loadAllRecordSt.columnInt(0);
                int transmitted = loadAllRecordSt.columnInt(1);
                int id = loadAllRecordSt.columnInt(3);
                String fileName = loadAllRecordSt.columnString(4);
                int size = loadAllRecordSt.columnInt(5);
                int action = loadAllRecordSt.columnInt(6);
                
                SQLiteStatement query = connection.prepare("UPDATE files"
                    + " SET status='1' WHERE filename='" + fileName
                    + "' AND version='" + newversion + "'");
                query.step();
                
                // File has to be added/modified
                if (action == 0 || action == 1) {
                    // Files needs to be downloaded or resumed
                    if (status < 2) {
                        UpdateServiceClient.log("Requested File: "
                            + fileName + "(" + size + " bytes)");
                        // Recieve file from the required pointer
                        if(!recieveFile(fileName, size, transmitted, inputstream,
                                bufferedwriter))
                        updated = false;
                    }
                    // Replace the file
                    if (!replaceFile(fileName + ".temp", fileName))
                        updated = false;
                    bufferedreader.readLine();
                } // File has to be deleted
                else {
                    String[] f = fileName.split("/");
                    StringBuilder s = new StringBuilder();
                    
                    // Create actual File Path for Remote file
                    for (int j = 0; j < f.length; j++) {
                        if (j > 1 && f[j] != null) {
                            s.append(f[j]);
                            if (j < f.length-1)
                                s.append("/");
                        }
                    }
                    
                    File fil = new File(s.toString());
                    
                    // Check if file exists
                    if(fil.exists()){
                        UpdateServiceClient.log("Deleting file: "
                                + s.toString());
                        
                        // Try to delete the file
                        if(!fil.delete())
                            updated = false;
                        else {
                            query = connection.prepare("UPDATE files"
                                + " SET status='3' WHERE filename='"
                                + fileName + "'' AND version='" + newversion + "'");
                            query.step();
                        }
                    }
                    else {
                        query = connection.prepare("UPDATE files"
                            + " SET status='3' WHERE filename='"
                            + fileName + "'' AND version='" + newversion + "'");
                        query.step();
                    }
                }
            }
        }
        
        // If update has been completed successfully,
        // update the database accordingly
        if (newversion != null && updated) {
            
            SQLiteStatement loadAllRecordSt = connection.prepare("SELECT"
                + " * FROM settings");
            int id = 0;
            while (loadAllRecordSt.step()) {
                id = loadAllRecordSt.columnInt(2);
            }
            
            /*
            System.out.println("UPDATE settings SET"
                    + " new_version='', version='" + newversion + "'"
                    + ", update_status='1' WHERE id=" + id);
            */
            SQLiteStatement query = connection.prepare("UPDATE settings SET"
                    + " new_version='', version='" + newversion + "'"
                    + ", update_status='1' WHERE id=" + id);
            query.step();
            
        }
    }
    
    // Reconnect to the server after an interrupted connection
    public void reconnect() throws ParseException, SQLiteException, IOException {
        // If already tried and failed 11 times, exit
        if (reconnectCounts > 10)
            return;
        // Wait for 5 seconds between reconnects
        try {
            Thread.sleep(5000);
        }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        reconnectCounts++;
        authenticated = false;
        begin(server, port);
    }
    
    /*
     * Authenticate with the server
     */
    public void authenticate() throws ParseException, SQLiteException, IOException {
        
        String string;
        JSONObject json;
        
        // If not authenticated, commence        
        if (!authenticated) {
                
            try {
                // Keep reading messages from the server
                while ((string = bufferedreader.readLine()) != null) {
                    
                    // Log message
                    //UpdateServiceClient.log(string);
                    
                    json = (JSONObject)new JSONParser().parse(string);
                    JSONObject reply = new JSONObject();
                    String message = (String) json.get("message");

                    // Server is asking to authenticate
                    if (message.equals("authenticate")) {
                        UpdateServiceClient.log("System asking for"
                                + " authorization.");
                        // Ask server to read your Machine ID and
                        // authenticate you
                        reply.put("message", "readKey");
                        reply.put("machineId", machineId);
                        reply.put("gameId", gameId);
                        reply.put("version", version);
                        bufferedwriter.write(reply.toJSONString() + "\r\n");
                        bufferedwriter.flush();
                    } // Server could not authenticate you, subscription expired?
                    else if (message.equals("authenticationFailed")) {
                        UpdateServiceClient.log("Authentication Failed.");
                        break;
                    } // Server has authenticated you
                    else if (message.equals("authenticated")) {
                        UpdateServiceClient.log("Authenticated.");
                        authenticated = true;
                        break;
                    } // Could not understand server reply
                    else {
                        break;
                    }
                }
                
            }
            catch (SocketException se) {
                UpdateServiceClient.log("Socket Exception, re-establishing"
                        + " connection.");
                reconnect();
                return;
            }
            catch (SSLHandshakeException ssl) {
                UpdateServiceClient.log("SSL Exception, re-establishing"
                        + " connection");
                reconnect();
                return;
            }
            catch (IOException e) {
                UpdateServiceClient.log("IO Exception, re-establishing"
                        + " connection.");
                reconnect();
                return;
            }
            catch (Exception e) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                e.printStackTrace(new PrintStream(out));
                UpdateServiceClient.log(new String(out.toByteArray()));
            }
        }
    }
    
    /*
     * Once authenticated, communicate with the client to check for updates
     * and recieve files
     */
    public void listen() throws ParseException, SQLiteException, IOException {
        
        // If not authenticated, authenticate
        if (!authenticated)
            authenticate();
        
        try {

            String string = null;
            JSONObject json;
        
            Boolean exit = false;
            
            // Check for an update
            JSONObject reply1 = new JSONObject();
            reply1.put("message", "checkUpdate");
            bufferedwriter.write(reply1.toJSONString() + "\r\n");
            bufferedwriter.flush();
            JSONObject reply = null;
            
            // Keep reading server messages
            while ((string = bufferedreader.readLine()) != null) {
                json = (JSONObject)new JSONParser().parse(string);
                reply = new JSONObject();
                String message = (String) json.get("message");
                
                // Log server reply
                //UpdateServiceClient.log(string);
                
                // An update exists for this game
                if (message.equals("updateExists")) {
                    // Get the new version(Commit hash) from server
                    newversion = (String) json.get("newversion");
                    JSONArray array=(JSONArray)json.get("files");
                    
                    // Update game database with the new version
                    SQLiteStatement loadAllRecordSt = connection.prepare("SELECT"
                        + " * FROM settings");
                    int id = 0;
                    while (loadAllRecordSt.step()) {
                        id = loadAllRecordSt.columnInt(2);
                    }
                    SQLiteStatement query = connection.prepare("UPDATE settings"
                            + " SET new_version='" + newversion + "',"
                            + " update_status='0' WHERE id=" + id);
                    query.step();
                    
                    // Insert into the database the new version files and
                    // their details
                    for (int i = 0; i < array.size(); i++) {
                        JSONArray file = (JSONArray)array.get(i);
                        String fileName = (String) file.get(0);
                        int size = Integer.parseInt((String) file.get(1));
                        int action = Integer.parseInt((String) file.get(2));
                        query = connection.prepare("INSERT INTO files (filename"
                                + ",size,action,version,transmitted,status)"
                                + " VALUES ('" + fileName + "','" + size + "','"
                                + action + "','" + newversion + "','0','0')");
                        query.step();
                    }
                    
                    UpdateServiceClient.log(array.toJSONString());
                    
                    // Iterate through all files and Download/Delete them
                    for (int i = 0; i < array.size(); i++) {
                        JSONArray file = (JSONArray)array.get(i);
                        String fileName = (String) file.get(0);
                        int size = Integer.parseInt((String) file.get(1));
                        int action = Integer.parseInt((String) file.get(2));
                        query = connection.prepare("UPDATE files"
                                + " SET status='1' WHERE filename='" + fileName
                                + "' AND version='" + newversion + "'");
                        query.step();
                        // The file has been modified/added, download it
                        if (action == 0 || action == 1) {
                            UpdateServiceClient.log("Requested File: "
                                    + fileName + "(" + size + " bytes)");
                            // Start file transfer
                            if(!recieveFile(fileName, size, 0, inputstream,
                                    bufferedwriter))
                                updated = false;
                            // Replace the file
                            if (!replaceFile(fileName + ".temp", fileName))
                                updated = false;
                            bufferedreader.readLine();
                        } // Delete the file (if it exists)
                        else {
                            String[] f = fileName.split("/");
                            StringBuilder s = new StringBuilder();
                            
                            // Create actual File Path for Remote file
                            for (int j = 0; j < f.length; j++) {
                                if (j > 1 && f[j] != null) {
                                    s.append(f[j]);
                                    if (j < f.length-1)
                                        s.append("/");
                                }
                            }
                            File fil = new File(s.toString());
                            if(fil.exists()){
                                UpdateServiceClient.log("Deleting file: "
                                        + s.toString());
                                if(!fil.delete())
                                    updated = false;
                                else {
                                    query = connection.prepare("UPDATE files"
                                        + " SET status='3' WHERE filename='"
                                        + fileName + "' AND version='"
                                            + newversion + "'");
                                    query.step();
                                }
                            }
                            else {
                                query = connection.prepare("UPDATE files"
                                    + " SET status='3' WHERE filename='"
                                    + fileName + "' AND version='"
                                        + newversion + "'");
                                query.step();
                            }
                        }
                    }
                    // Notify the server of the update
                    if (updated) {
                        reply.put("message", "updated");
                        bufferedwriter.write(reply.toJSONString() + "\r\n");
                        bufferedwriter.flush();
                        UpdateServiceClient.log("Updates downloaded");
                    }
                    break;
                } // No new updates exist
                else if (message.equals("noUpdateExists")) {
                    reply.put("message", "updated");
                    bufferedwriter.write(reply.toJSONString() + "\r\n");
                    bufferedwriter.flush();
                    UpdateServiceClient.log("No update exists");
                    break;
                } // Could not understand server reply
                else {
                    UpdateServiceClient.log("Unrecognized server command."
                            + "\nExiting.");
                    break;
                }
            }
            // Close all streams
            inputstream.close();
            outputstream.close();
            socket.close();
            UpdateServiceClient.log("Finished updating game " + gameId + ".\n");
            //System.exit(0);
            
        }
        catch (SocketException se) {
            UpdateServiceClient.log("Socket Exception, re-establishing"
                    + " connection.");
            reconnect();
            return;
        }
        catch (SSLHandshakeException ssl) {
            UpdateServiceClient.log("SSL Exception, re-establishing"
                    + " connection.");
            reconnect();
            return;
        }
        catch (IOException e) {
            UpdateServiceClient.log("IO Exception, re-establishing"
                    + " connection.");
            reconnect();
            return;
        }
    }
    
    /*
     * Commence File transfer
     */
    private Boolean recieveFile(String fileName, int size, int startByte,
        InputStream inputstream, BufferedWriter bufferedwriter
      ) throws ParseException, SQLiteException, IOException {
        
        int total = 0;
        
        try {
            
            String[] f = fileName.split("/");
            StringBuilder s = new StringBuilder();
            
            s.append(UpdateServiceClient.gameDir + "/");
            
            // Create actual File Path for Remote file
            
            for (int i = 0; i < f.length; i++) {
                if (i > 1 && f[i] != null) {
                    s.append(f[i]);
                    if (i < f.length-1) {
                        
                        s.append("/");
                        
                        File file = new File(s.toString());
                        if(file.exists()){
                            UpdateServiceClient.log("Directory Exists: "
                                    + s.toString());
                        }
                        else{
                            boolean wasDirecotyMade = file.mkdirs();
                            if(wasDirecotyMade)
                                System.out.println("Directory Created: "
                                        + s.toString());
                            else
                                System.out.println("Sorry could not create"
                                        + " directory: " + s.toString());
                        }
                    }
                }
            }
            
            // Open file stream from the required byte (append mode)
            // or create a new temp file (if the temp file does not exist
            // or does not match the filesize in database)
        
            FileOutputStream fos = null;
            File f1 = new File(s.toString() + ".temp");
            
            // File has to be resumed
            if(startByte > 0) {
                // File exists, has to be resumed and database state is same as
                // file state, open file in append mode
                if (f1.exists() && f1.length() > 0 && f1.length() == startByte)
                    fos = new FileOutputStream(s.toString() + ".temp", true);
                else {
                    startByte = 0;
                    fos = new FileOutputStream(s.toString() + ".temp");
                }
            }
            else
                fos = new FileOutputStream(s.toString() + ".temp");
            
            total = startByte;
            
            // Ask server to send the file
            JSONObject reply = new JSONObject();
            reply.put("message", "sendFile");
            reply.put("file", fileName);
            reply.put("startByte", startByte);
            bufferedwriter.write(reply.toJSONString() + "\r\n");
            bufferedwriter.flush();
            
            // Read server reply to check if file exists on server
            // and you are authorized to access it
            String string = bufferedreader.readLine();
            JSONObject json = (JSONObject)new JSONParser().parse(string);
            String message = (String) json.get("message");
            if (message.equals("fileDoesNotExist")) {
                UpdateServiceClient.log("File: " + fileName + " of size "
                        + size + " bytes, could not be found.");
                return false;
            }
            
            UpdateServiceClient.log("Starting download of " + fileName
                    + " of size " + size + " bytes.");
            
            // Open Output stream on the file
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int bufferSize = socket.getReceiveBufferSize();
            
            UpdateServiceClient.log("Buffer size: " + bufferSize);
        
            byte[] bytes = new byte[bufferSize];

            int count;
            
            // Recieve the file
            while ((count = inputstream.read(bytes)) > 0) {
                total += count;
                bos.write(bytes, 0, count);
            
                if (total >= size)
                    break;
            }

            bos.flush();
            bos.close();
            
            // Update state of file to downloaded
            SQLiteStatement query = connection.prepare("UPDATE files"
                    + " SET status='2', transmitted='" + total + "' WHERE"
                    + " filename='" + fileName + "' AND version='"
                    + newversion + "'");
            query.step();
            return true;
            
        }
        catch (SocketException se) {
            UpdateServiceClient.log("Socket Exception, re-establishing"
                    + " connection.");
            // Update state of file to Downloading and set downloaded bytes
            // and reconnect
            SQLiteStatement query = connection.prepare("UPDATE files"
                    + " SET transmitted='" + total + "' WHERE"
                    + " filename='" + fileName + "' AND version='"
                    + newversion + "'");
            query.step();
            reconnect();
            return recieveFile(fileName, size, total, inputstream, bufferedwriter);
        }
        catch (SSLHandshakeException ssl) {
            UpdateServiceClient.log("SSL Exception, re-establishing"
                    + " connection.");
            // Update state of file to Downloading and set downloaded bytes
            // and reconnect
            SQLiteStatement query = connection.prepare("UPDATE files"
                    + " SET transmitted='" + total + "' WHERE"
                    + " filename='" + fileName + "' AND version='"
                    + newversion + "'");
            query.step();
            reconnect();
            return recieveFile(fileName, size, total, inputstream, bufferedwriter);
        }
        catch (IOException e) {
            UpdateServiceClient.log("IO Exception, re-establishing"
                    + " connection.");
            // Update state of file to Downloading and set downloaded bytes
            // and reconnect
            SQLiteStatement query = connection.prepare("UPDATE files"
                    + " SET transmitted='" + total + "' WHERE"
                    + " filename='" + fileName + "' AND version='"
                    + newversion + "'");
            query.step();
            reconnect();
            return recieveFile(fileName, size, total, inputstream, bufferedwriter);
        }
        
    }
    
    // Replace the File (Move temporary file)
    private Boolean replaceFile(String tempFile, String destinationFile) {
        try {
            
            // Build the actual file path
            String[] f = destinationFile.split("/");
            StringBuilder s = new StringBuilder();
            s.append(UpdateServiceClient.gameDir + "/");
            
            for (int i = 0; i < f.length; i++) {
                if (i > 1 && f[i] != null) {
                    s.append(f[i]);
                    if (i < f.length-1)
                        s.append("/");
                }
            }
            
            // Create the temporary file path
    	    File afile = new File(s.toString() + ".temp");
            
            // Perform the Rename/File move operation
            if (afile.renameTo(new File(s.toString()))) {
                UpdateServiceClient.log("File moved successfully!");
                SQLiteStatement query = connection.prepare("UPDATE files"
                    + " SET status='3' WHERE filename='" + destinationFile
                        + "' AND version='" + newversion + "'");
                query.step();
                return true;
            }
            else {
                UpdateServiceClient.log("File failed to move!");
                return false;
            }
 
        }
        catch (Exception e) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            UpdateServiceClient.log(new String(out.toByteArray()));
            return false;
    	}
    }
     
}

/*
 * Class to identify Moderboard ID and HDD ID.
 */
class MachineIdentifier {
    
    public static String getMotherboardSN() {
        String result = "";
        try {
          File file = File.createTempFile("realhowto",".vbs");
          file.deleteOnExit();
          FileWriter fw = new java.io.FileWriter(file);

          String vbs =
             "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
            + "Set colItems = objWMIService.ExecQuery _ \n"
            + "   (\"Select * from Win32_BaseBoard\") \n"
            + "For Each objItem in colItems \n"
            + "    Wscript.Echo objItem.SerialNumber \n"
            + "    exit for  ' do the first cpu only! \n"
            + "Next \n";

          fw.write(vbs);
          fw.close();
          Process p = Runtime.getRuntime().exec("cscript //NoLogo "
                  + file.getPath());
          BufferedReader input =
            new BufferedReader
              (new InputStreamReader(p.getInputStream()));
          String line;
          while ((line = input.readLine()) != null) {
             result += line;
          }
          input.close();
        }
        catch(Exception e){
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            UpdateServiceClient.log(new String(out.toByteArray()));
        }
        return result.trim();
    }
    
    public static String getSerialNumber(String drive) {
        String result = "";
        try {
          File file = File.createTempFile("realhowto",".vbs");
          file.deleteOnExit();
          FileWriter fw = new java.io.FileWriter(file);

          String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                      +"Set colDrives = objFSO.Drives\n"
                      +"Set objDrive = colDrives.item(\"" + drive + "\")\n"
                      +"Wscript.Echo objDrive.SerialNumber";  // see note
          fw.write(vbs);
          fw.close();
          Process p = Runtime.getRuntime().exec("cscript //NoLogo "
                  + file.getPath());
          BufferedReader input =
            new BufferedReader
              (new InputStreamReader(p.getInputStream()));
          String line;
          while ((line = input.readLine()) != null) {
             result += line;
          }
          input.close();
        }
        catch(Exception e){
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            UpdateServiceClient.log(new String(out.toByteArray()));
        }
        return result.trim();
    }
}
 