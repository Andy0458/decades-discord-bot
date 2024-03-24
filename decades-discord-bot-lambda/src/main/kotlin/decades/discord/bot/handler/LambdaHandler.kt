package decades.discord.bot.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.InputStream
import java.io.OutputStream

interface LambdaHandler<I : Any, O : Any?> : RequestStreamHandler {
    companion object {
        internal val OBJECT_MAPPER =
            ObjectMapper()
                .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerKotlinModule()
        private val OBJECT_WRITER = OBJECT_MAPPER.writer()
    }

    val objectMapper: ObjectMapper
        get() = OBJECT_MAPPER
    val inputType: Class<I>

    fun <T : Any> readJson(
        clazz: Class<T>,
        stream: InputStream,
    ): T = OBJECT_MAPPER.readValue(stream, clazz)

    fun <T : Any> InputStream.readJson(clazz: Class<T>): T = readJson(clazz, this)

    fun Any?.writeJsonNullable(outputStream: OutputStream) {
        if (this != null) OBJECT_WRITER.writeValue(outputStream, this)
    }

    fun handle(
        input: I,
        context: Context?,
    ): O?

    override fun handleRequest(
        input: InputStream,
        output: OutputStream,
        context: Context,
    ) {
        handle(input.readJson(inputType), context).writeJsonNullable(output)
    }
}
