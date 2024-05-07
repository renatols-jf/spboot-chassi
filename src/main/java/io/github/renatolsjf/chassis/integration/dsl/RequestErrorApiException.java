package io.github.renatolsjf.chassis.integration.dsl;

class RequestErrorApiException extends ApiException {

    private ApiResponse apiResponse;

    protected RequestErrorApiException(ApiResponse apiResponse) {
        this.apiResponse = apiResponse;
    }

    protected RequestErrorApiException(String message, ApiResponse apiResponse) {
        super(message);
        this.apiResponse = apiResponse;
    }

    protected RequestErrorApiException(Throwable cause, ApiResponse apiResponse) {
        super(cause);
        this.apiResponse = apiResponse;
    }

    protected RequestErrorApiException(String message, Throwable cause, ApiResponse apiResponse) {
        super(message, cause);
        this.apiResponse = apiResponse;
    }

    public ApiResponse getApiResponse() {
        return this.apiResponse;
    }

    static RequestErrorApiException create(ApiResponse apiResponse) {
        return create(null, null, apiResponse);
    }

    static RequestErrorApiException create(String message, Throwable cause, ApiResponse apiResponse) {
        if (apiResponse.isUnauthorized()) {
            return new UnauthorizedApiException(message, cause, apiResponse);
        } else if (apiResponse.isForbidden()) {
            return new ForbiddenApiException(message, cause, apiResponse);
        } else if (apiResponse.isClientError()) {
            return new ClientErrorApiException(apiResponse);
        } else {
            return new ServerErrorApiException(apiResponse);
        }
    }



}
