package ScalaScape.components

class GameState:
  val targetFps: Int           = 30
  var selectedScene: Scene     = WorldMenuScene()
  var activityLog: ActivityLog = ActivityLog()

  var inventory = new Inventory(
    Map(
      "Wood"  -> InventoryItem("Wood", 0),
      "Stone" -> InventoryItem("Stone", 0)
    )
  )
  
  var skills: Map[String, Skill] = Map(
    "Woodcutting" -> Woodcutting()
  )
  
  def swapScene(scene: Scene): Unit =
    selectedScene = scene
    
    activityLog.add(s"Entered ${scene.name}")
  end swapScene
  
  def getScene: Scene = selectedScene
end GameState
