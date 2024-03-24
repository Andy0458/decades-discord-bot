package decades.discord.bot.service

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import decades.discord.bot.model.ErrorResponse
import org.apache.logging.log4j.LogManager
import software.amazon.awssdk.utils.Either
import java.io.InputStream
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
        protected val LOG = LogManager.getLogger(AbstractService::class.java)
    }

    protected inline fun <reified Response : Any> get(
        resource: String,
        id: String? = null,
        queryParams: Map<String, String>? = null,
        additionalHeaders: Map<String, String>? = null,
    ): Either<ErrorType, Response>? =
        request<Response>(
            method = "GET",
            resource = resource,
            id = id,
            queryParams = queryParams,
            additionalHeaders = additionalHeaders,
        )

    protected inline fun <reified Response : Any> post(
        resource: String,
        body: Any?,
        queryParams: Map<String, String>? = null,
        additionalHeaders: Map<String, String>? = null,
    ): Either<ErrorType, Response>? =
        request<Response>(
            method = "POST",
            resource = resource,
            body = body,
            queryParams = queryParams,
            additionalHeaders = additionalHeaders,
        )

    protected inline fun <reified Response : Any> put(
        resource: String,
        id: String,
        body: Any? = null,
        queryParams: Map<String, String>? = null,
        additionalHeaders: Map<String, String>? = null,
    ): Either<ErrorType, Response>? =
        request<Response>(
            method = "PUT",
            resource = resource,
            id = id,
            body = body,
            queryParams = queryParams,
            additionalHeaders = additionalHeaders,
        )

    protected inline fun <reified Response : Any> delete(
        resource: String,
        id: String,
        queryParams: Map<String, String>? = null,
        additionalHeaders: Map<String, String>? = null,
    ): Either<ErrorType, Response>? =
        request<Response>(
            method = "DELETE",
            resource = resource,
            id = id,
            queryParams = queryParams,
            additionalHeaders = additionalHeaders,
        )

    protected inline fun <reified Response : Any?> request(
        method: String,
        resource: String,
        id: String? = null,
        body: Any? = null,
        queryParams: Map<String, String>? = null,
        additionalHeaders: Map<String, String>? = null,
    ): Either<ErrorType, Response>? {
        var response: HttpResponse<InputStream>
        val uri =
            URI(
                "$endpoint/$resource".plus(
                    id?.let {
                        "/$id"
                    }.orEmpty(),
                ).plus(
                    queryParams?.map { entry -> "${entry.key}=${entry.value}" }?.let {
                        "?${it.joinToString(separator = "&")}"
                    }.orEmpty(),
                ),
            )
        LOG.debug("Performing {} operation on URI {}", method, uri)
        do {
            response =
                httpClient.send(
                    HttpRequest.newBuilder(uri)
                        .apply {
                            when (method) {
                                "GET" -> GET()
                                "DELETE" -> DELETE()
                                else -> {
                                    method(
                                        method,
                                        HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(body)),
                                    )
                                    header("Content-Type", "application/json")
                                }
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
            if (response.statusCode() == 429) {
                val secondsToSleep = response.headers().firstValueAsLong("Retry-After").orElse(1)
                LOG.debug("Rate Limited! Sleeping {} seconds -- response headers: {}", secondsToSleep, response.headers())
                Thread.sleep(1000 * secondsToSleep)
            }
        } while (response.statusCode() == 429)
        LOG.debug("Response StatusCode: {}", response.statusCode())
        when (response.statusCode()) {
            200, 201 -> {
                return Either.right(OBJECT_MAPPER.readValue(response.body(), object : TypeReference<Response>() {}))
            }
            204 -> {
                return null
            }
        }
        return Either.left(OBJECT_MAPPER.readValue(response.body(), errorType))
    }
}
