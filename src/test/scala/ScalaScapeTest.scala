import ScalaScape.ScalaScape
import _root_.ScalaScape.components.*
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.screen.TerminalScreen
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.mockito.MockitoSugar

class ScalaScapeTest extends AnyFunSuite with MockitoSugar {
  test("the game can run a number of ticks without crashing") {
    val game = new ScalaScape(mock[TerminalScreen], mock[TextGraphics])

    game.state.swapScene(game.state.scenes.oak)

    for (_ <- 1 to 100) {
      game.update(game.state)
      game.render(game.state)
    }
  }
  
  test("the skill values are persisted throughout scene swaps") {
    val game = new ScalaScape(mock[TerminalScreen], mock[TextGraphics])

    game.state.skills.woodcutting.mastery[OakMastery].xp = 100
    game.state.skills.woodcutting.mastery[OakMastery].level = 5

    game.state.swapScene(game.state.scenes.teak)
    game.update(game.state)

    game.state.swapScene(game.state.scenes.oak)

    assert(game.state.skills.woodcutting.mastery[OakMastery].xp == 100)
    assert(game.state.skills.woodcutting.mastery[OakMastery].level == 5)
  }

  test("the menu shows the correct mastery levels even after updating") {
    val game = new ScalaScape(mock[TerminalScreen], mock[TextGraphics])

    game.state.swapScene(game.state.scenes.woodcutting)

    game.state.skills.woodcutting.mastery[OakMastery].level = 0
    assert(game.render(game.state).hasStringLike("Oak Mastery (0 / 99)"))

    game.state.skills.woodcutting.mastery[OakMastery].level = 99
    assert(game.render(game.state).hasStringLike("Oak Mastery (99 / 99)"))
  }
}
