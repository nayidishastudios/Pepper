/**
 * @author Deepanshu
 * @email deepanshumehndiratta[at]gmail.com
 */

package activationserviceclient;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.math.BigInteger;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
 * Main class which also uses Swing
 */

public class ActivationServiceClient extends JFrame {
    
    // Set server URL and port
    private static final String SERVER = "pepper.nayidishastudios.com";
    private static final int PORT = 10000;
    // Initialize all 4 panels
    private JPanel loginPanel = null;
    private JPanel connectionLostPannel = null;
    private JPanel optionPanel = null;
    private JPanel registerPanel = null;
    // Text/password fields that have to be accessed on Button clicks
    private JTextField Username;
    private JPasswordField Password;
    // School ID is set by drop down action listener (Localized, not class method)
    private String schoolId = null;
    // Machine Name
    private JTextField Machine;
    // Object of Communicator class, initialized on login
    private Communicator client;
    
    /*
     * Load login screen on Application startup
     */
    public ActivationServiceClient() {
        
       setTitle("Machine Manager - Nayi Disha Studios");
       setSize(600, 400);
       setLocationRelativeTo(null);

       splashScreen();
       loginPanel("Please Login to continue");
       
       setDefaultCloseOperation(EXIT_ON_CLOSE);        
    }
    
    /*
     * Waiting screen (Local panel)
     */
    private void splashScreen() {
        
        JPanel loading = new JPanel();
        
        getContentPane().removeAll();
        
        getContentPane().validate();
        getContentPane().repaint();
        
        getContentPane().add(loading);

        loading.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        JLabel Notification = new JLabel("Loading. Please wait.");
        
        // Notification Label
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(0,20,50,20);
        loading.add(Notification,c);
        
        getContentPane().validate();
        getContentPane().repaint();
        
    }
    
    /*
     * Login Panel (Pass message to be displayed on Login Screen)
     */
    private void loginPanel(String notification) {

        loginPanel = new JPanel();
        
        getContentPane().removeAll();
        
        getContentPane().validate();
        getContentPane().repaint();
        
        getContentPane().add(loginPanel);

        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        JLabel Notification = new JLabel(notification);
       
        JButton Login = new JButton("Login");
	JLabel ULabel = new JLabel("Username");
	JLabel PLabel = new JLabel("Password");
        Username = new JTextField();
        Password = new JPasswordField();
        
        // Notification Label
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(0,20,50,20);
        loginPanel.add(Notification,c);
        
        c = new GridBagConstraints();
        
        // Username Label
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3,20,3,20);
        loginPanel.add(ULabel,c);
        
        c = new GridBagConstraints();
        
