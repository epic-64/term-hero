import org.scalatest.wordspec.AnyWordSpec

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.jdk.StreamConverters.*

class CountLinesTest extends AnyWordSpec {
  def countLinesInSrcFolder(srcFolder: String): Int = {
    def countLinesInFile(filePath: java.nio.file.Path): Int =
      Files.readAllLines(filePath, StandardCharsets.UTF_8).size()

    Files.walk(Paths.get(srcFolder)).toScala(Seq)
      .filter(path => Files.isRegularFile(path) && path.toString.endsWith(".scala"))
      .map(countLinesInFile)
      .sum
  }

  "The project" when {
    "running tests" should {
      "report its lines of code" in {
        val srcFolder = "src/main/scala"
        val testFolder = "src/test/scala"

        val srcLines = countLinesInSrcFolder(srcFolder)
        val testLines = countLinesInSrcFolder(testFolder)
        val totalLines = srcLines + testLines

        info(s"Lines of code in project: $totalLines")
        info(s"Lines of code in $srcFolder: $srcLines")
        info(s"Lines of code in $testFolder: $testLines")

        assert(totalLines >= 0, "The total lines of code should be a non-negative number")
      }
    }
  }
}
