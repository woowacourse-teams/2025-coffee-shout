package coffeeshout.global.event;

public record TraceInfo(String traceId, String spanId) {

    public boolean isAvailableTrace() {
        return !traceId.isBlank() && !spanId.isBlank();
    }
}
