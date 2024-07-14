package com.example.exception;

import com.example.util.JsonUtils;
import com.example.util.MapUtil;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {


//    @ExceptionHandler({RuntimeException.class})
//    public ResponseEntity<Object> BadRequestException(final RuntimeException ex) {
//        return ResponseEntity.badRequest().body(ex.getMessage());
//    }

//    @ExceptionHandler({ xxxx.class })
//    public ResponseEntity<Object> EveryException(final Exception ex) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    @ExceptionHandler({
            // Parameter Validation Error
            // MissingServletRequestParameterException.class,
            // Parameter Validation Error
            BindException.class,
            // Parameter Validation Error
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class,
            ParameterException.class,
    })
    public ResponseEntity<?> CommonErrorException(Exception e) throws Exception {
        e.printStackTrace();

        // 공통에러상수 객체 셋팅
        String errorName = e.getClass().getSimpleName();
        String errorMessage = null;
        /* Exception 별 커스텀 처리 */
        switch (errorName) {
            case "BindException": // 요청 파라메터 유효성 검증 실패
                errorMessage = getBindResultFieldErrorMessage(((BindException) e).getBindingResult());
                break;
            case "MethodArgumentNotValidException": // 요청 파라메터 유효성 검증 실패
                errorMessage = getBindResultFieldErrorMessage(((MethodArgumentNotValidException) e).getBindingResult());
                break;
            case "HttpMessageNotReadableException":
                HttpMessageNotReadableException hm = (HttpMessageNotReadableException) e;
                Throwable cause = hm.getCause();
                JsonMappingException mapping = null;
                if(cause instanceof JsonMappingException) {
                    mapping = (JsonMappingException) cause;
                    log.error("message:: getMessage {}",mapping.getMessage());
                    // message:: getMessage Cannot deserialize value of type `java.lang.String` from Object value (token `JsonToken.START_OBJECT`)
                    // at [Source: (org.springframework.util.StreamUtils$NonClosingInputStream); line: 16, column: 13] (through reference chain: com.tidesquare.omakase.api.dto.recent.RecentSearchDto["recent_search"]->com.tidesquare.omakase.api.domain.recent.RecentSearch["from"])
                    log.error("message:: getLocalizedMessage {}",mapping.getLocalizedMessage());
                    // message:: getLocalizedMessage Cannot deserialize value of type `java.lang.String` from Object value (token `JsonToken.START_OBJECT`)
                    // at [Source: (org.springframework.util.StreamUtils$NonClosingInputStream); line: 16, column: 13] (through reference chain: com.tidesquare.omakase.api.dto.recent.RecentSearchDto["recent_search"]->com.tidesquare.omakase.api.domain.recent.RecentSearch["from"])
                    log.error("message:: getCause {}",mapping.getCause());
                    // message:: getCause null
                    log.error("message:: getPath {}",mapping.getPath());
                    // message:: getPath [com.tidesquare.omakase.api.dto.recent.RecentSearchDto["recent_search"], com.tidesquare.omakase.api.domain.recent.RecentSearch["from"]]
                    log.error("message:: getClass {}",mapping.getClass());
                    // message:: getClass class com.fasterxml.jackson.databind.exc.MismatchedInputException
                    log.error("message:: getPathReference {}",mapping.getPathReference());
                    // message:: getPathReference com.tidesquare.omakase.api.dto.recent.RecentSearchDto["recent_search"]->com.tidesquare.omakase.api.domain.recent.RecentSearch["from"]
                    log.error("message:: getOriginalMessage {}",mapping.getOriginalMessage());
                    // message:: getOriginalMessage Cannot deserialize value of type `java.lang.String` from Object value (token `JsonToken.START_OBJECT`)
                    log.error("message:: getProcessor {}",mapping.getProcessor());
                    // message:: getProcessor com.fasterxml.jackson.core.json.UTF8StreamJsonParser@241f7789
                    log.error("message:: getLocation {}",mapping.getLocation());
                    // message:: getLocation [Source: (org.springframework.util.StreamUtils$NonClosingInputStream); line: 16, column: 13]
                    errorMessage = "JSON Parsing Error: "+mapping.getPath();
                }
                errorMessage = errorMessage != null ? errorMessage : hm.getMessage();
                break;
            case "ParameterException":
                return ResponseEntity.badRequest().body(exMap(HttpStatus.BAD_REQUEST, e));
            default:
                errorMessage = JsonUtils.toMapper(e.getMessage());
                break;
        }
        return ResponseEntity.badRequest().body(errorMessage);
    }


    @ExceptionHandler({ ResponseStatusException.class })
    public ResponseEntity<String> ResponseStatusException(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
    }

    @ExceptionHandler({ ChangeSetPersister.NotFoundException.class })
    public ResponseEntity<Object> NotFoundExceptionResponse(NotFoundException e) {
        return new ResponseEntity<>(e.getErrmsg(), e.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> NotFoundExceptionResponse(Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().body(e.getMessage());
    }

    /**
     * BindException Field 메세지 가공
     * @param bindingResult
     * @return
     */
    protected String getBindResultFieldErrorMessage(BindingResult bindingResult) {

        Map<String, Object> resultMap = new LinkedHashMap<>();

        resultMap.put("title","요청 파라메터 유효성 검증 실패");
        List<FieldError> fieldErrorList = bindingResult.getFieldErrors();
        List<Map<String, Object>> paramList = new ArrayList<>();
        for (FieldError fieldError: fieldErrorList){

            Map<String, Object> resultParam = new LinkedHashMap<>();
            resultParam.put(fieldError.getField(),fieldError.getRejectedValue());
            resultParam.put("message",fieldError.getDefaultMessage());
            paramList.add(resultParam);
            /*
            log.debug("## ERROR getField = {}", fieldError.getField());
            log.debug("## ERROR getRejectedValue = {}", fieldError.getRejectedValue());
            log.debug("## ERROR getArguments = {}", fieldError.getArguments());
            log.debug("## ERROR getCode = {}", fieldError.getCode());
            log.debug("## ERROR getCodes = {}", fieldError.getCodes());
            log.debug("## ERROR getObjectName = {}", fieldError.getObjectName());
            log.debug("## ERROR getDefaultMessage = {}", fieldError.getDefaultMessage());
            */
        }
        resultMap.put("fields",paramList);

        return JsonUtils.toMapperPretty(resultMap);
    }

    private Map<String, Object> exMap(HttpStatus status, Exception e, String errorMessage) {
        return exMap(status, e.getClass().getSimpleName(), errorMessage);
    }
    private Map<String, Object> exMap(HttpStatus status, Exception e) {
        if (e instanceof ParameterException) {
            ParameterException pe = (ParameterException) e;
            if (pe.getErrorMap() != null)
                return exMap(status, e.getClass().getSimpleName(), pe.getErrorMap());
        }

        return exMap(status, e.getClass().getSimpleName(), e.getMessage());
    }

    private Map<String, Object> exMap(HttpStatus status, String error, String message) {
		/* @formatter:off
		{
		    "timestamp": "2021-06-18T01:45:21.166+00:00",
		    "status": 415,
		    "error": "Unsupported Media Type",
		    "message": "Unsupported Media Type",
		    "path": "/tna-api/rest/cart/_add"
		}
		@formatter:on */

        ServletRequestAttributes sras = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String path = sras != null ? sras.getRequest().getRequestURI() : "[unknown path]";

        return MapUtil.mapOf(
                "timestamp", new Date(),
                "status", status.value(),
                "error", error,
                "message", message,
                "path", path
        );
    }

    private Map<String, Object> exMap(HttpStatus status, String error, Map<String, String> errorMap) {
		/* @formatter:off
		{
		    "timestamp": "2021-06-18T01:45:21.166+00:00",
		    "status": 415,
		    "error": "Unsupported Media Type",
		    "message": "Unsupported Media Type",
		    "path": "/tna-api/rest/cart/_add"
		}
		@formatter:on */

        ServletRequestAttributes sras = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();

        String path = sras != null ? sras.getRequest().getRequestURI() : "[unknown path]";

        List<String> messages = errorMap.keySet().stream()
                .map(key -> "Field: %s, ErrorMessage: %s".formatted(key, errorMap.get(key)))
                .toList();

        return MapUtil.mapOf(
                "timestamp", new Date(),
                "status", status.value(),
                "error", error,
                "message", messages,
                "parameter", errorMap,
                "path", path
        );
    }
}

