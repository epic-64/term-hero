package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.graphics.TextGraphics

class SkillDisplay:
  def draw(graphics: TextGraphics, skill: Skill, position: Pos): Unit = drawSkill(skill, graphics, position)

  private def drawSkill(skill: Skill, graphics: TextGraphics, position: Pos): Unit =
    val x = position.x
    val y = position.y
    val pb = ProgressBarParameters

    def headerStart = TerminalParagraph(
      List(
        TerminalString(s"${skill.name} (${skill.level} / 99)", Pos(x, y), WHITE),
        TerminalString("----------------------------------------", Pos(x, y + 1), WHITE)
      )
    )

    def asciiArt: TerminalParagraph = skill.getAsciiArt(Pos(x, y + 2))

    def headerEnd = TerminalParagraph(
      List(
        TerminalString("----------------------------------------", Pos(x, y + 12), WHITE)
      )
    )

    def xpBar = TerminalParagraph(
      List(TerminalString(s"XP Progress: ${skill.xp} / ${skill.xpForNextLevel}", Pos(x, y + 13), WHITE))
        ++ ProgressBar.from(pb(40, skill.progressToNextLevel, Pos(x, y + 14), BLUE_BRIGHT))
    )

    def actionBar = TerminalParagraph(
      List(
        TerminalString(s"Action Progress: ETA: ", Pos(x, y + 16), WHITE),
        TerminalString(f"${skill.remainingDuration}%1.1f", Pos(x + 22, y + 16), CYAN_BRIGHT),
        TerminalString(" seconds", Pos(x + 26, y + 16), WHITE)
      )
        ++ ProgressBar.from(pb(40, skill.actionProgress, Pos(x, y + 17), GREEN_BRIGHT))
    )

    headerStart.draw(graphics)
    asciiArt.draw(graphics)
    headerEnd.draw(graphics)
    xpBar.draw(graphics)
    actionBar.draw(graphics)
  end drawSkill
end SkillDisplay