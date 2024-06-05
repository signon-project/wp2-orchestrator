package signon.orchestrator.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import signon.orchestrator.errors.exceptions.MissingAppInstanceIDException;
import signon.orchestrator.errors.exceptions.PipelineException;
import signon.orchestrator.errors.exceptions.FileFormatNotSupportedException;

@ControllerAdvice
class CustomControllerAdvice {

    private final static Logger logger = LoggerFactory.getLogger(CustomControllerAdvice.class);

    @Value("${server.error.include-stack-trace}")
    private Boolean includeStackTrace;

    @ExceptionHandler(MissingAppInstanceIDException.class)
    public ResponseEntity<ErrorResponse> handleMissingAppInstanceIDExceptions(Exception e) {

        MissingAppInstanceIDException customException = (MissingAppInstanceIDException) e;

        String type = createUrnErrorType(ErrorType.MISSING_APP_INSTANCE_ID);
        String title = "The AppInstanceID for the message is missing.";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // ErrorResponse errorResponse = new ErrorResponse(type, title, status, customException, INCLUDE_STACK_TRACE);
        ErrorResponse errorResponse = new ErrorResponse(type, title, status, customException, includeStackTrace);

        logger.error(errorResponse.loggerMessage(), customException);

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(PipelineException.class)
    public ResponseEntity<ErrorResponse> handlePipelineExceptions(Exception e) {

        PipelineException pipelineException = (PipelineException) e;

        ErrorResponse errorResponse;
        try{
            ObjectMapper customObjectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
            errorResponse = customObjectMapper.convertValue(pipelineException.getParameters(), ErrorResponse.class);
            logger.info("includeStackTrace:\t" + includeStackTrace);
        } catch (IllegalArgumentException exception){
            String type = createUrnErrorType(ErrorType.INTERNAL_SERVER_ERROR);
            String title = "An internal server error has occurred.";
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorResponse = new ErrorResponse(type, title, status, pipelineException, includeStackTrace);
        }

        HttpStatus status = HttpStatus.valueOf(errorResponse.getStatus());

        logger.error(errorResponse.loggerMessage(), pipelineException);

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(FileFormatNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleFileFormatNotSupportedExceptions(Exception e) {

        FileFormatNotSupportedException fileFormatNotSupportedException = (FileFormatNotSupportedException) e;

        ErrorResponse errorResponse;
        try{
            ObjectMapper customObjectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
            errorResponse = customObjectMapper.convertValue(fileFormatNotSupportedException.getParameters(), ErrorResponse.class);
            logger.info("includeStackTrace:\t" + includeStackTrace);
        } catch (IllegalArgumentException exception){
            String type = createUrnErrorType(ErrorType.INTERNAL_SERVER_ERROR);
            String title = "An internal server error has occurred.";
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorResponse = new ErrorResponse(type, title, status, fileFormatNotSupportedException, includeStackTrace);
        }

        HttpStatus status = HttpStatus.valueOf(errorResponse.getStatus());

        logger.error(errorResponse.loggerMessage(), fileFormatNotSupportedException);

        return new ResponseEntity<>(errorResponse, status);
    }


    // Handle all leftover exceptions
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleRestClientResponseExExceptions(Exception e) {

        RestClientResponseException customException = (RestClientResponseException) e;

        HttpStatus status = HttpStatus.valueOf(customException.getRawStatusCode());

        // Check whether the RestClientResponseException is from MV SLC Engine
        ErrorResponse errorResponse;
        try {
            ObjectMapper customObjectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
            errorResponse = customObjectMapper.readValue(customException.getResponseBodyAsString(), ErrorResponse.class);
        } catch (JsonProcessingException exception) {
            String type = createUrnErrorType(ErrorType.INTERNAL_SERVER_ERROR);
            String title = "An internal server error has occurred.";
            errorResponse = new ErrorResponse(type, title, status, customException, includeStackTrace);
        }

        logger.error(errorResponse.loggerMessage(), customException);

        return new ResponseEntity<>(errorResponse, status);
    }

    // Handle all leftover exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleExceptions(Exception e) {

        String type = createUrnErrorType(ErrorType.INTERNAL_SERVER_ERROR);
        String title = "An internal server error has occurred.";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse errorResponse = new ErrorResponse(type, title, status, e, includeStackTrace);

        logger.error(errorResponse.loggerMessage(), e);

        return new ResponseEntity<>(errorResponse, status);
    }

    private String createUrnErrorType (ErrorType errorType) {
        return "urn:error-type:" + errorType;
    }

}

