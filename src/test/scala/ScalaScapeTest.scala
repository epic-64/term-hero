import org.scalatest.funsuite.AnyFunSuite
import ScalaScape.ScalaScape
import ScalaScape.components.WoodCuttingOakScene
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.screen.TerminalScreen
import org.scalatestplus.mockito.MockitoSugar

class ScalaScapeTest extends AnyFunSuite with MockitoSugar {
  test("the game can run a number of ticks without crashing") {
    val mockScreen   = mock[TerminalScreen]
    val mockGraphics = mock[TextGraphics]
    val game         = new ScalaScape(mockScreen, mockGraphics)
    
    game.state.swapScene(WoodCuttingOakScene())
    
    for (_ <- 1 to 100) {
      game.update(game.state)
      game.draw(mockGraphics) // rendering is tested here, drawing is not
    }
  }
}
