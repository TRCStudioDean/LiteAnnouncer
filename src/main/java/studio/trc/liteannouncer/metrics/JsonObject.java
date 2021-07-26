package studio.trc.liteannouncer.metrics;

public class JsonObject {

    private final String value;

    JsonObject(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}