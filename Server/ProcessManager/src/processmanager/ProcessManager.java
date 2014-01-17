/**
 * @author Deepanshu
 * @email deepanshumehndiratta[at]gmail.com
 */

package processmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

/*
 * Main Class implementing Daemon library (Works even if not used a s a daemon)
 */
public class ProcessManager implements Daemon {
    
    // Store processes which can be killed on exit
    public static List<Process> processes = new ArrayList<Process>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    // Close log file
                    for (Process process: processes) {
                        try {
                            process.destroy();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Runtime.getRuntime().exec("kill " +  new Scanner(new File(
                            "/home/git/web-service/tmp/pids/server.pid"))
                                .useDelimiter("\\Z").next());
                }
                catch (IOException ex) {
                    Logger.getLogger(ProcessManager.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });
        
        // Start threads to launch and monitor various services
        new Workers("web").start();
        new Workers("activation").start();
        new Workers("update").start();
    }

    @Override
    public void init(DaemonContext dc) throws DaemonInitException, Exception {
        System.out.println("initializing ...");
    }

    @Override
    public void start() throws Exception {
        System.out.println("starting ...");
        main(null);
    }

    @Override
    public void stop() throws Exception {
        System.out.println("stopping ...");
    }

    @Override
    public void destroy() {
        System.out.println("done.");
    }
}

class Workers extends Thread {
    
    String command;
    String Directory;

    @Override
    public void run() {
        
        while (true) {
            try {
                String[] command = this.command.split(" ");

                ProcessBuilder pb = new ProcessBuilder(command)
                        .redirectErrorStream(true);
                pb.directory(new File(Directory));
                Process p = pb.start();
                InputStream is = p.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                ProcessManager.processes.add(p);

                String line;

                while ((line = br.readLine())!= null) {
                    System.out.println(line);
                }
                
                p.waitFor();
                
                if (ProcessManager.processes.contains(p))
                    ProcessManager.processes.remove(p);
                
            }
            catch (InterruptedException ex) {
                Logger.getLogger(Workers.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            catch (IOException ex) {
                Logger.getLogger(Workers.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        
        }
        
    }
    
    public Workers(String what) {
        // Web Application
        if (what.equals("web")) {
            command = "rails s";
            Directory = "/home/git/web-service";
        }
        // Activation server
        else if (what.equals("activation")) {
            command = "java -jar ActivationServiceServer.jar";
            Directory = "/home/git/ActivationServiceServer";
        }
        // Update server
        else if(what.equals("update")) {
            command = "java -jar UpdateServiceServer.jar";
            Directory = "/home/git/UpdateServiceServer";
        }
        // No process found, exit
        else
            this.destroy();
        
    }
    
}
