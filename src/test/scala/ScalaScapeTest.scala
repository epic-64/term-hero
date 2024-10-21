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

  test("the menu shows the correct mastery levels") {
    val mockScreen   = mock[TerminalScreen]
    val mockGraphics = mock[TextGraphics]
    val game         = new ScalaScape(mockScreen, mockGraphics)

    val woodCuttingActionMenu = game.state.scenes.woodcutting.menu
    val oakScene = game.state.scenes.oak
    
    game.state.skills.woodcutting.mastery[OakMastery].level = 0
    assert(woodCuttingActionMenu.getItems().keys.head.words.head.content == "Oak Mastery (0 / 99)")
    val oakSceneRendered = oakScene.render(game.state, Pos(0, 0))
    val occurrence = oakSceneRendered.list.find(_.content.contains("Oak Mastery (0 / 99)"))
    assert(occurrence.isDefined)

    game.state.skills.woodcutting.mastery[OakMastery].level = 99
    assert(woodCuttingActionMenu.getItems().keys.head.words.head.content == "Oak Mastery (99 / 99)")
    val oakSceneRendered2 = oakScene.render(game.state, Pos(0, 0))
    val occurrence2 = oakSceneRendered2.list.find(_.content.contains("Oak Mastery (99 / 99)"))
    assert(occurrence2.isDefined)
  }
}
