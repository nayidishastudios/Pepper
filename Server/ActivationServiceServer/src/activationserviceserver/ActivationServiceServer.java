/**
 * @author Deepanshu
 * @email deepanshumehndiratta[at]gmail.com
 */

package activationserviceserver;

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
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import net.java.ao.Entity;
import net.java.ao.EntityManager;
import net.java.ao.Query;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
 * Create interfaces for all database tables
 * Interface naming convention: Capitalize the fist letter of the table name
 *  Convention for names GET and SET methods for an interface:
 *      GET: public <return type(String in most cases)> getField_name
 *      SET: public void setField_name(<Field type> field_name)
 */

interface Installers extends Entity {
    public String getPassword_digest();
}

interface Groups extends Entity {
    public String getId();
    public String getName();
}

interface Schools extends Entity {
    public String getId();
    public String getName();
    public String getGroup_id();
}

interface Machines extends Entity {
    public String getId();
    public String getSchool_id();
    public String getMachine_id();
    public String getName();
    public String getSsl_key();
    public void setSchool_id(int school_id);
    public void setMachine_id(String machine_id);
    public void setName(String name);
    public void setSsl_key(String ssl_key);
    public void setCreated_at(String created_at);
    public void setUpdated_at(String updated_at);
}

/*
 * Main Class
 */
public class ActivationServiceServer {
    
