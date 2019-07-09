package neurones.java.fr;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class VM {
    private String id;
    private String name;
    private long diskSpace = -1; //Disk space in Go
    private long ramUsage = -1; //RAM usage in Ko
    private long cpuUsage = -1; //Number of CPU
    private long IOPS = -1; //Input and Output Per Seconds
    private long diskRecomm = -1;
    private long cpuRecomm = -1;
    private long ramRecomm = -1;

    /**
     * Constructor
     *
     * @param id id of the vm needed
     */
    public VM(String id) {
        this.id = id;

        String curDir = System.getProperty("user.dir");
        String path = curDir + File.separator + "jsons" + File.separator + "vm.json";
        File file = new File(path);

        if (file.exists() && !file.isDirectory()) {
            JSONObject jsonObject = new JSONObject(readFile(path));
            if (jsonObject.getString(id) != null) {
                this.name = jsonObject.getString(id);
            }
        } else {
            this.name = "Not defined";
        }
    }

    /**
     * Getter IOPS
     * @return IOPS of the VM
     */
    public long getIOPS() {
        return IOPS;
    }

    /**
     * Setter IOPS
     * @param IOPS IOPS that will be set
     */
    public void setIOPS(long IOPS) {
        this.IOPS = IOPS;
    }

    /**
     * Getter ID
     * @return The ID of the VM
     */
    public String getId() {
        return id;
    }

    /**
     * Getter Name
     * @return The name of VM
     */
    public String getName() {
        return name;
    }

    /**
     * Setter Name
     * @param name The name that will be set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter Diskapce
     * @return The space on the disk of the VM
     */
    public long getDiskSpace() {
        return diskSpace;
    }

    /**
     * Setter diskapce
     * @param diskSpace Value of the diskspace that will be set
     */
    public void setDiskSpace(long diskSpace) {
        this.diskSpace = diskSpace;
    }

    /**
     * Getter RAM
     * @return Value of the RAM of the VM
     */
    public long getRamUsage() {
        return ramUsage;
    }

    /**
     * Setter RAM
     * @param ramUsage Value of the RAM that will be set
     */
    public void setRamUsage(long ramUsage) {
        this.ramUsage = ramUsage;
    }

    /**
     * Getter CPU
     * @return Number of vCPU of the VM
     */
    public long getCpuUsage() {
        return cpuUsage;
    }

    /**
     * Setter CPU
     * @param cpuUsage Number of vCPU that will be set
     */
    public void setCpuUsage(long cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    /**
     * Getter disk recommendation
     * @return The recommendation for the space disk
     */
    public long getDiskRecomm() {
        return diskRecomm;
    }

    /**
     * Setter disk recommendation
     * @param diskRecomm Value of the recommendation that will be set
     */
    public void setDiskRecomm(long diskRecomm) {
        this.diskRecomm = diskRecomm;
    }

    /**
     * Getter CPU recommendation
     * @return Value of the recommendation for CPU
     */
    public long getCpuRecomm() {
        return cpuRecomm;
    }

    /**
     * Setter CPU recommendation
     * @param cpuRecomm Value of the recommendation for CPU that will be set
     */
    public void setCpuRecomm(long cpuRecomm) {
        this.cpuRecomm = cpuRecomm;
    }

    /**
     * Getter RAM recommendation
     * @return Value of the recommendation for the RAM
     */
    public long getRamRecomm() {
        return ramRecomm;
    }

    /**
     * Setter RAM recommendation
     * @param ramRecomm Value of the recommendation for the RAM that will be set
     */
    public void setRamRecomm(long ramRecomm) {
        this.ramRecomm = ramRecomm;
    }

    /**
     * Just a read a file
     *
     * @param path Path of the file we want to read
     * @return Content of the file we read
     */
    public String readFile(String path) {
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
     * toString() method
     * @return String of the object
     */
    public String toString() {
        return "Name : "
                + this.name
                + ", id : "
                + this.id
                + ", disk space (-1 if not set) : "
                + this.diskSpace
                + ", ram usage (-1 if not set) : "
                + this.ramUsage
                + ", cpu usage (-1 if not set) : "
                + this.cpuUsage;
    }
}
