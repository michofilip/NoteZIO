package zote.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.jsonBody
import zio.*
import zio.json.{JsonCodec, JsonEncoder}
import zote.dto.response.Response
import zote.dto.response.Response.ResponseInitializer

trait Endpoints {
  protected val tag: String
  val endpoints: List[AnyEndpoint]

  protected def jsonEndpoint[T, Res <: Response[T]: {JsonCodec, Schema}](using ResponseInitializer[T, Res]) =
    endpoint
      .tag(tag)
      .out(jsonBody[Res])
      .errorOut(statusCode and jsonBody[Res])
      .mapErrorOut[Throwable](Response.decode)(Response.encode)

  protected def secureJsonEndpoint[T, Res <: Response[T]: {JsonCodec, Schema}](using ResponseInitializer[T, Res]) =
    jsonEndpoint.securityIn(auth.bearer[String]())
}
