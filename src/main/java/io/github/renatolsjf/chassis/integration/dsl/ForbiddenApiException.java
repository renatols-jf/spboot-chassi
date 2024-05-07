package io.github.renatolsjf.chassis.integration.dsl;

public class ForbiddenApiException extends ClientErrorApiException {

    ForbiddenApiException(ApiResponse apiResponse) {
        super(apiResponse);
    }

    ForbiddenApiException(String message, ApiResponse apiResponse) {
        super(message, apiResponse);
    }

    ForbiddenApiException(Throwable cause, ApiResponse apiResponse) {
        super(cause, apiResponse);
    }

    ForbiddenApiException(String message, Throwable cause, ApiResponse apiResponse) {
        super(message, cause, apiResponse);
    }

}
