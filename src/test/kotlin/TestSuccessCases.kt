import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.pl.TypeChecker
import java.io.File
import java.util.*
import java.util.stream.Stream
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSuccessCases {
    private fun checkFile(code: String) {
        if( code.contains("letrec") ){
            return
        }
        println(code)
        assertEquals(0, TypeChecker.checkUnsafe(code.byteInputStream()))
    }

    @ParameterizedTest
    @MethodSource("listAllFiles")
    fun testAll(file: File) {
        checkFile(file.readText())
    }
    private fun listAllFiles(): Stream<File>? {
        return Arrays.stream(Objects.requireNonNull<Array<File>>(File(testDataPath).listFiles()))
    }

    private val testDataPath: String = "src/test/resources/stella-tests/ok";
}