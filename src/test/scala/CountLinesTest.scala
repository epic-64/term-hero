import org.scalatest.funsuite.AnyFunSuite
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets
import scala.jdk.StreamConverters._

def countLinesInSrcFolder(srcFolder: String): Int = {
  def countLinesInFile(filePath: java.nio.file.Path): Int =
    Files.readAllLines(filePath, StandardCharsets.UTF_8).size()

  Files.walk(Paths.get(srcFolder)).toScala(Seq)
    .filter(path => Files.isRegularFile(path) && path.toString.endsWith(".scala"))
    .map(countLinesInFile)
    .sum
}

class CountLinesTest extends AnyFunSuite {
  test("create a line count report") {
    val srcFolder = "src/main/scala"
    val testFolder = "src/test/scala"

    val srcLines = countLinesInSrcFolder(srcFolder)
    val testLines = countLinesInSrcFolder(testFolder)
    val totalLines = srcLines + testLines
    
    println(s"Lines of code in project: $totalLines")
    println(s"Lines of code in $srcFolder: $srcLines")
    println(s"Lines of code in $testFolder: $testLines")
  }
}
