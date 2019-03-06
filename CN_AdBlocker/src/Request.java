public class Request {
    private String method;
    private String URL;
    private String body;
    private String host;

    private int portA;
    public Request(String methodA, String URLA, int portA,String bodyA, String hostA){
        this.setBody(bodyA);
        this.setURL(URLA);
        this.setMethod(methodA);
        this.setBody(bodyA);
        this.setPortA(portA);
        this.setHost(hostA);

    }
    public String getHost() { return host;  }

    public void setHost(String host) { this.host = host;}

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String url) {
        this.URL = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getPortA() {
        return portA;
    }

    public void setPortA(int portA) {
        this.portA = portA;
    }


}
