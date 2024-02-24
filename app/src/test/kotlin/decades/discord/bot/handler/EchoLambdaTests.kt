package decades.discord.bot.handler

import com.amazonaws.services.lambda.runtime.Context
import decades.discord.bot.model.EchoInput
import decades.discord.bot.model.EchoOutput
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class EchoLambdaTests {
    companion object {
        private const val INPUT = "Hello, World!"
    }

    @Mock
    private lateinit var mockContext: Context

    private lateinit var echo: EchoLambda

    @BeforeEach
    fun setup() {
        echo = EchoLambda()
    }

    @Test
    fun `WHEN EchoInput valid THEN EchoOutput valid`() {
        Assertions.assertEquals(
            EchoOutput(INPUT),
            echo.handle(
                input =
                    EchoInput(
                        input = INPUT,
                    ),
                context = mockContext,
            ),
        )
    }
}
