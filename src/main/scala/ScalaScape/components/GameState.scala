package ScalaScape.components

import com.googlecode.lanterna.screen.Screen

class GameState(val targetFps: Int, val screen: Screen):
  var activityLog          = ActivityLog()
  var inventory            = Inventory()

  var skills: SkillList = new SkillList()
  val scenes: SceneList = new SceneList(this)
  var selectedScene: Scene = scenes.world

  def getScene: Scene = selectedScene
  def swapScene(scene: Scene): GameState =
    selectedScene = scene
    activityLog.add(s"Entered ${scene.name}")
    screen.clear() // scene swapping causes lots of changes, so it's the best time to clear the screen
    this
  end swapScene

  class SkillList {
    val woodcutting: Woodcutting = Woodcutting()
  }

  class SceneList(state: GameState):
    val world: WorldMenuScene = WorldMenuScene(state)
    val gathering: GatheringMenuScene = GatheringMenuScene(state)
    val woodcutting: WoodCuttingMenuScene = WoodCuttingMenuScene(state)
    val oak: OakScene = OakScene(state)
    val teak: TeakScene = TeakScene(state)
  end SceneList
end GameState