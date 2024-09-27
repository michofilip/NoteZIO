package zote.endpoints

import sttp.model.StatusCode
import sttp.tapir.AnyEndpoint
import sttp.tapir.ztapir.*
import zote.exceptions.*

trait Endpoints {
  val endpoints: List[AnyEndpoint]

  protected def baseEndpoint = endpoint.errorOut(getErrorOut)

  private def getErrorOut = oneOf[Throwable](
    oneOfVariant(
      statusCode(StatusCode.NotFound).and(stringBody.mapTo[NotFoundException])
    ),
    oneOfVariant(
      statusCode(StatusCode.UnprocessableEntity)
        .and(stringBody.map(ValidationException(_))(_.getMessage))
    ),
    oneOfDefaultVariant(
      statusCode(StatusCode.InternalServerError).and(
        stringBody.map(new RuntimeException(_))(_.getMessage)
      )
    )
  )
}