        // Username Field
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth  = 2;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3,20,3,20);
        loginPanel.add(Username,c);
        
        c = new GridBagConstraints();
        
        // Username Label
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3,20,3,20);
        loginPanel.add(PLabel,c);
        
        c = new GridBagConstraints();
        
        // Username Field
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth  = 2;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3,20,3,20);
        loginPanel.add(Password,c);
        
        c = new GridBagConstraints();
        
        // Username Field
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth  = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(50,20,50,20);
        loginPanel.add(Login,c);
        
        Login.addActionListener(new MyAction());
        
        getContentPane().validate();
        getContentPane().repaint();
        
    }
    
    /*
     * Options after Login (Download key or setup new machine)
     */
    private void optionPanel() {
        
        getContentPane().removeAll();
        
        getContentPane().validate();
        getContentPane().repaint();

        optionPanel = new JPanel();
        
        getContentPane().add(optionPanel);

        optionPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        JLabel Notification = new JLabel("Please choose machine type.");
        
        // Notification Label
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(0,20,50,20);
        optionPanel.add(Notification,c);
        
        c = new GridBagConstraints();
       
        JButton OldMachine = new JButton("This is an old machine.");
        JButton NewMachine = new JButton("Set-up the Machine"
                + " for the first time.");
        
        // Old Machine
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3,20,3,20);
        optionPanel.add(OldMachine,c);
        
        c = new GridBagConstraints();
        
        // New Machine
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = GridBagConstraints.REMAINDER/2;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3,20,3,20);
        optionPanel.add(NewMachine,c);
        
        OldMachine.addActionListener(new MyAction());
        NewMachine.addActionListener(new MyAction());
        
        getContentPane().validate();
        getContentPane().repaint();
        
    }
    
    /*
     * Screen for choosing options for adding a new machine
     */
    private void registerPanel() throws ParseException, IOException {
        
        getContentPane().removeAll();
        
        getContentPane().validate();
        getContentPane().repaint();

        registerPanel = new JPanel();
        
        getContentPane().add(registerPanel);

        registerPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        JLabel Notification = new JLabel("Please enter the machine details.");
        JLabel SLabel = new JLabel("School");
        JLabel MLabel = new JLabel("Machine Name");
        Machine = new JTextField();
        JButton Register = new JButton("Register");
        
        // Notification Label
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(0,20,50,20);
        registerPanel.add(Notification,c);
        
        c = new GridBagConstraints();
        
        // Username Label
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3,20,3,20);
        registerPanel.add(SLabel,c);
        
        c = new GridBagConstraints();
        
        final JComboBox schoolList;
        
        final HashMap<String, String> schools = client.getSchools();
        
        String[] schoolString = schools.values().toArray(
                new String[schools.values().size()]);

        //Create the combo box
        //Indices start at 0
        
        // Update the Class variable "schoolId" with the ID of school on
        // change of value in drop box corresponding to the school name
        schoolList = new JComboBox(schoolString);
        schoolId = schools.keySet().toArray(new String[schools.size()])[0];
        schoolList.addActionListener(new MyAction() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("comboBoxChanged")) {
                    int i = 0;
                    for (String ID: schools.keySet()) {
                        if (i == schoolList.getSelectedIndex()) {
                            schoolId = ID;
                            break;
                        }
                        i++;
                    }
                    //System.out.println(schoolId);
                }
            }
        });
        
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth  = 2;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3,20,3,20);
        registerPanel.add(schoolList,c);
        
        c = new GridBagConstraints();
        
        // Machine Name Label
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3,20,3,20);
        registerPanel.add(MLabel,c);
        
        c = new GridBagConstraints();
        
        // Machine Name Field
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth  = 2;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3,20,3,20);
        registerPanel.add(Machine,c);
        
        c = new GridBagConstraints();
        
        // Username Field
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth  = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(50,20,50,20);
        registerPanel.add(Register,c);
        
        Register.addActionListener(new MyAction());
        
        getContentPane().validate();
        getContentPane().repaint();
        
    }
    
    /*
     * Class to listen to all button clicks
     */
    private class MyAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            
            // Login button clicked
            if (e.getActionCommand().equals("Login")) {
                
                // Get username/password from text/password fields
                String username = Username.getText();
                String password = Password.getText();
                
                // Notify user if username/password is blank
                if (username == null || username.equals("")
                    || password == null || password.equals("")) {
                    JOptionPane.showMessageDialog(ActivationServiceClient.this,
                        "Username/password can't be blank.",
                        "Error Message",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Initialize client communicator object and connect to the server
                client = new Communicator(getMachineId(), username, password);
                try {
                    client.begin(SERVER, PORT);
                }
                catch (ParseException ex) {
                    Logger.getLogger(ActivationServiceClient.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
                catch (IOException ex) {
                    Logger.getLogger(ActivationServiceClient.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
            // Register (New machine) button clicked
            if (e.getActionCommand().equals("Register")) {
                
                // Get machine name
                String machine = Machine.getText();
                
                // Check for a blank machine name
                if (machine == null || machine.equals("")) {
                    JOptionPane.showMessageDialog(ActivationServiceClient.this,
                        "Please enter a name for the machine.",
                        "Error Message",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Show loading screen
                splashScreen();
                
                // Register machine
                try {
                    client.registerMachine();
                }
                catch (Exception ex) {
                    Logger.getLogger(ActivationServiceClient.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
            // Show New Machine Registration screen
            if (e.getActionCommand().equals("Set-up the Machine"
                + " for the first time.")) {
                try {
                    registerPanel();
                } catch (ParseException ex) {
                    Logger.getLogger(ActivationServiceClient.class.getName())
                            .log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ActivationServiceClient.class.getName())
                            .log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(ActivationServiceClient.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
            // Download Key File for the client
            if (e.getActionCommand().equals("This is an old machine.")) {
                
                client.getKey();
                
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException, IOException {
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // On run, initialize the Object of the class
                ActivationServiceClient ex = new ActivationServiceClient();
                ex.setVisible(true);
            }
        });
    }
    
    /*
     * Get MD5 Hashed Machine ID (Motherboard ID + HDD ID)
     */
    public static String getMachineId() {
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
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(out));
            ActivationServiceClient.log(new String(out.toByteArray()));
        }
        
        return hashtext;
    }
    
    /*
     * Log all the Messages/Errors
     */
    public static void log(String message) {
        //System.out.println(message);
    }
    
    /*
     * Class to communicate with the server
     */
    private class Communicator {
        // Socket object
        private SSLSocket socket;
        // Server URL and port
        private String server;
        private int port;
        // Machine ID (Retrieved by getMachineId of Main class)
        private String machineId;
        // Sentinel for authentication
        private Boolean authenticated = false;
        // Input/Output Streams and Buffers
        private InputStream inputstream;
        private OutputStream outputstream;
        private BufferedReader bufferedreader;
        private BufferedWriter bufferedwriter;
        // Connection and Re-connect counts
        private int reconnectCounts = 0;
        private int connectCounts = 0;
        // Username and Password supplied on Object creation
        private String username;
        private String password;
        
        /*
         * Initialize with Machine ID and Username/Password
         */
        Communicator(String machineId, String username, String password) {
            this.machineId = machineId;
            this.username = username;
            this.password = password;
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
         * Establish connection with client and call Authenticate
         */
        public void begin(String server, int port) throws ParseException,
                IOException {

            this.server = server;
            this.port = port;
            authenticated = false;
            
            // Try to open a socket with server
            try {
                
                // Set keyStore and trustStore Paths and Passwords
                
                SSLSocketFactory sslsocketfactory = loadClientStores("KeyStore",
                        "hl6d1vF8kow63ca6l3t9bO8YWD6x8GkgsKb2x0DISwng428LnbFqRr"
                        + "uC1DrU2yk6deb883tYpgiw1a3gEQMDEshxArbr21XP2Y8v",
                        "TrustStore", "y5CSGUULYiXBg0i8k6W12GwuG23kuS7lYWPvHODi"
                        + "BNJshV55AqgL1ZnGjtfTS0LZsYjQDYmjClum2f25fPPtNvjEEwCX"
                        + "42OZ63Mj");
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
                ActivationServiceClient.log("Disconnected from server.");
                // Return if already failed 11 times after showing message
                if (connectCounts > 10) {
                    loginPanel("Please Login to continue.");
                    JOptionPane.showMessageDialog(ActivationServiceClient.this,
                        "Error connecting to the server.\n"
                            + "Please check your internet connection.",
                        "Error Message",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Re-connect to the server if less than 11 attempts
                connectCounts++;
                // Wait for 5 seconds before reconnect
                try {
                    Thread.sleep(1000);
                }
                catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                finally {
                    // Begin connection
                    begin(server, port);
                    return;
                }
            }
            catch (SSLHandshakeException ssl) {
                ActivationServiceClient.log("Disconnected from server.");
                // Return if already failed 11 times after showing message
                if (connectCounts > 10){
                    JOptionPane.showMessageDialog(ActivationServiceClient.this,
                        "Unable to establish a connection to the server.\n"
                            + "Please check your internet connecton.",
                        "Error Message",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Re-connect to the server if less than 11 attempts
                connectCounts++;
                // Wait for 5 seconds before reconnect
                try {
                    Thread.sleep(1000);
                }
                catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                finally {
                    // Begin Connection
                    begin(server, port);
                    return;
                }
            }
            catch (Exception exception) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                exception.printStackTrace(new PrintStream(out));
                ActivationServiceClient.log(new String(out.toByteArray()));
                // Return if already failed 11 times after showing message
                if (connectCounts > 10){
                    JOptionPane.showMessageDialog(ActivationServiceClient.this,
                        "An error occurred please restart"
                            + " the application and try again.",
                        "Error Message",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Re-connect to the server if less than 11 attempts
                connectCounts++;
                // Wait for 5 seconds before reconnect
                try {
                    Thread.sleep(1000);
                }
                catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                finally {
                    // Begin Connection
                    begin(server, port);
                    return;
                }
            }
            // Authenticate client once connected
            reconnectCounts = 0;
            connectCounts = 0;
            authenticate();
        }
        
        /*
         * Reconnect cleint on interrupted connection
         */
        public void reconnect() throws ParseException, IOException {

            // Return if already failed 11 times after showing message
            if (reconnectCounts > 10){
                loginPanel("Please Login to continue.");
                JOptionPane.showMessageDialog(ActivationServiceClient.this,
                    "Error connecting to the server.\n"
                        + "Please check your internet connection.",
                    "Error Message",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Wait for 5 seconds before reconnect
            try {
                Thread.sleep(1000);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            finally {
                reconnectCounts++;
                authenticated = false;
                // Re-connect to the server if less than 11 attempts
                begin(server, port);
            }

        }
        
        /*
         * Authenticate with the server by using the Username/Password supplied
         * during object creation
         */
        public void authenticate() throws ParseException, IOException {

            String string;
            Boolean exit = false;
            JSONObject json;
            
            // If not authenticated already, commence
            if (!authenticated) {

                try {
                    // Keep reading output from server
                    while ((string = bufferedreader.readLine()) != null) {
                        
                        // Log server output
                        ActivationServiceClient.log(string);
                        
                        // Parse JSON from string
                        json = (JSONObject)new JSONParser().parse(string);
                        JSONObject reply = new JSONObject();
                        String message = (String) json.get("message");

                        // Server asking for authentication
                        if (message.equals("authenticate")) {
                            ActivationServiceClient.log("System asking for"
                                    + " authorization.");
                            // Send username/password to server
                            reply.put("message", "readKey");
                            reply.put("username", username);
                            reply.put("password", password);
                            bufferedwriter.write(reply.toJSONString() + "\r\n");
                            bufferedwriter.flush();
                        } // Authentication failed
                        else if (message.equals("authenticationFailed")) {
                            // Show the login panel again
                            loginPanel("Please Login to continue.");
                            ActivationServiceClient.log("Authentication Failed.");
                            // Notify user using Pop Up
                            JOptionPane.showMessageDialog(ActivationServiceClient.this,
                                "Authentication Failed. Try again.",
                                "Error Message",
                                JOptionPane.ERROR_MESSAGE);
                            return;
                        } // Authentication with server successful
                        else if (message.equals("authenticated")) {
                            ActivationServiceClient.log("Authenticated.");
                            authenticated = true;
                            // Show Options after login
                            optionPanel();
                            // Notify user using Pop Up
                            JOptionPane.showMessageDialog(ActivationServiceClient.this,
                                "Successfully logged in.");
                            return;
                        } // Server behaved unexpectedly, re-login
                        else {
                            // Show Login Panel
                            loginPanel("Please Login to continue.");
                            // Notify user using Pop Up
                            JOptionPane.showMessageDialog(ActivationServiceClient.this,
                                "Server responded in an unexpected way. Please"
                                    + " restart the application and try again.",
                                "Error Message",
                                JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                }
                catch (SocketException se) {
                    ActivationServiceClient.log("Socket Exception, re-establishing"
                            + " connection.");
                    reconnect();
                    return;
                }
                catch (SSLHandshakeException ssl) {
                    ActivationServiceClient.log("SSL Exception, re-establishing"
                            + " connection");
                    reconnect();
                    return;
                }
                catch (IOException e) {
                    ActivationServiceClient.log("IO Exception, re-establishing"
                            + " connection.");
                    reconnect();
                    return;
                }
                catch (Exception e) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    e.printStackTrace(new PrintStream(out));
                    ActivationServiceClient.log(new String(out.toByteArray()));
                    reconnect();
                    return;
                }
            }
            
            // If already authenticated, show the Login screen and
            // notify using Pop Ups
            optionPanel();
            JOptionPane.showMessageDialog(ActivationServiceClient.this,
                "You are already logged in.");
            return;
        }

        /*
         * Get Schools list from server and make a HashMap from it
         */
        public HashMap<String,String> getSchools() {

            JSONObject reply = new JSONObject();
            Map<String, String> sschools = null;
            
            try {

                // Ask server for list of schools
                reply.put("message", "getSchools");
                bufferedwriter.write(reply.toJSONString() + "\r\n");
                bufferedwriter.flush();

                String string = bufferedreader.readLine();

                JSONObject json = (JSONObject)new JSONParser().parse(string);
                String message = (String) json.get("message");

                sschools = new HashMap<String, String>();

                // If server sends schools list, read it and convert it to a
                // HashMap
                if (message.equals("readSchools")) {
                    // Read schools JSONArray from JSONObject reply
                    JSONArray schools = (JSONArray) json.get("schools");
                    // Iterate over the JSONArray to create a HashMap
                    for (int i = 0; i < schools.size(); i++) {
                        JSONObject school = (JSONObject) schools.get(i);
                        sschools.put((String) school.get("id"),
                                (String) school.get("name"));
                    }
                }
            }
            catch (Exception e) {
                client = null;
                // Re-login to continue, there was an exception
                loginPanel("Please Login to continue");
                // Notify client via Pop Up
                JOptionPane.showMessageDialog(ActivationServiceClient.this,
                    "Disconnected from server.\n"
                        + "Please log back in again.",
                    "Error Message",
                    JOptionPane.ERROR_MESSAGE);
                // Return an empty HashMap to avoid exception
                return new HashMap<String, String>();
            }
            
            // Return the list of schools
            return (HashMap<String, String>) sschools;
        }

        /*
         * Register machine on the server
         */
        public void registerMachine() {
            String machine;
            JSONObject reply;
            
            try {
                machine = Machine.getText();
                reply = new JSONObject();
                
                JSONObject Machine = new JSONObject();
                
                // Get Machine ID, School ID and Machine Name and build a 
                // machine object to send to the server
                Machine.put("name", machine);
                Machine.put("schoolId", schoolId);
                Machine.put("machineId", getMachineId());
                
                // Send server the machine details
                reply.put("message", "addMachine");
                reply.put("machine", Machine);
                
                bufferedwriter.write(reply.toJSONString() + "\r\n");
                bufferedwriter.flush();

                String string = bufferedreader.readLine();
                
                // Read server message
                JSONObject json = (JSONObject)new JSONParser().parse(string);
                String message = (String) json.get("message");
                
                // You supplied an Invalid School
                if (message.equals("invalidSchool")) {
                    // Show the new School Registration screen again
                    registerPanel();
                    // Notify user via Pop Up
                    JOptionPane.showMessageDialog(ActivationServiceClient.this,
                        "Please choose a valid school.",
                        "Error Message",
                        JOptionPane.ERROR_MESSAGE);
                } // The machine already exists
                else if (message.equals("machineExists")) {
                    // Show the Options screen again
                    optionPanel();
                    // Notify user about it via Pop Up
                    JOptionPane.showMessageDialog(ActivationServiceClient.this,
                        "This machine is already registered.\n"
                            + "Or a machine with a similar Machine ID exists.\n"
                            + "Please contact the administrator.",
                        "Error Message",
                        JOptionPane.ERROR_MESSAGE);
                } // The machine was successfully added
                else if (message.equals("addedMachine")) {
                    // Notify user about it
                    JOptionPane.showMessageDialog(ActivationServiceClient.this,
                        "Machine successfully added, fetching required keys"
                            + " for activation.");
                    // Download the Key file for the machine
                    getKey();
                } // Server responded in an unexpected way
                else {
                    client = null;
                    // Show the Login Screen
                    loginPanel("Please Login to continue");
                    // Noify the user via Pop Up
                    JOptionPane.showMessageDialog(ActivationServiceClient.this,
                        "Could not understand server reply.\n"
                            + "Please log back in again.",
                        "Error Message",
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } // Server responded in an unexpected way
            catch (ParseException ex) {
                client = null;
                // Show the Login Screen
                loginPanel("Please Login to continue");
                // Noify the user via Pop Up
                JOptionPane.showMessageDialog(ActivationServiceClient.this,
                    "Could not understand server reply.\n"
                        + "Please log back in again.",
                    "Error Message",
                    JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(ActivationServiceClient.class.getName())
                        .log(Level.SEVERE, null, ex);
            } // Server responded in an unexpected way
            catch (IOException ex) {
                client = null;
                // Show the Login Screen
                loginPanel("Please Login to continue");
                // Noify the user via Pop Up
                JOptionPane.showMessageDialog(ActivationServiceClient.this,
                    "Disconnected from server.\n"
                        + "Please log back in again.",
                    "Error Message",
                    JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(ActivationServiceClient.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
        
        /*
         * Download the Key file for a registered machine
         */
        public void getKey() {
            
            JSONObject reply;
                
            try {
                
                // Ask server for the Key file, send the Machine ID.
                reply = new JSONObject();       
                reply.put("message", "getKey");
                reply.put("machineId", getMachineId());     
                
                bufferedwriter.write(reply.toJSONString() + "\r\n");
                bufferedwriter.flush();

                String string = bufferedreader.readLine();

                // Read server reply
                JSONObject json = (JSONObject)new JSONParser().parse(string);
                String message = (String) json.get("message");
                
                // Machine exists, proceed to downloading key
                if (message.equals("machineExists")) {
                    
                    // Get size of Key file from reply
                    int size = Integer.parseInt((String) json.get("size"));
                    
                    ActivationServiceClient.log("Starting download of Key.");
                    
                    // Create FileOutputStream in the pepper folder
                    FileOutputStream fos = new FileOutputStream("../pepper/"
                            + "MyKey");
                    
                    // Open a Buffered Output Stream on the FOS
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    int bufferSize = socket.getReceiveBufferSize();

                    ActivationServiceClient.log("Buffer size: " + bufferSize);

                    byte[] bytes = new byte[bufferSize];

                    int count, total = 0;

                    // Start file transfer
                    while ((count = inputstream.read(bytes)) > 0) {
                        total += count;
                        bos.write(bytes, 0, count);

                        if (total >= size)
                            break;
                    }

                    bos.flush();
                    bos.close();
                    
                    client = null;
                    // Show the login screen
                    loginPanel("Please Login to continue.");
                    // Notify user about successful file transer
                    JOptionPane.showMessageDialog(ActivationServiceClient.this,
                        "Keys downloaded successfully.\n"
                            + "This machine can now recieve remote updates.");
                    return;
                    
                } // Machine Does not exist.
                else if (message.equals("machineDoesNotExist")) {
                    // Show the option screen again
                    optionPanel();
                    // Notify user via Pop Up
                    JOptionPane.showMessageDialog(ActivationServiceClient.this,
                        "This machine does not exist in the records.\n"
                            + "Please register it.",
                        "Error Message",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                } // Could not understand server reply
                else {
                    client = null;
                    // Show login screen again
                    loginPanel("Please Login to continue");
                    // Notify User via Pop Up
                    JOptionPane.showMessageDialog(ActivationServiceClient.this,
                        "Could not understand server reply.\n"
                            + "Please log back in again.",
                        "Error Message",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } // Some error occurred, User has to re-login
            catch (ParseException ex) {
                client = null;
                // Show login screen again
                loginPanel("Please Login to continue");
                // Notify User via Pop Up
                JOptionPane.showMessageDialog(ActivationServiceClient.this,
                    "Could not understand server reply.\n"
                        + "Please log back in again.",
                    "Error Message",
                    JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(ActivationServiceClient.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            catch (IOException ex) {
                client = null;
                // Show login screen again
                loginPanel("Please Login to continue");
                // Notify User via Pop Up
                JOptionPane.showMessageDialog(ActivationServiceClient.this,
                    "Disconnected from server.\n"
                        + "Please log back in again.",
                    "Error Message",
                    JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(ActivationServiceClient.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            
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
            ActivationServiceClient.log(new String(out.toByteArray()));
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
            ActivationServiceClient.log(new String(out.toByteArray()));
        }
        return result.trim();
    }
}
 
