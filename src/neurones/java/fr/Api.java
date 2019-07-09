package neurones.java.fr;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Api {
    private String username; //Username of the account to get the token
    private String password; //Password of the account to get the token
    private String path;
    private String url; //URL of the vrops
    private String basicUrl; //Same as URL but without https://www. and /suite-api/api
    private String token = "Nupe"; //Token needed to connect to the API
    private long tokenValidity = -1; //Timestamp of the expiration of the token
    private static final int MAX = 50; //MAX value for the getTop(); method
    private static final String curDir = System.getProperty("user.dir"); //Get the current directory

    /**
     * Constructor
     *
     * @param urlstr Url, username and password inserted by the user and needed ton construct the object and create
     *               the directory to save the jsons later
     */
    public Api(String urlstr, String username, String password) {
        setUrl(urlstr);
        this.username = username;
        this.password = password;

        path = curDir + File.separator + "jsons";
        File dir = new File(path);
        if (!dir.exists() || (dir.exists() && !dir.isDirectory())) { //Create the JSON directory if not created yet
            dir.mkdir();
        }
    }

    /**
     * Set the url and add /suite-api/api/ to it
     *
     * @param urlstr basic vrops url like https://vrops.pcc-10-10-10-10.ovh.com
     */
    private void setUrl(String urlstr) {
        this.url = urlstr + "/suite-api/api/";
    }

    /**
     * Getter token
     * @return The token needed to connect to the API
     */
    public String getToken() {
        return token;
    }

    /**
     * Setter token
     * @param token Token that will be set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Getter token validity
     * @return The timestamp of the expiration of the token
     */
    public long getTokenValidity() {
        return tokenValidity;
    }

    /**
     * Setter token validity
     * @param tokenValidity Validity timestamp of the token that will be set
     */
    public void setTokenValidity(long tokenValidity) {
        this.tokenValidity = tokenValidity;
    }

    /**
     * Setter username
     *
     * @param username Username that will be set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Setter password
     *
     * @param password Password that will be set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter username
     *
     * @return The username of the object
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter password
     *
     * @return The password of the object
     */
    public String getPassword() {
        return password;
    }

    /**
     * Getter URL
     *
     * @return The url of the object
     */
    public String getUrl() {
        return url;
    }

    /**
     * Getter basic url
     * @return The basic url
     */
    public String getBasicUrl() {
        return basicUrl;
    }

    /**
     * Setter basic url
     * @param basicUrl Basic url that will be set
     */
    public void setBasicUrl(String basicUrl) {
        this.basicUrl = basicUrl;
    }

    /**
     * Connect to an URL put the response and a string
     * + write a json file with the response
     *
     * @param language   language of the json
     * @param nameOfFile name of the file that will be writen
     * @param url        which url to curl
     * @return return the same content as the .json but as a String
     */
    public String curl(String language, String nameOfFile, URL url) {
        String str = "{";
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "vRealizeOpsToken " + this.getToken()); //need authentication
            conn.setRequestMethod("GET");
            conn.setReadTimeout(10000); //in ms
            conn.setConnectTimeout(10000); //in ms
            conn.setRequestProperty("Accept", "application/json"); //to get the data in json instead of xml
            conn.setRequestProperty("Accept-Language", language);
            InputStream inputStr = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStr));

            while ((reader.read()) != -1) {
                str += reader.readLine(); //string to store
            }
            str = prettyJson(str);  //indent json
            writeFile(str, nameOfFile + ".json"); //write in a file the data
            conn.disconnect();
            return str;
        } catch (IOException err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
            return null;
        }
    }

    /**
     * Get the token needed to get access to the vrops api
     */
    public void getTokenConn() {
        String res = "{";
        JSONObject obj;
        URL urlConn;
        int responseCode;
        String params = "{\"username\" : \"" + this.getUsername() //Parameters we want to put in the POST
                + "\",\"password\" : \"" + this.getPassword()
                + "\",\"authSource\" : \"" + this.getBasicUrl() + "\"}";
        try {
            urlConn = new URL(this.getUrl() + "auth/token/acquire");
            HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json"); //We want to send JSON
            conn.setRequestProperty("Accept", "application/json"); //We want to receive JSON
            conn.setReadTimeout(10000); //in ms
            conn.setConnectTimeout(10000); //in ms
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {//Sending the post parameters
                wr.write(params.getBytes(), 0, params.length());
                wr.flush();
                wr.close();
            }

            responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { //Response code need to be == 200
                InputStream inputStream = new BufferedInputStream(conn.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                while (bufferedReader.read() != -1) {
                    res += bufferedReader.readLine();
                }
                obj = new JSONObject(res);
                this.setToken(obj.getString("token"));
                this.setTokenValidity(obj.getLong("validity"));
            } else {
                System.err.println("Error while trying to get the api token : response code :: " + responseCode);
                return;
            }
        } catch (Exception err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
    }

    /**
     * Indent a JSON String
     *
     * @param txt Text of a json we want to indent
     * @return Indented json
     */
    public String prettyJson(String txt) {
        String prettyTxt; //String to store the result
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(txt);
        prettyTxt = gson.toJson(jsonElement);
        return prettyTxt;
    }

    /**
     * Write a file with content and name given in parameters
     *
     * @param txt  Content of the file
     * @param name Name of the file
     */
    public void writeFile(String txt, String name) {
        createIfPossible(name);
        try {
            path = curDir + File.separator + "jsons" + File.separator + name; // curent directory + /jsons/ + name chosen
            Files.write(Paths.get(path), txt.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
    }

    //PARSERS

    /**
     * Get the name of a resource following an ID
     *
     * @param resourceId The id of the resource that we want the name
     * @return String Return the name of the resource
     */
    public String getResourceName(String resourceId) { //Need to save to a file Name + ID
        String name = "";
        String path = curDir + File.separator + "jsons" + File.separator + "names.json";
        JSONObject jsonObject = new JSONObject(readFile(path));

        try {
            if (jsonObject.getString(resourceId) != null) { //If the ID exist in the file
                name = jsonObject.getString(resourceId);
                return name;
            }
            return name;
        } catch (Exception err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
            return null;
        }
    }

    /**
     * Create a JSON File with "id of the resource" : "name of the resource"
     */
    public void createIdAndName() {
        int i;
        String name = "";
        JSONObject jsonString = new JSONObject(); //JSONObject to create the json for all the ids
        JSONObject jsonStringVM = new JSONObject(); //JSONObject to create the json for the vm
        String storage = "";
        try {
            URL urlConn = new URL(this.url + "resources/");
            curl("fr-FR", "dataResource", urlConn); //We need all the resources to get theirs IDs and their names
            path = curDir + File.separator + "jsons" + File.separator + "dataResource.json";
            JSONObject jsonObject = new JSONObject(readFile(path));
            JSONArray jsonArray = jsonObject.getJSONArray("resourceList");
            for (i = 0; i < jsonArray.length(); ++i) {
                JSONObject object = jsonArray.getJSONObject(i);
                JSONObject objectDeep = object.getJSONObject("resourceKey");
                name = objectDeep.getString("name");
                jsonString.put(object.getString("identifier"), name);
                if (objectDeep.getString("resourceKindKey").equals("VirtualMachine")) { //We don't want datastores or other things in the jsonStringVM
                    jsonStringVM.put(object.getString("identifier"), name);
                }
            }
            storage = prettyJson(jsonString.toString());
            writeFile(storage, "names.json"); //JSON with all the resources in it
            storage = prettyJson(jsonStringVM.toString());
            writeFile(storage, "vm.json"); //JSON with only the VMs in it
        } catch (MalformedURLException err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
    }

    /**
     * Parse the alerts and print out, will not print in the future just return
     */
    public ArrayList<Alert> parseJsonAlerts() {
        int i;
        int count = 0;
        String name = "";
        ArrayList<Alert> arr = new ArrayList<>(); //Array to store the alerts
        try {
            path = curDir + File.separator + "jsons" + File.separator + "dataAlerts.json";
            JSONObject jsonObject = new JSONObject(readFile(path));
            JSONArray jsonarray = jsonObject.getJSONArray("alerts");
            createIdAndName();
            for (i = 0; i < jsonarray.length(); ++i) { //Going through "alerts" array
                Alert alert = new Alert();
                JSONObject object = jsonarray.getJSONObject(i);
                name = getResourceName(object.get("resourceId").toString()); //Get the name of the resource concerned by the alert
                if ((object.get("alertLevel").equals("WARNING") //We want alert with warning or critical or immediate status
                        || object.get("alertLevel").equals("CRITICAL")
                        || object.get("alertLevel").equals("IMMEDIATE"))
                        && object.get("status").equals("ACTIVE")) { //We want that alert to be active
                    alert.setName(name);
                    alert.setType(object.get("alertLevel").toString());
                    alert.setStatus(object.get("status").toString());
                    alert.setContent(object.get("alertDefinitionName").toString());
                    arr.add(alert);
                    ++count;
                }
            }
            System.out.println(count + " warning or critical or immediate that are active");
        } catch (Exception err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
        return arr;
    }

    /**
     * Get the resource given of one vm
     *
     * @param id The id of the resource that we want the space disk
     * @return The space disk of the resource
     */
    public long getHardwareResource(String id, String nameResource) {
        String storage = "";
        long res = 0;
        String path = System.getProperty("user.dir") + File.separator + "jsons" + File.separator + "tempData";
        try {
            URL urlSpace = new URL(this.url
                    + "resources/"
                    + id
                    + "/stats?statKey="
                    + nameResource);
            storage = curl("FR-fr", "tempData", urlSpace);
            JSONObject jsonObject = new JSONObject(storage);
            JSONArray jsonArray = jsonObject.getJSONArray("values");
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject values = jsonArray.getJSONObject(i);
                JSONObject valuesArr = values.getJSONObject("stat-list");
                JSONArray anotherArr = valuesArr.getJSONArray("stat");
                for (int j = 0; j < anotherArr.length(); ++j) {
                    JSONObject anotherObj = anotherArr.getJSONObject(j);
                    JSONArray diskSpace = anotherObj.getJSONArray("data");
                    res = diskSpace.getLong(0);
                }
            }
        } catch (Exception err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
        new File(path).delete();
        return res;
    }

    /**
     * Get all the data of a resource given and write it in a json file, for example all the data of disk space of the vms
     *
     * @param nameResource Name of the resource wi want to get, example : disk_Space
     */
    //May need some optimization
    public void getAllHardwareResource(String nameResource) {
        String storage = "";
        String name = "";
        long space = -1;
        JSONObject jsonString = new JSONObject();

        switch (nameResource) {
            case "config|hardware|disk_Space":
                name = "diskspace";
                break;
            case "config|hardware|num_Cpu":
                name = "numberOfCPU";
                break;
            case "mem|guest_usage":
                name = "RAM";
                break;
            case "virtualDisk|commandsAveraged_average":
                name = "IOPS";
                break;
            default:
                name = "undefined";
        }

        path = curDir + File.separator + "jsons" + File.separator + "vm.json";
        JSONObject jsonObject = new JSONObject(readFile(path));

        Iterator<String> iter = jsonObject.keys();
        System.out.print("Getting " + name + ". This action can take a minute...");
        while (iter.hasNext()) {
            String id = iter.next();
            space = getHardwareResource(id, nameResource);
            jsonString.put(id, space);
        }
        storage = prettyJson(jsonString.toString());
        writeFile(storage, name + ".json");
        System.out.println("Done");
    }


    /**
     * Create a file if it's possible
     *
     * @param name Name of the file we want to create in the current directory
     */
    public void createIfPossible(String name) {
        try {
            path = curDir + File.separator + "jsons" + File.separator + name;
            File file = new File(path);
            file.createNewFile();
            FileOutputStream oFile = new FileOutputStream(file, false);
        } catch (IOException err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
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
     * Get the top of vm that consume the most of the resource selected
     * Method a bit too long, may need some work on it
     *
     * @param nameResource Name of the resource we want to get the top (example : cpu, ram)
     * @param top          Number of VM you want in your top (Max 10)
     * @return Return a string with the top of vms
     */
    public ArrayList<VM> getTop(String nameResource, int top) {
        long recomm = -1;
        int i;
        int j;
        int maxIndex = -1;
        long value = -1;
        long maxValue = -1;
        VM maxVm = null;
        ArrayList<VM> arr = arrayOfVms(); //Array with all the VMs
        ArrayList<VM> topArr = new ArrayList<>(); //Array with only the VMs of the top

        if (top > MAX) {
            System.err.println("Top too high, maximum = " + MAX);
            return null;
        } else if (top < 1) {
            System.err.println("Top too low, minimum = 1");
            return null;
        }

        for (i = 1; i <= top; ++i) {
            for (j = 0; j < arr.size(); ++j) {
                VM vm = arr.get(j);
                switch (nameResource) {
                    case "diskspace":
                        value = vm.getDiskSpace();
                        break;
                    case "cpu":
                        value = vm.getCpuUsage();
                        break;
                    case "ram":
                        value = vm.getRamUsage();
                        break;
                    case "iops":
                        value = vm.getIOPS();
                        break;
                    default:
                        System.err.println("Error, undefined resource");
                        return null;
                }
                if (value > maxValue) { //If the value > to the actual highest value
                    maxValue = value;
                    maxIndex = j; //We store the index to delete the value of our global arr later
                    maxVm = vm;
                    switch (nameResource) {
                        case "diskspace":
                            recomm = getRecommendation(vm.getId(), "diskspace");
                            vm.setDiskRecomm(recomm);
                            break;
                        case "cpu":
                            recomm = getRecommendation(vm.getId(), "cpu");
                            vm.setCpuRecomm(recomm);
                            break;
                        case "ram":
                            recomm = getRecommendation(vm.getId(), "mem");
                            vm.setRamRecomm(recomm);
                    }
                }
            }
            arr.remove(maxIndex); //We remove the VM of the global array
            topArr.add(maxVm); //We add the VM to the "top" array
            maxValue = -1; //Reset of the max value
        }
        return topArr;
    }

    /**
     * Get the recommendation for a resource (cpu,memory,spacedisk)
     *
     * @param vmId     Id of the vm we want to look for recommendation
     * @param resource Resource concerned
     * @return Return the recommendation of the vrops
     */
    public long getRecommendation(String vmId, String resource) {
        int i;
        int j;
        long res = -1;
        String path = curDir + File.separator + "jsons" + File.separator + "tmp.json";
        try {
            URL urlConn = new URL(this.url
                    + "resources/"
                    + vmId
                    + "/stats?statKey="
                    + resource
                    + "|size.recommendation");
            curl("fr-FR", "tmp", urlConn);
            JSONObject jsonObject = new JSONObject(readFile(path));
            JSONArray values = jsonObject.getJSONArray("values");
            for (i = 0; i < values.length(); ++i) {
                JSONObject valuesObject = values.getJSONObject(i);
                JSONObject statList = valuesObject.getJSONObject("stat-list");
                JSONArray stats = statList.getJSONArray("stat");
                for (j = 0; j < stats.length(); ++j) {
                    JSONObject statsObject = stats.getJSONObject(j);
                    JSONArray data = statsObject.getJSONArray("data");
                    res = data.getLong(0);
                }
            }
            new File(path).delete();
            return res;
        } catch (MalformedURLException err) {
            System.err.println("Error with the link : " + err.getCause());
        }
        return -1;
    }

    /**
     * Create an array of all the vm in names.json and set disk space, cpu and ram if possible
     *
     * @return An array of VM objects
     */
    public ArrayList<VM> arrayOfVms() {
        String path = curDir + File.separator + "jsons" + File.separator;
        String pathId = path + "vm.json";
        String pathDisk = path + "diskspace.json";
        String pathCPU = path + "numberOfCPU.json";
        String pathRAM = path + "RAM.json";
        String pathIOPS = path + "IOPS.json";

        JSONObject idObject = new JSONObject(readFile(pathId));
        JSONObject diskObject = new JSONObject(readFile(pathDisk));
        JSONObject cpuObject = new JSONObject(readFile(pathCPU));
        JSONObject ramObject = new JSONObject(readFile(pathRAM));
        JSONObject iopsObject = new JSONObject(readFile(pathIOPS));

        File diskFile = new File(pathDisk);
        File cpuFile = new File(pathCPU);
        File ramFile = new File(pathRAM);
        File iopsFile = new File(pathIOPS);

        ArrayList<VM> arr = new ArrayList<>();
        try {
            Iterator<String> iter = idObject.keys();
            while (iter.hasNext()) {
                String id = iter.next();
                VM vm = new VM(id);

                if (diskFile.exists() && !diskFile.isDirectory()) {
                    vm.setDiskSpace(diskObject.getLong(id));
                }
                if (cpuFile.exists() && !cpuFile.isDirectory()) {
                    vm.setCpuUsage(cpuObject.getLong(id));
                }
                if (ramFile.exists() && !ramFile.isDirectory()) {
                    vm.setRamUsage(ramObject.getLong(id));
                }
                if (iopsFile.exists() && !iopsFile.isDirectory()) {
                    vm.setIOPS(iopsObject.getLong(id));
                }
                arr.add(vm);
            }
        } catch (Exception err) {
            System.err.println(err.getCause() + " : " + err.getMessage());
        }
        return arr;
    }

    /**
     * toString() method
     *
     * @return object in a String
     */
    public String toString() {
        return "Url used : " + url;
    }
}


