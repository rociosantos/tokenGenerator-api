package exceptions;

public class UnauthorizedException extends Exception {
    private final String userName;
    public UnauthorizedException(String userName) {
        this.userName = userName;
    }

    public String getResourceId() {
        return userName;
    }
}
