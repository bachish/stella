import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.*
import java.util.stream.Stream

/**
 * Tests that are run for all files in the directory
 * @param <T>
</T> */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
interface TestAllInDirectory {
    fun checkFile(code: String)
    val testDataPath: String

    @ParameterizedTest
    @MethodSource("listAllFiles")
    fun testAll(file: File) {
        checkFile(file.readText())
    }

    fun listAllFiles(): Stream<File>? {
        return Arrays.stream(Objects.requireNonNull<Array<File>>(File(testDataPath).listFiles()))
    }
}
