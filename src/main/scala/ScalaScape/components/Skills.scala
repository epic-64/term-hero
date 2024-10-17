package ScalaScape.components

import ScalaScape.utils.TerminalArt
import com.googlecode.lanterna.TextColor.ANSI.*

trait Skill:
  val name: String
  var xp: Int
  var level: Int
  var actionProgress: Between0And1 = 0.0
  val actionDuration: Seconds      = 3.0

  def xpForNextLevel: Int         = level * 100
  def progressToNextLevel: Double = xp.toDouble / xpForNextLevel
  def remainingDuration: Double   = actionDuration * (1 - actionProgress)

  private def gainXp(amount: Int): Unit = {
    xp += amount

    if (xp >= xpForNextLevel) {
      level += 1
      xp = 0
    }
  }

  protected def onComplete(state: GameState): Unit = ()

  def update(state: GameState, targetFps: Int): Unit = {
    actionProgress = actionProgress min 1.0

    if (actionProgress >= 1.0) {
      actionProgress = 0.0
      gainXp(10)
      onComplete(state)
    } else {
      actionProgress += 1.0 / (actionDuration * targetFps)
    }
  }

  def getAsciiArt(position: Pos): TerminalParagraph = parseArt(position)

  protected def parseArt(position: Pos): TerminalParagraph = ???
end Skill

case class Woodcutting() extends Skill {
  val name: String = "Woodcutting"
  var xp: Int      = 0
  var level: Int   = 1

  override def onComplete(state: GameState): Unit =
    state.inventory = state.inventory.updated("Wood", state.inventory("Wood") + 1)

  override def parseArt(position: Pos): TerminalParagraph = WoodCuttingArtwork(position)
}

case class Quarrying() extends Skill {
  val name: String = "Quarrying"
  var xp: Int      = 0
  var level: Int   = 1

  override def onComplete(state: GameState): Unit =
    state.inventory = state.inventory.updated("Stone", state.inventory("Stone") + 1)

  override def parseArt(position: Pos): TerminalParagraph = {
    val art: String = """
      |           .           .     .
      |  .      .      *           .       .
      |                 .       .   . *
      |  .      ------    .      . .
      |   .    /WWWI; \  .       .
      |       /WWWWII; =====;    .   /WI; \
      |      /WWWWWII;..      _  . /WI;:. \
      |  .  /WWWWWIIIIi;..      _/WWWIIII:.. _
      |    /WWWWWIIIi;;;:...:   ;\WWWWWWIIIII;
      |  /WWWWWIWIiii;;;.:.. :   ;\WWWWWIII;;;
      |""".stripMargin

    val colorMap = Map(
      '-'  -> WHITE_BRIGHT,
      '/'  -> WHITE_BRIGHT,
      '\\' -> WHITE_BRIGHT,
      ':'  -> WHITE_BRIGHT,
      'U'  -> WHITE_BRIGHT,
      'B'  -> BLUE_BRIGHT,
      'R'  -> RED_BRIGHT,
      'L'  -> YELLOW_BRIGHT
    )

    val colors: String = """
     |           .           U     .
     |  .      R      U           .       .
     |                 .       .   . L
     |  B      ------    .      B .
     |   .    /WWWI; \  .       .
     |       /WWWWII; =====;    .   /WI; \
     |      /WWWWWII;..      _  . /WI;:. \
     |  .  /WWWWWIIIIi;..      _/WWWIIII:.. _
     |    /WWWWWIIIi;;;:...:   ;\WWWWWWIIIII;
     |  /WWWWWIWIiii;;;.:.. :   ;\WWWWWIII;;;
     |""".stripMargin

    TerminalParagraph(TerminalArt.parse(art, colors, Pos(position.x, position.y - 1), colorMap))
  }
}

case class Woodworking() extends Skill {
  val name: String = "Woodworking"
  var xp: Int      = 0
  var level: Int   = 1
}

case class Stonecutting() extends Skill {
  val name: String = "Stonecutting"
  var xp: Int      = 0
  var level: Int   = 1
}
