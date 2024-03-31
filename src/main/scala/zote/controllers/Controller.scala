package zote.controllers

import sttp.model.StatusCode
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import zio.*
import zote.exceptions.*

trait Controller {
    val endpoints: List[AnyEndpoint]
    val routes: List[ServerEndpoint[Any, Task]]

    protected val tag: String

    protected def baseEndpoint = endpoint.tag(tag).errorOut(getErrorOut)

    private def getErrorOut = oneOf[Throwable](
        oneOfVariant(statusCode(StatusCode.NotFound).and(stringBody.mapTo[NotFoundException])),
        oneOfVariant(statusCode(StatusCode.UnprocessableEntity).and(stringBody.mapTo[ValidationException])),
        oneOfDefaultVariant(
            statusCode(StatusCode.InternalServerError).and(stringBody.map(new RuntimeException(_))(_.getMessage))
        )
    )
}
