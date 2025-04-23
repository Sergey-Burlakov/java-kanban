public class ManagerLoadException extends RuntimeException {
    public ManagerLoadException(String message, Throwable e) {
        super(message, e);
    }

    public ManagerLoadException(String message) {
        super(message);
    }
}
