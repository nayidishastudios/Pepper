/**
 * @author Deepanshu
 * @email deepanshumehndiratta[at]gmail.com
 */

package updateserviceserver;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import net.java.ao.Entity;
import net.java.ao.EntityManager;
import net.java.ao.Query;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/*
 * Create interfaces for all database tables
 * Interface naming convention: Capitalize the fist letter of the table name
 *  Convention for names GET and SET methods for an interface:
 *      GET: public <return type(String in most cases)> getField_name
 *      SET: public void setField_name(<Field type> field_name)
 */

interface Repositories extends Entity {
    public String getId();
    public String getName();  
}

interface Comits extends Entity {
    public String getComit_hash();  
    public String getId();  
    public String getTime();  
}

interface Fils extends Entity {
    public String getId();
    public String getFilename();
    public String getSize();
    public String getAction();
}

interface Updates extends Entity {
    public String getId();
    public String getComit_id();
}

interface Groups extends Entity {
    public String getId();
}

interface Schools extends Entity {
    public String getId();
    public String getGroup_id();
}

interface Machines extends Entity {
    public String getId();
    public String getSchool_id();
}

interface Purchases extends Entity {
    public String getId();
    public String getTyp();
    public String getTyp_id();
}

interface Update_records extends Entity {
    public String getId();
    public String getUpdate_id();
    public String getMachine_id();
    public void setUpdate_id(String update_id);
    public void setMachine_id(String machine_id);
    public void setCreated_at(String created_at);
    public void setUpdated_at(String updated_at);
}

/*
 * Main Class
 */

public class UpdateServiceServer {
    
    /*
     * Retrive MySQL connection variables from /etc/environment
     */
    
