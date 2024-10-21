import ScalaScape.ScalaScape
import _root_.ScalaScape.components.*
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.screen.TerminalScreen
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.mockito.MockitoSugar

class ScalaScapeTest extends AnyFunSuite with MockitoSugar {
  test("the game can run a number of ticks without crashing") {
    val mockScreen   = mock[TerminalScreen]
    val mockGraphics = mock[TextGraphics]
    val game         = new ScalaScape(mockScreen, mockGraphics)

    game.state.swapScene(game.state.scenes.oak)

    for (_ <- 1 to 100) {
      game.update(game.state)
      game.draw(mockGraphics) // rendering is tested here, drawing is not
    }
  }
  
  test("the skill values are persisted throughout scene swaps") {
    val mockScreen   = mock[TerminalScreen]
    val mockGraphics = mock[TextGraphics]
    val game         = new ScalaScape(mockScreen, mockGraphics)

    game.state.skills.woodcutting.mastery[OakMastery].xp = 100
    game.state.skills.woodcutting.mastery[OakMastery].level = 5

    game.state.swapScene(game.state.scenes.teak)
    game.update(game.state)

    game.state.swapScene(game.state.scenes.oak)

    assert(game.state.skills.woodcutting.mastery[OakMastery].xp == 100)
    assert(game.state.skills.woodcutting.mastery[OakMastery].level == 5)
  }
}
