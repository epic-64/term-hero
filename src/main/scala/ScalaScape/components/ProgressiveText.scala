package ScalaScape.components

import com.googlecode.lanterna.{SGR, TextColor}

class ProgressiveText(line: String, framesPerCharacter: Int = 1):
  private var frameCount   = 0
  private var currentText  = ""
  private var currentIndex = 0

  private def isComplete: Boolean = currentIndex == line.length

  private def update(): ProgressiveText =
    if isComplete then return this

    frameCount += 1

    if frameCount % framesPerCharacter == 0 then
      currentText += line(currentIndex)
      currentIndex += 1
    end if

    this
  end update

  def render(pos: Pos): RenderedBlock =
    update() // caller should not need to call this manually

    if currentText.isEmpty then return RenderedBlock.empty

    val line = if (isComplete) {
      ColorLine(List(ColorWord(currentText)))
    } else {
      // make the LAST character bold
      val lastChar = ColorWord(currentText.last.toString, TextColor.ANSI.WHITE_BRIGHT, Some(SGR.BOLD))
      ColorLine(List(ColorWord(currentText.dropRight(1)), lastChar))
    }

    RenderedBlock(line.render(pos))
  end render
end ProgressiveText
