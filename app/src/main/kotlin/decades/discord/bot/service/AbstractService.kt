package decades.discord.bot.service

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import decades.discord.bot.model.ErrorResponse
import software.amazon.awssdk.utils.Either
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

abstract class AbstractService<ErrorType : ErrorResponse>(
    protected open val httpClient: HttpClient,
    protected val endpoint: String,
    protected val errorType: Class<ErrorType>,
    protected val defaultHeaders: Map<String, String>? = null,
) {
    companion object {
        protected val OBJECT_MAPPER =
            ObjectMapper()
                .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerKotlinModule()
    }

    protected inline fun <reified Response : Any> get(
        resource: String,
        id: String,
        additionalHeaders: Map<String, String>? = null,
    ): Either<ErrorType, Response> =
        request<Response>(
            method = "GET",
            resource = resource,
            id = id,
            additionalHeaders = additionalHeaders,
        )

    protected inline fun <reified Response : Any> post(
        resource: String,
        body: Any?,
        additionalHeaders: Map<String, String>? = null,
    ): Either<ErrorType, Response> =
        request<Response>(
            method = "POST",
            resource = resource,
            body = body,
            additionalHeaders = additionalHeaders,
        )

    protected inline fun <reified Response : Any> put(
        resource: String,
        id: String,
        body: Any?,
        additionalHeaders: Map<String, String>? = null,
    ): Either<ErrorType, Response> =
        request<Response>(
            method = "PUT",
            resource = resource,
            id = id,
            body = body,
            additionalHeaders = additionalHeaders,
        )

    protected inline fun <reified Response : Any> delete(
        resource: String,
        id: String,
        additionalHeaders: Map<String, String>? = null,
    ): Either<ErrorType, Response> =
        request<Response>(
            method = "DELETE",
            resource = resource,
            id = id,
            additionalHeaders = additionalHeaders,
        )

    protected inline fun <reified Response : Any?> request(
        method: String,
        resource: String,
        id: String? = null,
        body: Any? = null,
        additionalHeaders: Map<String, String>? = null,
    ): Either<ErrorType, Response> {
        val response =
            httpClient.send(
                HttpRequest.newBuilder(URI("$endpoint/$resource/".plus(id)))
                    .apply {
                        when (method) {
                            "GET" -> GET()
                            "DELETE" -> DELETE()
                            else ->
                                method(
                                    method,
                                    HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(body)),
                                )
                        }
                        defaultHeaders?.forEach { (key, value) ->
                            header(key, value)
                        }
                        additionalHeaders?.forEach { (key, value) ->
                            header(key, value)
                        }
                    }
                    .build(),
                HttpResponse.BodyHandlers.ofInputStream(),
            )
        when (response.statusCode() / 100) {
            2 -> return Either.right(OBJECT_MAPPER.readValue(response.body(), Response::class.java))
        }
        return Either.left(OBJECT_MAPPER.readValue(response.body(), errorType))
    }
}
