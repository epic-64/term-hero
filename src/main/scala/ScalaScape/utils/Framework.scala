package ScalaScape.utils

trait AsList {
  def asList[T <: AnyRef]: List[T] = {
    this.getClass.getDeclaredFields.toList.map { field =>
      field.setAccessible(true)
      field.get(this).asInstanceOf[T]
    }
  }
}