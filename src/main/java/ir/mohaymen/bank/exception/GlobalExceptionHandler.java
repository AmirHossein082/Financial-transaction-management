package ir.mohaymen.bank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle 400 - Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBadRequest(Exception ex) {
        return buildErrorPage(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), "error/400");
    }

    // Handle 403 - Forbidden
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleForbidden(Exception ex) {
        return buildErrorPage(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), "error/403");
    }

    // Handle 404 - Not Found
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(Exception ex) {
        return buildErrorPage(HttpStatus.NOT_FOUND, "Resource Not Found", "The requested resource was not found.", "error/404");
    }

    // Handle 500 - Internal Server Error
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGlobalException(Exception ex) {
        return buildErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred.", "error/500");
    }

    // Generic method to return custom error pages
    private ModelAndView buildErrorPage(HttpStatus status, String error, String message, String errorPage) {
        ModelAndView modelAndView = new ModelAndView(errorPage);
        modelAndView.addObject("status", status.value());
        modelAndView.addObject("error", error);
        modelAndView.addObject("message", message);
        modelAndView.addObject("timestamp", LocalDateTime.now());
        return modelAndView;
    }
}
