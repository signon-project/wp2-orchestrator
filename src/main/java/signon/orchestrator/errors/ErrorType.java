package signon.orchestrator.errors;

public enum ErrorType {
    INTERNAL_SERVER_ERROR("internal-server-error"),
    MISSING_APP_INSTANCE_ID("missing-app-instance-id"),
    PIPELINE_ERROR("pipeline-error");

    private final String value;

    ErrorType(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