    private static EntityManager manager = null;
    private static final String ROOT = System.getenv("REPO_DIR");
    private static final String MYSQL_HOST = System.getenv("DB_HOST");
    private static final String MYSQL_DB = System.getenv("UPDATE_DB");
    private static final String MYSQL_USER = System.getenv("DB_USER");
    private static final String MYSQL_PASS = System.getenv("DB_PASSWORD");

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException {
        
        Logger logger = Logger.getLogger("net.java.ao.EntityManager");
        logger.setLevel(Level.WARNING);
        
        /*
         * All repository revisions are permanently mounted
         * on pushing to the repository
        
        // Mount all the repositories
        mountRepositories(false);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Unmount all the repositories on exit
                mountRepositories(true);
            }
        });
        
        */
         
        manager = new EntityManager("jdbc:mysql://" + MYSQL_HOST +
                "/" + MYSQL_DB, MYSQL_USER, MYSQL_PASS);
                 
        // Initialize the socket variables
           
        SSLServerSocketFactory sslserversocketfactory = null;
        SSLServerSocket sslserversocket = null;
        SSLSocket sslsocket = null;
            
        try {
            
            // Create the socket
            // Set keyStore and trustStore Paths and Passwords            
            sslserversocketfactory = 
                    loadServerStores(System.getenv("SERVER_UPDATE_KEY_STORE"),
                        System.getenv("SERVER_UPDATE_KEY_STORE_PASS"),
                        System.getenv("SERVER_UPDATE_TRUST_STORE"), 
                        System.getenv("SERVER_UPDATE_TRUST_STORE_PASS"));
            sslserversocket = (SSLServerSocket)
                    sslserversocketfactory.createServerSocket(9999);

        }
        catch (SSLHandshakeException ssl) {
            System.out.println("Client disconnected.");
            //return;
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        
        // Infinite loop to listen to clients till the program is killed
        while (true) {
            try {
                sslsocket = (SSLSocket) sslserversocket.accept();
                sslsocket.setSoTimeout(5000000);
            }
            catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            // new thread for a client
            System.out.println("New client connected.");
            new Communicator(sslsocket).start();
        }
    }
        
    private static SSLServerSocketFactory loadServerStores(String keyStoreFile,
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
        return ctx.getServerSocketFactory();
    }
    
    /*
    
    private static void mountRepositories(Boolean unmount) {
        File file = new File(ROOT);
        File[] subdirs = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });
        for (File dir: subdirs) {
            try {
                ProcessBuilder pb = null;
                if (unmount)
                    pb = new ProcessBuilder("git", "fs", "umount");
                else
                    pb = new ProcessBuilder("git", "fs");
                pb.directory(dir);
                Process p = pb.start();
                p.waitFor();
                InputStream is = p.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            }
            catch (InterruptedException ex) {
                Logger.getLogger(UpdateServiceServer.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            catch (IOException ex) {
                Logger.getLogger(UpdateServiceServer.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }
     * 
     */
    
    /*
     * Class to authenticate the connecting client after Socket connection
     * has been established
     */
    
    private static class Authenticator {
        public static Boolean authenticate(String machineId, String schoolId,
         String groupId, String gameId, EntityManager manager)
          throws SQLException {
            
            // Check purchase of a game by Machine, School or Group
            Purchases[] purchases = manager.find(Purchases.class,
                    Query.select().where("((typ=2 AND typ_id=" + machineId + ")"
                    + " OR (typ=1 AND typ_id=" + schoolId + ")"
                    + " OR (typ=0 AND typ_id=" + groupId + "))"
                    + " AND repository_id=" + gameId));
            if (purchases.length > 0)
                return true;
            return false;
        }
    }
    
    /*
     * All communication with the client takes place here
     */
    
    private static class Communicator extends Thread {
        /*
         * Retrieve MySQL variables from Main Class
         */
        private final String ROOT = UpdateServiceServer.ROOT;
        private final String MYSQL_HOST = UpdateServiceServer.MYSQL_HOST;
        private final String MYSQL_DB = UpdateServiceServer.MYSQL_DB;
        private final String MYSQL_USER = UpdateServiceServer.MYSQL_USER;
        private final String MYSQL_PASS = UpdateServiceServer.MYSQL_PASS;
        /*
         * Socket is accessible to all class methods
         * All inout and output streams and buffers are available to all
         * class methods
         */
        private SSLSocket socket;
        private InputStream inputstream;
        private OutputStream outputstream;
        private BufferedReader bufferedreader;
        private BufferedWriter bufferedwriter;
        /*
         * Client sends machine ID which on authentication is used to
         * retrieve School ID and Group ID
         */
        private String groupId;
        private String schoolId;
        private String machineId;
        /*
         * Repository(Repository name), Version(Commit Hash)
         * are sent by the client on connection and gameId is retrieved from 
         * database after authentication of client
         */
        private String gameId;
        private String repository = "";
        private String version;
        /*
         * Commit ID of the New version sent to the client to be updated on the
         * database if update is completed on the client side
         * Update ID is the ID of the latest Update that has been created for
         * the client (Machine/School/Group)
         */
        private String newversion;
        private String updateId;
        /*
         * now: Time of the current version
         * time: Time of the latest version
         */
        private String now;
        private String time;
        /*
         * Each thread has its own Sentinel "authenticated" to restrict client
         * from performing any update withput authenticating.
         */
        private Boolean authenticated = false;
        EntityManager manager = UpdateServiceServer.manager;
        private String[][] files;
        
        // Initialize this instance with Socket object
        public Communicator(SSLSocket clientSocket) {
            this.socket = clientSocket;
        }
        
        /* Check for available updates and load the file information (if any)
         * into the files[][] array.
         */
        private void getUpdates() throws SQLException,
          ClassNotFoundException {
            files = null;
            newversion = null;
            Class.forName("com.mysql.jdbc.Driver") ;
            Connection conn = (Connection) DriverManager.getConnection(
                    "jdbc:mysql://" + MYSQL_HOST + "/" + MYSQL_DB,
                    MYSQL_USER, MYSQL_PASS);
            Statement stmt = (Statement) conn.createStatement();
            // Get one latest commit which is an update from the DB
            // which was created after the current version of clients commit.
            // The Commit is chosen from a list of commits which are for a Game
            // and are updates for the client Machine/School/Group and thus
            // are valid for the client (Nested MySQL query)
            String query = "SELECT * FROM comits WHERE id IN"
                    + " (SELECT comit_id FROM updates WHERE"
                    + " ((typ=2 AND typ_id=" + machineId + ")"
                    + " OR (typ=1 AND typ_id=" + schoolId + ")"
                    + " OR (typ=0 AND typ_id=" + groupId + "))"
                    + " AND repository_id=" + gameId
                    + ") ORDER by time DESC LIMIT 0,1";
            ResultSet rs = stmt.executeQuery(query);
            String comit = null;
            String time = null;
            // Get the Commit ID, time and Commit hash of the Commit
            while (rs.next()) {
                comit = rs.getString("id");
                time = rs.getString("time");
                newversion = rs.getString("comit_hash");
            }
            // If a commit with time >= current time exists
            if (comit != null ){
                // If the latest version isn't the same as the client side
                // current version
                if (!comit.equals(version)) {
                    // Retrieve ID of the update by Commit ID
                    Updates[] updates = manager.find(Updates.class,
                        Query.select().where("((typ=2 AND typ_id=" + machineId
                            + ") OR (typ=1 AND typ_id=" + schoolId + ")"
                            + " OR (typ=0 AND typ_id=" + groupId + "))"
                            + " AND comit_id=" + comit)
                            .limit(1));
                    updateId = updates[0].getId();
                    // Find all commits between the current client version
                    // and the latest version
                    Comits[] comits = manager.find(Comits.class,
                        Query.select().where("time >= '" + now + "' AND time<='"
                            + time + "' AND repository_id=" + gameId)
                            .order("time ASC"));
                    // Difference is between current and latest versions
                    // addFiles => All newly created files
                    // modFiles => All modified files
                    // delFiles => All deleted files
                    List<String[]> addFiles = new ArrayList<String[]>(),
                            modFiles = new ArrayList<String[]>(),
                            delFiles = new ArrayList<String[]>();
                    // Iterate through all commits
                    for (Comits c : comits) {
                        // Retrieve all files for a commit
                        Fils[] fils = manager.find(Fils.class,
                            Query.select().where("comit_id=" + c.getId()));
                        // Iterate through all these files
                        for (Fils fil : fils) {
                            int action = Integer.parseInt(fil.getAction());
                            // File added
                            if (action == 0) {
                                Iterator itr = delFiles.iterator();
                                // Iterate through all deleted files till
                                // commit younger than the current one
                                while (itr.hasNext()) {
                                    String[] f = (String[]) itr.next();
                                    // If the file was deleted in a previous 
                                    // commit, remove it from "delFiles"
                                    // as it has been added again
                                    if (f[0].equals(fil.getFilename())) {
                                        itr.remove();
                                    }
                                }
                                String[] n = {fil.getFilename(), fil.getSize(),
                                    fil.getAction(), c.getComit_hash()};
                                addFiles.add(n);
                            } // File modified
                            else if (action == 1) {
                                Iterator itr = addFiles.iterator();
                                // Iterate through all added files till
                                // commit younger than the current one
                                while (itr.hasNext()) {
                                    String[] f = (String[]) itr.next();
                                    // If the file was added in previous commit
                                    // remove it from "addFiles" as it has been
                                    // modified in this commit
                                    if (f[0].equals(fil.getFilename())) {
                                        itr.remove();
                                    }
                                }
                                itr = modFiles.iterator();
                                // Iterate through all modified files till
                                // commit younger than the current one
                                while (itr.hasNext()) {
                                    String[] f = (String[]) itr.next();
                                    // If the file was modified in the previous
                                    // commit remove it from "modFiles" and
                                    // add it again as it has been
                                    // modified in this commit so the file
                                    // size needs to be updated
                                    if (f[0].equals(fil.getFilename())) {
                                        itr.remove();
                                    }
                                }
                                String[] n = {fil.getFilename(), fil.getSize(),
                                    fil.getAction(), c.getComit_hash()};
                                modFiles.add(n);
                            } // File deleted
                            else {
                                Iterator itr = addFiles.iterator();
                                // Iterate through all added files till
                                // commit younger than the current one
                                while (itr.hasNext()) {
                                    String[] f = (String[]) itr.next();
                                    // If the file was added in previous commit
                                    // remove it from "addFiles" as it has been
                                    // deleted in this commit
                                    if (f[0].equals(fil.getFilename())) {
                                        itr.remove();
                                    }
                                }
                                itr = modFiles.iterator();
                                // Iterate through all modified files till
                                // commit younger than the current one
                                while (itr.hasNext()) {
                                    String[] f = (String[]) itr.next();
                                    // If the file was modified in previous
                                    // commit remove it from "addFiles" as it
                                    // has been deleted in this commit
                                    if (f[0].equals(fil.getFilename())) {
                                        itr.remove();
                                    }
                                }
                                String[] n = {fil.getFilename(), fil.getSize(),
                                    fil.getAction(), c.getComit_hash()};
                                delFiles.add(n);
                            }
                        }
                    }
                    // Merge all 3 lists into "allFiles"
                    List<String[]> allFiles = new ArrayList<String[]>(addFiles);
                    allFiles.addAll(modFiles);
                    allFiles.addAll(delFiles);
                    List<String[]> Files = new ArrayList<String[]>();
                    int i = 0;
                    // Iterate through all files
                    for (String[] f: allFiles) {
                        // Build actual download path for the files
                        if ((Integer.parseInt(f[2]) == 2) || 
                                ((Integer.parseInt(f[2]) == 0 
                                    || Integer.parseInt(f[2]) == 1) 
                                        && Integer.parseInt(f[1]) > 0)) {
                            String[] a = new String[3];
                            a[0] = f[3] + "/worktree/" + f[0];
                            a[1] = f[1];
                            a[2] = f[2];
                            Files.add(a);
                            i++;
                        }
                    }
                    // Convert the list to an array
                    files = Files.toArray(new String[Files.size()][]);
                }
            }
        }
        
        /* Send requested file after verifying if the client is eligible
         * to recieve it
         */
        private void sendFile(String fileName, int startByte)
          throws FileNotFoundException, IOException, ClassNotFoundException,
          SQLException {
            
            // Sentinel set to false by default, only sets to true if client
            // can recieve the requested file
            Boolean cont = false;
            
            String[] f = fileName.split("/");
            
            StringBuilder s = new StringBuilder();
            
            // Create actual name of the file and remove version information
            for (int i = 2; i < f.length; i++) {
                s.append(f[i]);
                if (i < f.length-1)
                    s.append("/");
            }
            
            // Check if user is allowed to access the file and it exists
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = (Connection) DriverManager.getConnection(
                    "jdbc:mysql://" + MYSQL_HOST + "/" + MYSQL_DB,
                    MYSQL_USER, MYSQL_PASS);
            Statement stmt = (Statement) conn.createStatement();
            
            // Select all files which are younger or equal to the time
            // of the latest update and belong to the client game.
            String query = "SELECT f.comit_id, f.filename, c.id, c.comit_hash"
                    + " FROM `fils` f, comits c WHERE f.comit_id IN"
                    + " (SELECT id FROM comits WHERE repository_id='" + gameId
                    + "' AND time<='" + time + "') AND f.comit_id=c.id";
            ResultSet rs = stmt.executeQuery(query);
            String comit = null;
            time = null;
            // Iterate through all retrieved file data
            while (rs.next()) {
                String comit_hash = rs.getString("comit_hash");
                String fName = rs.getString("fileName");
                // Check if a file with the particular version exists and
                // is avaialable to the client
                if (comit_hash.equals(f[0]) && s.toString().equals(fName)) {
                    cont = true;
                    break;
                }
            }
            
            // Send a message accordingly
            JSONObject reply = new JSONObject();
            if (!cont)
                reply.put("message", "fileDoesNotExist");
            else
                reply.put("message", "fileExists");
            
            bufferedwriter.write(reply.toJSONString() + "\r\n");
            bufferedwriter.flush();
            
            if (!cont)
                return;
            
            // Create path for the file
            File file = new File(ROOT + "/" + repository
                    + "/fs/commits/" + fileName);
            
            byte[] buf = new byte[4096];
            int len;
            
            FileInputStream fis = new FileInputStream(file);
            
            // Start from the specified byte in the file
            if (startByte > 0)
                fis.skip(startByte - 1);
            
            // Write byte output to the Output Stream
            while((len=fis.read(buf))!=-1) {
                outputstream.write(buf, 0, len);
                outputstream.flush();
            }
            // Close the file after it has been sent
            fis.close();
        }

        @Override
        public void run() {
            
            JSONObject json;
            
            try {
                // Create Input/Output and Buffered streams for the 
                // socket session
                inputstream = socket.getInputStream();
                InputStreamReader inputstreamreader =
                    new InputStreamReader(inputstream);
                bufferedreader = new BufferedReader(inputstreamreader);

                outputstream = socket.getOutputStream();
                OutputStreamWriter outputstreamwriter =
                    new OutputStreamWriter(outputstream);
                bufferedwriter = new BufferedWriter(outputstreamwriter);
                
                String string;
                
                // Ask client for authentication
                JSONObject send = new JSONObject();
                send.put("message", "authenticate");
                bufferedwriter.write(send.toJSONString() + "\r\n");
                bufferedwriter.flush();
                System.out.println("Asking client to authorize");
                
                // Keep listening to client
                while ((string = bufferedreader.readLine()) != null) {
                    // Read the client reply and parse JSON from it
                    json = (JSONObject)new JSONParser().parse(string);
                    JSONObject reply = new JSONObject();
                    // Get client message
                    String message = (String) json.get("message");
                    System.out.println(string);
                    // Write to Output Buffer or not (rue by default)
                    Boolean sendMsg = true;
                    // If client is not authenticated restrict it from
                    // performing any update
                    if (!authenticated) {
                        // Client sends its machine id
                        if (message.equals("readKey")) {
                            // Retrive client machine from machineId
                            Machines[] machines = manager.find(Machines.class,
                                Query.select().where("machine_id='"
                                    + (String) json.get("machineId") + "'"));
                            // Retrieve Game details from repository(game) name
                            Repositories[] repositories = manager.find(
                                    Repositories.class,
                                    Query.select().
                                    where("name='"
                                        + (String) json.get("gameId") + "'"));
                            // Check if machine and game exist
                            if (machines.length > 0 && repositories.length > 0) {
                                // Get Repository name, Game ID,
                                // School, ID and Group ID
                                repository = repositories[0].getName();
                                machineId = machines[0].getId();
                                gameId = repositories[0].getId();
                                schoolId = machines[0].getSchool_id();
                                Schools[] schools = manager.find(Schools.class,
                                   Query.select().where("id=" + schoolId));
                                groupId = schools[0].getGroup_id();
                                Comits[] comits = manager.find(Comits.class,
                                    Query.select().where("comit_hash='"
                                    + (String) json.get("version")
                                    + "' AND repository_id=" + gameId));
                                // Authenticate machine based on all the
                                // availabe information
                                if (Authenticator.authenticate(machineId,
                                 schoolId, groupId, gameId, manager)
                                        && comits.length > 0) {
                                    authenticated = true;
                                    version = comits[0].getId();
                                    now = comits[0].getTime();
                                    getUpdates();
                                    reply.put("message", "authenticated");
                                }
                                else {
                                    reply.put("message", "authenticationFailed");
                                    bufferedwriter.write(reply.toJSONString()
                                            + "\r\n");
                                    bufferedwriter.flush();
                                    inputstream.close();
                                    outputstream.close();
                                    socket.close();
                                    return;
                                }
                            }
                            else {
                                reply.put("message", "authenticationFailed");
                                bufferedwriter.write(reply.toJSONString()
                                        + "\r\n");
                                bufferedwriter.flush();
                                inputstream.close();
                                outputstream.close();
                                socket.close();
                                return;
                            }
                        } // Kep asking client for authentication
                        // if it isn't authenticated
                        else {
                            reply.put("message", "authenticate");
                        }
                    }
                    else {
                        // Client asking for updates
                        if (message.equals("checkUpdate")) {
                            // Check if files[][] array is empty
                            if (files == null || files.length == 0) {
                                reply.put("message", "noUpdateExists");
                            }
                            else {
                                // If update exists, send client the available
                                // files and the commit hash of the latest
                                // version
                                reply.put("message", "updateExists");
                                reply.put("newversion", newversion);
                                JSONArray list = new JSONArray();
                                for (String[] update: files) {
                                    JSONArray up = new JSONArray();
                                    up.addAll(Arrays.asList(update));
                                    list.add(up);
                                }
                                /*
                                // Structure of files array {"fileName", "size"}
                                String[][] files = {
                                    {"a", "1024", "0"}, // File added
                                    {"b", "1024", "1"}, // File modified
                                    {"b", "0", "2"} // File deleted
                                };
                                */
                                reply.put("files", list);
                            }
                        } // Client is asking for a file
                        else if (message.equals("sendFile")) {
                            String file = (String) json.get("file");
                            int startByte = Integer.parseInt(
                                    json.get("startByte").toString());
                            // Send file to the client (if it exists and client
                            // is authorized to recieve it)
                            sendFile(file, startByte);
                            sendMsg = false;
                            bufferedwriter.write("\n");
                            bufferedwriter.flush();
                        } // Client has been updated
                        else if (message.equals("updated")) {
                            sendMsg = false;
                            // Check if an update record for the client and 
                            // update exists, if not create it
                            Update_records[] records = manager.find(
                                Update_records.class,Query.select().where(
                                    "update_id=" + updateId
                                    + " AND machine_id=" + machineId ));
                            if (records.length <= 0 && updateId != null) {
                                
                                java.util.Date dt = new java.util.Date();
                                java.text.SimpleDateFormat sdf =
                                new java.text.SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm:ss");
                                String currentTime = sdf.format(dt);
                                
                                // Initialize an empty update_record row,
                                // add details about the record to it and
                                // save it
                                Update_records record = manager.create(
                                        Update_records.class);  
                                record.setUpdate_id(updateId);
                                record.setMachine_id(machineId);
                                record.setCreated_at(currentTime);
                                record.setUpdated_at(currentTime);
                                System.out.println(record);
                                record.save();
                            }
                            System.out.println("Client updated.\nClosing"
                                    + " connection to client.");
                            this.interrupt();
                        }
                    }
                    if (sendMsg) {
                        bufferedwriter.write(reply.toJSONString() + "\r\n");
                        bufferedwriter.flush();
                    }
                }
            }
            catch (ClassNotFoundException ex) {
                Logger.getLogger(UpdateServiceServer.class.getName()).log(
                        Level.SEVERE, null, ex);
            }            catch (SQLException ex) {
                Logger.getLogger(UpdateServiceServer.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            catch (org.json.simple.parser.ParseException ex) {
                Logger.getLogger(UpdateServiceServer.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            catch (SocketException se) {
                System.out.println("Client disconnected.");
                return;
            }
            catch (SSLHandshakeException ssl) {
                System.out.println("Client disconnected.");
                return;
            }
            catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
