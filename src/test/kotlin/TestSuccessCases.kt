import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.pl.TypeChecker
import java.io.File
import kotlin.test.assertEquals

class TestSuccessCases {
    private val unsupportedTests = listOf<String>()

    private fun checkFile(code: String) {
        println(code)
        assertEquals(0, TypeChecker.checkUnsafe(code.byteInputStream()))
    }


    @TestFactory
    fun testAll(): Collection<DynamicTest> {
        val files = File(testDataPath).listFiles()
        if (files != null) {
            return files.map { file ->
                DynamicTest.dynamicTest(file.name) {
                    checkFile(file.readText())
                }
            }
        } else {
            throw Exception("test folder $testDataPath not found")
        }
    }

    private val testDataPath: String = "src/test/resources/stella-tests/ok"
}