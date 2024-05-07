package io.github.renatolsjf.chassis.integration.dsl;

public class UnauthorizedApiException extends ClientErrorApiException {

    UnauthorizedApiException(ApiResponse apiResponse) {
        super(apiResponse);
    }

    UnauthorizedApiException(String message, ApiResponse apiResponse) {
        super(message, apiResponse);
    }

    UnauthorizedApiException(Throwable cause, ApiResponse apiResponse) {
        super(cause, apiResponse);
    }

    UnauthorizedApiException(String message, Throwable cause, ApiResponse apiResponse) {
        super(message, cause, apiResponse);
    }

}
