/*
 *  Program created by Louis TARDY for Neurones IT
 *  This program will get the RAM, number of CPU, disk space and IOPS of the VMs on a VROps
 *  With this data it'll create a top of the VM that consume the most and generate a report in .xls format
 *  and will upload this data to a MySQL database
 *  May 17th - July 12th 2019
 */

package neurones.java.fr;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Launcher {

    private static final String USER = "root"; //User to connect to mysql
    private static final String PASS = ""; //Password to connect to mysql
    private static final String URL_SQL = "jdbc:mysql://localhost:3306/PCC?autoReconnect=true&useSSL=false"; //database
    private static final String PATH = System.getProperty("user.dir") + File.separator + "login.json";
    private static String pccName = ""; //ip of the pcc
    private static String username = ""; //Username to connect to the API
    private static String password = ""; //Password to connect to the API
    private static String url = "";
    private static int TOP = 20;


    public static void main(String[] args) {
        File file = new File(PATH);
        int i;
        int j;

        //Verifying args
        if (args.length > 0) {
            for (j = 0; j < args.length; j++) {
                switch (args[j]) {
                    case "-c": //Add a PCC to login.json
                        if (!file.exists() || file.isDirectory()) {
                            try {
                                Files.write(Paths.get(PATH), "{\n\t\"pccs\":[]}".getBytes(), StandardOpenOption.CREATE);
                            } catch (IOException err) {
                                System.err.println("Error while creating login.json : " + err.getCause());
                                return;
                            }
                        }
                        newPcc();
                        try {
                            Connection conn = DriverManager.getConnection(URL_SQL, USER, PASS);
                            PreparedStatement stmnt = conn.prepareStatement("CREATE TABLE `" + pccName + "`( `ID` INT NOT NULL AUTO_INCREMENT," +
                                    "`date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `name` VARCHAR(255),`RAM` BIGINT NULL DEFAULT NULL ," +
                                    "`CPU` BIGINT NULL DEFAULT NULL, `diskspace` BIGINT NULL DEFAULT NULL, `iops` BIGINT NULL DEFAULT NULL, PRIMARY KEY (`ID`))");
                            stmnt.executeUpdate();
                            stmnt.close();
                        } catch (SQLException err) {
                            System.err.println("Error :" + err.getCause());
                            return;
                        }
                        System.out.println("PCC added");
                        break;
                    case "-t": //Change value of the top (20 by default)
                        if (args[j + 1] == null) {
                            System.err.println("Put a number between 1 and 50, ex : java -jar prog.jar -t 32");
                            return;
                        }
                        try {
                            int arg = Integer.parseInt(args[j + 1]);
                            if (arg < 0 || arg > 50) {
                                System.err.println("Not a valid number (min 0, max 50), will use default value");
                                TOP = 20;
                            } else {
                                TOP = arg;
                            }
                        } catch (NumberFormatException err) {
                            System.err.println("Incorrect parameter for -t, will use default value");
                            TOP = 20;
                        }
                        break;
                    case "-r": //remove
                        deletePcc();
                        return;
                    case "--version":
                        System.out.println("1.0");
                        return;
                    case "-h":
                        System.out.println("Availables option:\n\t-t X : X VMs will be in the top (20 by default)\n\t-c : add a new pcc to login.json");
                        return;
                }
            }
        }

        try {
            if (file.exists() && !file.isDirectory()) {
                JSONObject object = new JSONObject(readFile(PATH));
                JSONArray pccs = object.getJSONArray("pccs");
                for (i = 0; i < pccs.length(); i++) {
                    JSONObject pcc = pccs.getJSONObject(i);
                    username = pcc.getString("username");
                    password = pcc.getString("password");
                    pccName = pcc.getString("url");
                    url = "https://vrops.pcc-" + pccName + ".ovh.com";
                    connect();
                }
            } else {
                Files.write(Paths.get(PATH), "{\n\t\"pccs\":[]}".getBytes(), StandardOpenOption.CREATE);
                newPcc();
                try {
                    Connection connOth = DriverManager.getConnection(URL_SQL, USER, PASS);
                    //Creation of the table that correspond to the PCC
                    PreparedStatement stmnt = connOth.prepareStatement("CREATE TABLE `" + pccName + "`( `ID` INT NOT NULL AUTO_INCREMENT," +
                            "`date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, `name` VARCHAR(255),`RAM` BIGINT NULL DEFAULT NULL ," +
                            "`CPU` BIGINT NULL DEFAULT NULL, `diskspace` BIGINT NULL DEFAULT NULL, `iops` BIGINT NULL DEFAULT NULL, PRIMARY KEY (`ID`))");
                    stmnt.executeUpdate();
                    stmnt.close();
                } catch (SQLException err) {
                    System.err.println("Error : " + err.getCause());
                }
            }
        } catch (IOException err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
    }

    /**
     * Scan user input for a new pcc then -> addPcc()
     */
    public static void newPcc() {
        System.out.println("Vrops IP (ex: 10-0-0-0):");
        pccName = scanf();
        url = "https://vrops.pcc-" + pccName + ".ovh.com";
        System.out.println("Username :");
        username = scanf();
        System.out.println("Password :");
        password = scanf();
        try {
            File login = new File(PATH);
            login.createNewFile();
            addPcc();
        } catch (IOException err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
    }

    /**
     * Add a pcc object to the login.json
     */
    public static void addPcc() {
        String curDir = System.getProperty("user.dir");
        String PATH = curDir + File.separator + "login.json";
        try {
            JSONObject object = new JSONObject(readFile(PATH));
            JSONArray pccs = object.getJSONArray("pccs");
            JSONObject pccObject = new JSONObject();
            JSONArray arr = new JSONArray();
            pccObject.put("url", pccName);
            pccObject.put("username", username);
            pccObject.put("password", password);
            arr.put(pccObject);
            pccs.put(pccObject);
            Files.write(Paths.get(PATH), object.toString().getBytes(), StandardOpenOption.CREATE);
        } catch (IOException err) {
            System.err.println("Error while writing : " + err.getCause());
        } catch (JSONException err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
    }

    /**
     * Get the input of the user
     *
     * @return Return a String of the content of the inpunt
     */
    public static String scanf() {
        String str = "";
        try {
            BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
            str = buffer.readLine();
            return str;
        } catch (Exception err) {
            System.err.println(err.getMessage() + " : " + err.getCause());
            return null;
        }
    }

    //Default connection

    /**
     * Default connection to the vrops API
     */
    public static void connect() {
        URL urlConn;
        try {
            Api api = new Api(url, username, password);
            api.setBasicUrl("pcc-" + pccName + ".ovh.com");

            urlConn = new URL(api.getUrl() + "alerts/");
            api.getTokenConn();
            if (api.curl("fr-FR", "dataAlerts", urlConn) == null) {
                System.err.println("Incorrect URL or username and password");
                return;
            }
            normal(api, url);
        } catch (MalformedURLException err) {
            System.err.println("Problem with the url : " + err.getCause());
        } catch (JSONException err) {
            System.err.println("Error : " + err.getCause());
        }
    }

    /**
     * Get all the resources (memory,cpu,diskspace,iops)
     *
     * @param api Instance of the api to have access to the methods
     */
    public static void getAll(Api api) {    //cpu|usagemhz_average  config|hardware|disk_Space mem|host_usage virtualDisk|commandsAveraged_average
        api.createIdAndName();
        api.getAllHardwareResource("mem|guest_usage"); // RAM
        api.getAllHardwareResource("config|hardware|num_Cpu"); // number of CPU
        api.getAllHardwareResource("config|hardware|disk_Space"); //Disk Space
        api.getAllHardwareResource("virtualDisk|commandsAveraged_average"); //IOPS
    }

    /**
     * Normal case of utilization : get all resources, parse alerts, create the tops and create the report.xls
     *
     * @param api Instance of the api to have access to methods and attributes
     */
    public static void normal(Api api, String url) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        String curDir = System.getProperty("user.dir");
        String dir = curDir + File.separator + "reports";
        String PATH = dir + File.separator + pccName + "_report_" + sdf.format(timestamp) + ".xls";
        ArrayList<VM> arrIops;
        ArrayList<VM> arrCpu;
        ArrayList<VM> arrDisk;
        ArrayList<VM> arrRam;
        ArrayList<Alert> arrAlerts;
        Excel excel = new Excel();

        //Connected
        System.out.println("Connected to : " + url);

        //Getting all the resources
        System.out.println("Getting all resources (may take a few minutes)...");
        getAll(api);

        //Parsing the alerts
        System.out.println("Done");
        System.out.println("Getting the alerts...");
        arrAlerts = api.parseJsonAlerts();

        //Tops
        //Tops IOPS
        System.out.println("Done");
        System.out.print("Getting the top " + TOP + " of iops...");
        arrIops = api.getTop("iops", TOP);
        //Top CPU
        System.out.println("Done");
        System.out.print("Getting the top " + TOP + " of cpu...");
        arrCpu = api.getTop("cpu", TOP);
        System.out.println("Done");
        //Top Diskspace
        System.out.print("Getting the top " + TOP + " of diskspace...");
        arrDisk = api.getTop("diskspace", TOP);
        System.out.println("Done");
        //Top RAM
        System.out.print("Getting the top " + TOP + " of ram...");
        arrRam = api.getTop("ram", TOP);
        System.out.println("Done");

        //Upload to MySQL
        System.out.print("Putting data into database...");
        mySql(arrRam);
        mySql(arrCpu);
        mySql(arrDisk);
        mySql(arrIops);
        System.out.println("Done");

        //Creating the report.xls
        System.out.print("Creating a report.xls...");
        try {
            File file = new File(dir);
            if (!file.exists() || (file.exists() && !file.isDirectory())) {
                file.mkdir();
            }
            excel.generate(arrRam, "Top of RAM (in Go)", "ram");
            excel.generate(arrIops, "Top of IOPS", "iops");
            excel.generate(arrCpu, "Top of vCPU", "cpu");
            excel.generate(arrDisk, "Top of diskspace (in Go)", "diskspace");
            excel.addAlerts(arrAlerts);
            FileOutputStream fileO = new FileOutputStream(PATH);
            excel.getWorkbook().write(fileO);
            excel.getWorkbook().close();
            fileO.close();
            System.out.println("Done");
        } catch (Exception err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
        System.out.println("Done");
    }

    /**
     * Just read a file
     *
     * @param path path to the file we want to read
     * @return Return a String of the content of a file
     */
    public static String readFile(String path) {
        String everything = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                everything = sb.toString();
            } catch (Exception err) {
                System.err.println(err.getCause() + " : " + err.getMessage());
                return null;
            } finally {
                br.close();
            }
            return everything;
        } catch (IOException err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
            return null;
        }
    }

    /**
     * Put the name, the RAM, the number of CPUs, the IOPS and the diskspace of each VM in the array
     * in the table corresponding to the PCC in a mysql database
     *
     * @param VMs array of VMs
     */
    public static void mySql(ArrayList<VM> VMs) {
        int i;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try {
            for (i = 0; i < VMs.size(); i++) {
                VM vm = VMs.get(i);
                Connection conn = DriverManager.getConnection(URL_SQL, USER, PASS);
                PreparedStatement stmnt = conn.prepareStatement("INSERT  INTO `" + pccName + "` (date,name,RAM,CPU,diskspace,iops) VALUES(?,?,?,?,?,?)");
                stmnt.setTimestamp(1, timestamp);
                stmnt.setString(2, vm.getName());
                stmnt.setLong(3, vm.getRamUsage() / 1000000);
                stmnt.setLong(4, vm.getCpuUsage());
                stmnt.setLong(5, vm.getDiskSpace());
                stmnt.setLong(6, vm.getIOPS());
                stmnt.executeUpdate();
                stmnt.close();
            }
        } catch (SQLException err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
    }

    /**
     * Remove a PCC from login.json
     */
    public static void deletePcc() {
        int i;
        int res;
        System.out.println("Which one do you want to delete? (-1 to cancel)");
        JSONObject object = new JSONObject(readFile(PATH));
        JSONArray pccs = object.getJSONArray("pccs");
        for (i = 0; i < pccs.length(); i++) { //List all the PCC saved
            JSONObject pcc = pccs.getJSONObject(i);
            System.out.println(i + 1 + "- " + pcc.getString("url"));
        }
        try {
            res = Integer.parseInt(scanf());
            if (res == -1) {
                System.out.println("Operation canceled");
            } else if (res > pccs.length() || res < -1 || res == 0) {
                System.err.println("Not a valid number");
            } else {
                pccs.remove(res - 1);
                new File(PATH).delete();
                Files.write(Paths.get(PATH), object.toString().getBytes(), StandardOpenOption.CREATE);
                System.out.println("Object removed!");
            }
        } catch (NumberFormatException err) {
            System.err.println("Not a valid number");
        } catch (IOException err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
    }
}
