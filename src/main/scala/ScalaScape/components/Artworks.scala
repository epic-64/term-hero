package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*
import ScalaScape.utils.TerminalArt

def WoodCuttingArtwork(pos: Pos) =
  val art: String =
    """
      |               ,@@@@@@@,
      |       ,,,.   ,@@@@@@/@@,  .oo8888o.
      |    ,&%%&%&&%,@@@@@/@@@@@@,:8888\88/8o
      |   ,%&\%&&%&&%,@@@\@@/@@@88\88888/88'
      |   %&&%&%&/%&&%@@\@@/ /@@@88888\88888'
      |   %&&%/ %&%%&&@@\ V /@@' `88\8 `/88'
      |   `&%\ ` /%&'    |.|        \ '|8'
      |       |o|        | |         | |
      |       |.|        | |         | |
      | ___ \/ ._\//_/__/  ,\_\//__\/.  \_//__
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

  val colors: String =
    """
      |               ,@@@@@@@,
      |       ,,,.   ,@@@@@@/@@,  .oo8888o.
      |    ,&%%&%&&%,@@@@@/@@@@@@,:8888\88/8o
      |   ,%&\%&&%&&%,@@@\@@/@@@88\88888/88'
      |   %&&%&%&/%&&%@@\@@/ /@@@88888\88888'
      |   %&&%/ %&%%&&@@\ V /@@' `88\8 `/88'
      |   `&%\ ` /%&'    |.|        \ '|8'
      |       |W|        | |         | |
      |       |.|        | |         | |
      | ___ B/ ._\BG_B__/  G\_BGG__B/.  \_BG__
      |""".stripMargin

  TerminalParagraph(TerminalArt.parse(art, colors, Pos(pos.x, pos.y - 1), colorMap))
end WoodCuttingArtwork

def WorldMapArtwork(pos: Pos) =
  val art: String = """
    |           _T      .,,.    ~--~ ^^
    |     ^^   // \                    ~
    |          ][O]    ^^      ,-~ ~
    |       /''-I_I         _II____
    |    __/_  /   \ ______/ ''   /'\_,__
    |      | II--'''' \,--:--..,_/,.-{ },
    |    ; '/__\,.--';|   |[] .-.| O{ _ }
    |    :' |  | []  -|   ''--:.;[,.'\,/
    |    '  |[]|,.--'' '',   ''-,.    |
    |      ..    ..-''    ;       ''. '
    """.stripMargin

  val colorMap = Map(
    '\'' -> YELLOW,
    '^' -> WHITE_BRIGHT,
    'C' -> CYAN,
    'B' -> BLUE_BRIGHT,
  )

  val colors: String ="""
    |           _T      .,,.    ~--~ ^^
    |     ^^   // \                    ~
    |          ][O]    ^^      ,-~ ~
    |       /''-I_I         _II____
    |    __/_  /   \ ______/ ''   /'\_,__
    |      | II--'''' \,--:--..,_/,.-{ },
    |    ; '/__\,.--';|   |BB .-.| O{ _ }
    |    :' |  | BB  -|   ''--:.;[,.'\,/
    |    '  |[]|,.--'' '',   ''-,.    |
    |      ..    ..-''    ;       ''. '
    """.stripMargin

  TerminalParagraph(TerminalArt.parse(art, colors, Pos(pos.x, pos.y - 1), colorMap))
end WorldMapArtwork