package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.graphics.TextGraphics

class SkillDisplay:
  def draw(graphics: TextGraphics, state: GameState, position: Position): Unit =
    state.activeSkill match {
      case Some(skill: Woodcutting) => drawSkill(skill, graphics, position)
      case Some(skill: Quarrying) => drawSkill(skill, graphics, position)
      case _ => graphics.putString(position.x, 1, "No active skill")
    }
  end draw

  private def drawSkill(skill: Skill, graphics: TextGraphics, position: Position): Unit =
    val x = position.x
    val y = position.y
    val pb = ProgressBarParameters

    def headerStart = TerminalParagraph(
      List(
        TerminalString(s"${skill.name} (${skill.level} / 99)", Position(x, y), WHITE),
        TerminalString("----------------------------------------", Position(x, y + 1), WHITE)
      )
    )

    def asciiArt: TerminalParagraph = TerminalParagraph(skill.getAsciiArt(Position(x, y + 2)))

    def headerEnd = TerminalParagraph(
      List(
        TerminalString("----------------------------------------", Position(x, y + 12), WHITE)
      )
    )

    def xpBar = TerminalParagraph(
      List(TerminalString(s"XP Progress: ${skill.xp} / ${skill.xpForNextLevel}", Position(x, y + 13), WHITE))
        ++ ProgressBar.from(pb(40, skill.progressToNextLevel, Position(x, y + 14), BLUE_BRIGHT))
    )

    def actionBar = TerminalParagraph(
      List(
        TerminalString(s"Action Progress: ETA: ", Position(x, y + 16), WHITE),
        TerminalString(f"${skill.remainingDuration}%1.1f", Position(x + 22, y + 16), CYAN_BRIGHT),
        TerminalString(" seconds", Position(x + 26, y + 16), WHITE)
      )
        ++ ProgressBar.from(pb(40, skill.actionProgress, Position(x, y + 17), GREEN_BRIGHT))
    )

    headerStart.draw(graphics)
    asciiArt.draw(graphics)
    headerEnd.draw(graphics)
    xpBar.draw(graphics)
    actionBar.draw(graphics)
  end drawSkill
end SkillDisplay