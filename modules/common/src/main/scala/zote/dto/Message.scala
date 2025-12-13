package zote.dto

import sttp.tapir.Schema
import zio.json.JsonCodec
import zote.enums.MessageType

case class Message(
    messageType: MessageType,
    text: String,
) derives JsonCodec,
      Schema

object Message {
  def debug(text: String): Message   = Message(MessageType.Debug, text)
  def error(text: String): Message   = Message(MessageType.Error, text)
  def info(text: String): Message    = Message(MessageType.Info, text)
  def warning(text: String): Message = Message(MessageType.Warning, text)
}
