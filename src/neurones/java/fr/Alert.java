package neurones.java.fr;

public class Alert {
    private String name;
    private String type;
    private String content;
    private String status;

    /**
     * Getter name
     * @return Name of the alert
     */
    public String getName() {
        return name;
    }

    /**
     * Setter Name
     * @param name Name that will be set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter type
     * @return Type of the alert
     */
    public String getType() {
        return type;
    }

    /**
     * Setter type
     * @param type Type that will be set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter Content
     * @return content of the alert
     */
    public String getContent() {
        return content;
    }

    /**
     * Setter content
     * @param content Content that will be set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Getter status
     * @return Status of the alert
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter status
     * @param status Status that will be set
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
