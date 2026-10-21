package nl.klrnbk.common.responses.error

class ErrorResponse(
    val status: Int,
    val type: String,
    val message: String,
    val detail: String,
    val instance: String,
) {
    companion object {
        const val UNAUTHORIZED = """
            {
              "status": 401,
              "type": "UNAUTHORIZED",
              "message": "You have to be logged in to do this.",
              "detail": "No x-authorization header is present.",
              "instance": "/v1/example/endpoint"
            }
        """

        const val BAD_REQUEST = """
            {
              "status": 400,
              "type": "BAD_REQUEST",
              "message": "Client sent a bad request.",
              "detail": "Client sent a bad request.",
              "instance": "/v1/example/endpoint"
            }
        """

        const val NOT_FOUND = """
            {
              "status": 404,
              "type": "NOT_FOUND",
              "message": "Resource not found.",
              "detail": "Resource not found.",
              "instance": "/v1/example/endpoint"
            }
        """

        const val INTERNAL_SERVER_ERROR = """
            {
              "status": 500,
              "type": "INTERNAL_SERVER_ERROR",
              "message": "An unexpected error occurred.",
              "detail": "An unexpected error occurred.",
              "instance": "/v1/example/endpoint"
            }
        """
    }
}
