package ScalaScape.components

class GameState:
  val targetFps: Int       = 30
  var selectedScene: Scene = WorldMenuScene()
  var activityLog          = ActivityLog()
  var inventory            = Inventory()

  var skills: SkillList = SkillList()

  def swapScene(scene: Scene): Unit =
    selectedScene = scene

    activityLog.add(s"Entered ${scene.name}")
  end swapScene

  def getScene: Scene = selectedScene
end GameState

class SkillList:
  val woodcutting: Woodcutting       = Woodcutting()
  val woodCuttingOak: WoodCuttingOak = WoodCuttingOak()
end SkillList
