package ScalaScape.game.skills

import ScalaScape.GameState
import ScalaScape.ui.lantern.{Position, TerminalString}
import ScalaScape.utils.TerminalArt
import com.googlecode.lanterna.TextColor.ANSI.*

trait Skill:
  val name: String
  var xp: Int
  var level: Int
  var actionProgress: Double                               = 0.0
  val actionDurationSeconds: Double                        = 3.0
  private var cachedAsciiArt: Option[List[TerminalString]] = None

  def xpForNextLevel: Int         = level * 100
  def progressToNextLevel: Double = xp.toDouble / xpForNextLevel
  def remainingDuration: Double   = actionDurationSeconds * (1 - actionProgress)

  private def gainXp(amount: Int): Unit = {
    xp += amount

    if (xp >= xpForNextLevel) {
      level += 1
      xp = 0
    }
  }

  protected def onComplete(state: GameState): Unit = ()

  def update(state: GameState, targetFps: Int): Unit = {
    actionProgress += 1.0 / (actionDurationSeconds * targetFps)

    if (actionProgress >= 1.0) {
      actionProgress = 0.0
      gainXp(10)

      onComplete(state)
    }
  }

  // Method to retrieve cached ASCII art or parse it once
  def getAsciiArt(position: Position): List[TerminalString] =
    cachedAsciiArt match {
      case Some(art) => art
      case None      =>
        val parsedArt = parseArt(position)
        cachedAsciiArt = Some(parsedArt)
        parsedArt
    }

  // Abstract method to be implemented by each skill for parsing art
  protected def parseArt(position: Position): List[TerminalString] = ???
end Skill

case class Woodcutting() extends Skill {
  val name: String = "Woodcutting"
  var xp: Int      = 0
  var level: Int   = 1

  override def onComplete(state: GameState): Unit = {
    state.inventory = state.inventory.updated("Wood", state.inventory("Wood") + 1)
  }

  override def parseArt(position: Position): List[TerminalString] = {
    val art: String = """
                        |              ,@@@@@@@,
                        |      ,,,.   ,@@@@@@/@@,  .oo8888o.
                        |   ,&%%&%&&%,@@@@@/@@@@@@,:8888\88/8o
                        |  ,%&\%&&%&&%,@@@\@@/@@@88\88888/88'
                        |  %&&%&%&/%&&%@@\@@/ /@@@88888\88888'
                        |  %&&%/ %&%%&&@@\ V /@@' `88\8 `/88'
                        |  `&%\ ` /%&'    |.|        \ '|8'
                        |      |o|        | |         | |
                        |      |.|        | |         | |
                        |___ \/ ._\//_/__/  ,\_\//__\/.  \_//__
                        |""".stripMargin

    val colorMap = Map(
      '@' -> GREEN_BRIGHT,
      '&' -> GREEN,
      '%' -> GREEN,
      '8' -> GREEN,
      'o' -> GREEN,
      'G' -> GREEN,
      'B' -> GREEN_BRIGHT,
      'W' -> WHITE
    )

    val colors: String = """
                           |              ,@@@@@@@,
                           |      ,,,.   ,@@@@@@/@@,  .oo8888o.
                           |   ,&%%&%&&%,@@@@@/@@@@@@,:8888\88/8o
                           |  ,%&\%&&%&&%,@@@\@@/@@@88\88888/88'
                           |  %&&%&%&/%&&%@@\@@/ /@@@88888\88888'
                           |  %&&%/ %&%%&&@@\ V /@@' `88\8 `/88'
                           |  `&%\ ` /%&'    |.|        \ '|8'
                           |      |W|        | |         | |
                           |      |.|        | |         | |
                           |___ B/ ._\BG_B__/  G\_BGG__B/.  \_BG__
                           |""".stripMargin

    TerminalArt.parse(art, colors, Position(position.x, position.y - 1), colorMap)
  }
}

case class Quarrying() extends Skill {
  val name: String = "Quarrying"
  var xp: Int      = 0
  var level: Int   = 1

  override def onComplete(state: GameState): Unit = {
    state.inventory = state.inventory.updated("Stone", state.inventory("Stone") + 1)
  }

  override def parseArt(position: Position): List[TerminalString] = {
    val art: String = """
                        |          .           .     .
                        | .      .      *           .       .
                        |                .       .   . *
                        | .      ------    .      . .
                        |  .    /WWWI; \  .       .
                        |      /WWWWII; =====;    .   /WI; \
                        |     /WWWWWII;..      _  . /WI;:. \
                        | .  /WWWWWIIIIi;..      _/WWWIIII:.. _
                        |   /WWWWWIIIi;;;:...:   ;\WWWWWWIIIII;
                        | /WWWWWIWIiii;;;.:.. :   ;\WWWWWIII;;;
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
                           |          .           U     .
                           | .      R      U           .       .
                           |                .       .   . L
                           | B      ------    .      B .
                           |  .    /WWWI; \  .       .
                           |      /WWWWII; =====;    .   /WI; \
                           |     /WWWWWII;..      _  . /WI;:. \
                           | .  /WWWWWIIIIi;..      _/WWWIIII:.. _
                           |   /WWWWWIIIi;;;:...:   ;\WWWWWWIIIII;
                           | /WWWWWIWIiii;;;.:.. :   ;\WWWWWIII;;;
                           |""".stripMargin

    TerminalArt.parse(art, colors, Position(position.x, position.y - 1), colorMap)
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
