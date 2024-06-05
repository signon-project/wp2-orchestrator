package signon.orchestrator.errors;

import com.fasterxml.jackson.annotation.JsonFormat;

import signon.orchestrator.errors.exceptions.ParametrizedException;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.UUID;

public class ErrorResponse {

    // RFC 7807 specs short recap (full specs: https://tools.ietf.org/html/rfc7807)
    // "type": "urn:error-type:not-entitled-for-payment-method" // stable, do not change
    // "title": "Human readable short description"
    // "status": "403", // repeats the response status code
    // "detail": "Human readable full description about what went wrong"
    // "instance": "urn:uuid:d294b32b-9dda-4292-b51f-35f65b4bf64d"
    //  + extensions, i.e. custom, machine readable fields

    // custom timestamp serialization format
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")


    // RFC 7807 standard fields
    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;

    // RFC 7807 extensions
    private String stackTrace;
    private Date timestamp;
    private Object parameters;

    public Object getParameters() {
        return parameters;
    }

    public void setParameters(Object parameters) {
        this.parameters = parameters;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public ErrorResponse (){}

    public ErrorResponse(String type, String title, HttpStatus status, Exception e, boolean includeStackTrace) {
        this.type = type;
        this.title = title;
        this.status = status.value();
        this.detail = e.getMessage();
        this.instance = createUrnInstanceUUID();

        if (includeStackTrace) this.stackTrace = stackTraceToString(e);
        this.timestamp = new Date();
    }

    public ErrorResponse(String type, String title, HttpStatus status, ParametrizedException e, boolean includeStackTrace) {
        this.type = type;
        this.title = title;
        this.status = status.value();
        this.detail = e.getMessage();
        this.instance = createUrnInstanceUUID();

        if (includeStackTrace) this.stackTrace = stackTraceToString(e);
        this.timestamp = new Date();
        this.parameters = e.getParameters();
    }

    public ErrorResponse(String type, String title, HttpStatus status, RestClientResponseException e, boolean includeStackTrace) {
        this.type = type;
        this.title = title;
        this.status = status.value();
        this.detail = e.getMessage() + " Response Body: " + e.getResponseBodyAsString();
        this.instance = createUrnInstanceUUID();

        if (includeStackTrace) this.stackTrace = stackTraceToString(e);
        this.timestamp = new Date();
    }

    private String createUrnInstanceUUID () {
        return "urn:uuid:" + UUID.randomUUID();
    }

    private String stackTraceToString (Exception e){
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    public String loggerMessage() {
        return getTitle() + " (" + getInstance() + ")";
    }

}
