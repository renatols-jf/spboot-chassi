package io.github.renatolsjf.chassis.integration.dsl;

public class ClientErrorApiException extends RequestErrorApiException {

    ClientErrorApiException(ApiResponse apiResponse) {
        super(apiResponse);
    }

    ClientErrorApiException(String message, ApiResponse apiResponse) {
        super(message, apiResponse);
    }

    ClientErrorApiException(Throwable cause, ApiResponse apiResponse) {
        super(cause, apiResponse);
    }

    ClientErrorApiException(String message, Throwable cause, ApiResponse apiResponse) {
        super(message, cause, apiResponse);
    }

}
