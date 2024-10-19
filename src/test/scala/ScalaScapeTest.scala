import org.scalatest.funsuite.AnyFunSuite
import ScalaScape.ScalaScape
import _root_.ScalaScape.components.GameState

class ScalaScapeTest extends AnyFunSuite {
    test("the game can run 100 ticks without crashing") {
      val game = new ScalaScape(forceTerminal = false)
      
      for (_ <- 1 to 100) {
        var state = game.update(game.state)
      }
    }
}
