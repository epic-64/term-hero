import org.scalatest.funsuite.AnyFunSuite
import ScalaScape.ScalaScape
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.screen.TerminalScreen
import org.scalatestplus.mockito.MockitoSugar

class ScalaScapeTest extends AnyFunSuite with MockitoSugar {
  test("the game can run 100 ticks without crashing") {
    val mockScreen   = mock[TerminalScreen]
    val mockGraphics = mock[TextGraphics]
    val game         = new ScalaScape(mockScreen, mockGraphics)

    for (_ <- 1 to 100) {
      val state = game.update(game.state)
    }
  }
}