    // Get all MySQL variables
    private static EntityManager manager = null;
    private static final String MYSQL_HOST = System.getenv("DB_HOST");
    private static final String MYSQL_DB = System.getenv("UPDATE_DB");
    private static final String MYSQL_USER = System.getenv("DB_USER");
    private static final String MYSQL_PASS = System.getenv("DB_PASSWORD");
    private static final String KEY_DIR = System.getenv("KEY_DIR");
    // Get Trust store(Update), its password and Client update key store password.
    private static final String TRUST_STORE
            = System.getenv("SERVER_UPDATE_TRUST_STORE");
    private static final String TRUST_STORE_PASS
            = System.getenv("SERVER_UPDATE_TRUST_STORE_PASS");
    private static final String CLIENT_KEY_STORE_PASS
            = System.getenv("CLIENT_UPDATE_KEY_STORE_PASS");

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, IOException,
            InterruptedException {
        
        Logger logger = Logger.getLogger("net.java.ao.EntityManager");
        logger.setLevel(Level.WARNING);
         
        manager = new EntityManager("jdbc:mysql://" + MYSQL_HOST +
                "/" + MYSQL_DB, MYSQL_USER, MYSQL_PASS);
            
        SSLServerSocketFactory sslserversocketfactory = null;
        SSLServerSocket sslserversocket = null;
        SSLSocket sslsocket = null;
            
        try {
            
            // Set keyStore and trustStore Paths and Passwords
            sslserversocketfactory = 
                    loadServerStores(System.getenv("SERVER_ACTIVATE_KEY_STORE"),
                        System.getenv("SERVER_ACTIVATE_KEY_STORE_PASS"),
                        System.getenv("SERVER_ACTIVATE_TRUST_STORE"), 
                        System.getenv("SERVER_ACTIVATE_TRUST_STORE_PASS"));
            sslserversocket = (SSLServerSocket)
                    sslserversocketfactory.createServerSocket(10000);

        }
        catch (SSLHandshakeException ssl) {
            System.out.println("Client disconnected.");
            //return;
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        
        // Listen to new connections untill terminated
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
     * Class to authenticate installers based on username and password
     * Password is encrypted by BCrypt (Same is used by Rails, so password
     * isn't avaialable in a raw format anywhere on the server)
     */
    private static class Authenticator {
        public static Boolean authenticate(String username, String password,
                EntityManager manager)
          throws SQLException {
            
            // Check for blank Username/Password
            if (username == null || username.equals("")
                    || password == null || password.equals(""))
                return false;
            
            String pass = null;
            
            // Find Installer with the supplied username
            Installers[] installers = manager.find(Installers.class,
                    Query.select().where("username='" + username + "'"));
            
            // Get Encryped password of that installer
            for (Installers installer: installers)
                pass = installer.getPassword_digest();
            
            // If installer exists, check its password
            // against the supplied password
            if (pass != null)
                if (BCrypt.checkpw(password, pass))
                    return true;
            return false;
        }
    }
    
    /*
     * Commumnicator class to interact with the clients
     * (Every connection has a unique Object of this class and communicates
     * through it)
     */
    private static class Communicator extends Thread {
        // Get MySQL variables from the Main class
        private final String MYSQL_HOST = ActivationServiceServer.MYSQL_HOST;
        private final String MYSQL_DB = ActivationServiceServer.MYSQL_DB;
        private final String MYSQL_USER = ActivationServiceServer.MYSQL_USER;
        private final String MYSQL_PASS = ActivationServiceServer.MYSQL_PASS;
        // Socket object is available to all Class methods.
        private SSLSocket socket;
        // Input/Output streams and buffers
        private InputStream inputstream;
        private OutputStream outputstream;
        private BufferedReader bufferedreader;
        private BufferedWriter bufferedwriter;
        // Sentinel for authentication
        private Boolean authenticated = false;
        EntityManager manager = ActivationServiceServer.manager;

        public Communicator(SSLSocket clientSocket) {
            this.socket = clientSocket;
        }
        
        /*
         * Read file in Hex format
         */
        private String getHexFile(String filename) throws FileNotFoundException,
                IOException{
            FileInputStream in = new FileInputStream(filename);
            int read;
            StringBuilder data = new StringBuilder();
            int charCount = 0, wordCount = 0;
            while((read = in.read()) != -1){
                if (wordCount >= 7) {
                    data.append("\n");
                    charCount = 0;
                    wordCount = 0;
                }
                else {
                    if (charCount >= 1) {
                        data.append(" ");
                        charCount = 0;
                        wordCount++;
                    }
                }
                data.append(Integer.toHexString(read));
                charCount++;
            }
            return data.toString();
        }

        @Override
        public void run() {
            
            JSONObject json;
            
            try {
                // Get Inout/Output streams and buffers
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
                
                // Keep reading client output
                while ((string = bufferedreader.readLine()) != null) {
                    json = (JSONObject)new JSONParser().parse(string);
                    JSONObject reply = new JSONObject();
                    String message = (String) json.get("message");
                    System.out.println(string);
                    Boolean sendMsg = true;
                    // Ask client to authenticate
                    if (!authenticated) {
                        // Authenticate client by the username/password supplied
                        if (message.equals("readKey")) {
                            String username = (String) json.get("username");
                            String password = (String) json.get("password");
                            // Chek if username/password are not set
                            if (username != null && password != null) {
                                try {
                                    // Auth logic here
                                    if (Authenticator.authenticate(username,
                                     password, manager)) {
                                        authenticated = true;
                                        reply.put("message", "authenticated");
                                    } // Authentication Failed
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
                                catch (SQLException ex) {
                                    Logger.getLogger(ActivationServiceServer
                                            .class.getName())
                                            .log(Level.SEVERE, null, ex);
                                }
                            }
                            // Authentication Failed, username/password not
                            // present
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
                        } // Ask client to authenticate first
                        else {
                            reply.put("message", "authenticate");
                        }
                    }
                    else {
                        // Client is asking for list of Schools
                        if (message.equals("getSchools")) {
                            try {
                                Schools[] schools = manager.find(Schools.class);
                                JSONArray sschools = new JSONArray();
                                for (Schools school: schools) {
                                    JSONObject sschool = new JSONObject();
                                    Groups[] groups = manager.find(Groups.class,
                                            Query.select().where("id="
                                            + school.getGroup_id()));
                                    sschool.put("name", school.getName()
                                            + "(" + groups[0].getName() + ")");
                                    sschool.put("id", school.getId());
                                    sschools.add(sschool);
                                }
                                reply.put("message", "readSchools");
                                reply.put("schools", sschools);
                            }
                            catch (SQLException ex) {
                                Logger.getLogger(ActivationServiceServer
                                        .class.getName()).log(Level.SEVERE,
                                            null, ex);
                            }
                        } // Client has requested to add a machine
                        else if (message.equals("addMachine")) {
                            // Get machine object sent by client in reply
                            JSONObject machine =
                                    (JSONObject) json.get("machine");
                            // Retrieve Machine name from Reply
                            String name = (String) machine.get("name");
                            // Retrieve School ID from Reply
                            int school_id = Integer.parseInt(
                                    (String) machine.get("schoolId"));
                            // Retrieve Machine ID from Reply
                            String machine_id =
                                    (String) machine.get("machineId");
                            // If everything is set, go further
                            if (name != null && school_id != 0
                                    && machine_id != null) {
                                try {
                                    Schools[] schools = manager.find(Schools.class,
                                        Query.select().where("id=" + school_id));
                                    
                                    // Check if the School is valid
                                    if (schools.length <= 0) {
                                        reply.put("message", "invalidSchool");
                                    }
                                    else {
                                        Machines[] machines = manager.find(
                                                Machines.class,Query.select()
                                                .where("machine_id='"
                                                + machine_id + "'"));
                                        // Check if the machine already exists
                                        if (machines.length > 0) {
                                            reply.put("message",
                                                    "machineExists");
                                        }
                                        else {
                                            // Register new client here
                                            // Generate a new Keystore for client
                                            String[] command = {"keytool",
                                                "-genkey", "-keyalg", "RSA",
                                                "-alias", machine_id,
                                                "-keystore", machine_id + ".jks",
                                                "-storepass", 
                                                ActivationServiceServer
                                                    .CLIENT_KEY_STORE_PASS,
                                                "-validity", "36500",
                                                "-keysize", "2048"};

                                            ProcessBuilder pb =
                                                    new ProcessBuilder(command)
                                                    .redirectErrorStream(true);
                                            pb.directory(new File(
                                                ActivationServiceServer.KEY_DIR));
                                            Process p = pb.start();
                                            InputStream is = p.getInputStream();
                                            OutputStream os = p.getOutputStream();
                                            InputStreamReader isr =
                                                    new InputStreamReader(is);
                                            OutputStreamWriter osr =
                                                    new OutputStreamWriter(os);
                                            BufferedReader br =
                                                    new BufferedReader(isr);
                                            PrintWriter pw =
                                                    new PrintWriter(osr);
                                            String line;
                                            while ((line = br.readLine())!= null) {
                                                if (line.equals("  [Unknown]:"
                                                        + "  Is CN=Unknown,"
                                                        + " OU=Unknown,"
                                                        + " O=Unknown,"
                                                        + " L=Unknown,"
                                                        + " ST=Unknown,"
                                                        + " C=Unknown"
                                                        + " correct?"))
                                                    pw.println("yes");
                                                else
                                                    pw.println("");
                                                pw.flush();
                                                //System.out.println(line);
                                            }
                                            
                                            // Export certificate from the
                                            // keystore
                                            String[] command1 = {"keytool",
                                                "-export", "-alias", machine_id,
                                                "-file", machine_id + ".crt",
                                                "-keystore", machine_id + ".jks",
                                                "-storepass",
                                                ActivationServiceServer
                                                    .CLIENT_KEY_STORE_PASS};
                                            
                                            pb = new ProcessBuilder(command1)
                                                    .redirectErrorStream(true);
                                            pb.directory(new File(
                                                ActivationServiceServer.KEY_DIR));
                                            p = pb.start();
                                            is = p.getInputStream();
                                            isr = new InputStreamReader(is);
                                            br = new BufferedReader(isr);
                                            
                                            while ((line = br.readLine())!= null) {
                                            }
                                            
                                            java.util.Date dt =
                                                    new java.util.Date();
                                            java.text.SimpleDateFormat sdf =
                                            new java.text.SimpleDateFormat(
                                                    "yyyy-MM-dd HH:mm:ss");
                                            String currentTime = sdf.format(dt);
                                            
                                            // Create a new machine in DB
                                            Machines record = manager.create(
                                                    Machines.class);  
                                            record.setMachine_id(machine_id);
                                            record.setSchool_id(school_id);
                                            record.setName(name);
                                            record.setSsl_key(new String(
                                                    getHexFile(
                                                        ActivationServiceServer
                                                            .KEY_DIR + "/"
                                                        + machine_id + ".crt")));
                                            record.setCreated_at(currentTime);
                                            record.setUpdated_at(currentTime);
                                            System.out.println(record);
                                            
                                            try {
                                                
                                                // Save the record
                                                
                                                record.save();
                                            
                                                // Import the certificate into 
                                                // keystore
                                                command = new String(
                                                        "keytool -import -file "
                                                        + machine_id + ".crt -alias"
                                                        + " " + machine_id
                                                        + " -keystore "
                                                        + ActivationServiceServer
                                                            .TRUST_STORE
                                                        + " -storepass "
                                                        + ActivationServiceServer
                                                            .TRUST_STORE_PASS +
                                                        " -noprompt")
                                                    .split(" ");
                                                pb = new ProcessBuilder(command);
                                                pb.directory(new File(
                                                     ActivationServiceServer
                                                        .KEY_DIR));
                                                p = pb.start();
                                                try {
                                                    p.waitFor();
                                                } catch (InterruptedException ex) {
                                                    Logger.getLogger(
                                                            ActivationServiceServer
                                                            .class.getName())
                                                            .log(Level.SEVERE,
                                                                null, ex);
                                                }
                                                is = p.getInputStream();
                                                isr = new InputStreamReader(is);
                                                br = new BufferedReader(isr);
                                                while ((line = br.readLine())
                                                        != null) {
                                                    System.out.println(line);
                                                }
                                                // Added the machine
                                                reply.put("message", "addedMachine");
                                                
                                            }
                                            catch (Exception e) {
                                                reply.put("message", "serverError");
                                            }
                                        }
                                    }
                                }
                                catch (SQLException ex) {
                                    Logger.getLogger(ActivationServiceServer
                                            .class.getName()).log(Level.SEVERE
                                                , null, ex);
                                }
                                
                            }
                            else {
                                // Empty Fields
                                reply.put("message", "emptyFields");
                            }
                        } // Download the Key File for the machine
                        else if (message.equals("getKey")) {
                            String machine_id = (String) json.get("machineId");
                            Machines[] machines = null;
                            try {
                                machines = manager.find(Machines.class,
                                        Query.select().where("machine_id='"
                                        + machine_id + "'"));
                            }
                            catch (SQLException ex) {
                                Logger.getLogger(ActivationServiceServer.class
                                        .getName()).log(Level.SEVERE, null, ex);
                            }
                            if (machines.length > 0) {
                                
                                // Create path for the file
                                File file = new File(ActivationServiceServer
                                                            .KEY_DIR + "/"
                                                        + machine_id + ".jks");
                                
                                reply.put("message", "machineExists");
                                reply.put("size", String.valueOf(file.length()));
                                
                                bufferedwriter.write(reply.toJSONString() + "\r\n");
                                bufferedwriter.flush();

                                byte[] buf = new byte[4096];
                                int len;

                                FileInputStream fis = new FileInputStream(file);
                                
                                // Send file
                                while((len=fis.read(buf))!=-1) {
                                    outputstream.write(buf, 0, len);
                                    outputstream.flush();
                                }
                                fis.close();
                                
                                sendMsg = false;
                            }
                            else {
                                reply.put("message", "machineDoesNotExist");
                            }
                        }
                    }
                    
                    if (sendMsg) {
                        bufferedwriter.write(reply.toJSONString() + "\r\n");
                        bufferedwriter.flush();
                    }
                }
            }
            catch (IOException ex) {
                Logger.getLogger(ActivationServiceServer.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            catch (ParseException ex) {
                Logger.getLogger(ActivationServiceServer.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    
    }
}
