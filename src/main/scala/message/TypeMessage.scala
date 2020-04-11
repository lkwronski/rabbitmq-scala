package message

import enumeratum._

sealed trait TypeMessage extends EnumEntry with Product with Serializable

object TypeMessage extends Enum[TypeMessage] with CirceEnum[TypeMessage]{

  case object Approved extends TypeMessage
  case object Commission extends TypeMessage
  case object PlainText extends TypeMessage

  override val values = findValues

}
