import com.strumenta.antlrkotlin.runtime.assert
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.pl.TypeChecker
import org.pl.TypeCheckingError
import java.io.File
import java.util.*
import java.util.stream.Stream
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)

class TestFailedCases {
    private val testDataPath: String = "src/test/resources/stella-tests/bad";

    private fun checkFile(input: String, errorTag: String) {
        println(input)
        try {
            TypeChecker.checkUnsafe(input.byteInputStream())
            assert(false, "Have to throw Type Checking Error")
        } catch (e: TypeCheckingError) {
            assertEquals(errorTag, e.errorTag)
        }
    }

    @TestFactory
    fun testAll(): Collection<DynamicContainer> {
        val folders = File(testDataPath).listFiles()
        return folders.map { folder ->
            DynamicContainer.dynamicContainer(folder.name, folder.listFiles().map { file ->
                DynamicTest.dynamicTest(file.name) {
                    checkFile(file.readText(), folder.name)
                }
            })
        }
    }


    private fun listAllFiles(): Stream<Pair<File, String>>? {
        val folders = Arrays.stream(Objects.requireNonNull<Array<File>>(File(testDataPath).listFiles()))
        return folders.flatMap { a ->
            Arrays.stream(a.listFiles()).map { b ->
                b to a.name
            }
        }
    }

}