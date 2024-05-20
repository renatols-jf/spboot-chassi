package io.github.renatolsjf.chassis.integration.dsl;

public class ServerErrorApiException extends RequestErrorApiException {

    ServerErrorApiException(ApiResponse apiResponse) {
        super(apiResponse);
    }

    ServerErrorApiException(String message, ApiResponse apiResponse) {
        super(message, apiResponse);
    }

    ServerErrorApiException(Throwable cause, ApiResponse apiResponse) {
        super(cause, apiResponse);
    }

    ServerErrorApiException(String message, Throwable cause, ApiResponse apiResponse) {
        super(message, cause, apiResponse);
    }

}
