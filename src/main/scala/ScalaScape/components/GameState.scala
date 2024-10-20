package ScalaScape.components

class GameState:
  val targetFps: Int       = 30
  var selectedScene: Scene = WorldMenuScene(this)
  var activityLog          = ActivityLog()
  var inventory            = Inventory()

  class SkillList {
    val woodcutting: Woodcutting = Woodcutting()
  }

  var skills: SkillList = new SkillList()

  def swapScene(scene: Scene): Unit =
    selectedScene = scene
    activityLog.add(s"Entered ${scene.name}")
  end swapScene

  def getScene: Scene = selectedScene
end GameState
