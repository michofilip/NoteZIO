package zote.utils

import sttp.client4.*
import sttp.client4.impl.zio.FetchZioBackend
import sttp.tapir.Endpoint
import sttp.tapir.client.sttp4.SttpClientInterpreter
import zio.*
import zote.config.BackendClientConfig
import zote.dto.response.{NoteResponse, NotesResponse, UserResponse, UsersResponse}
import zote.dto.{Note, NoteHeader, User}
import zote.endpoints.{NoteEndpoints, UserEndpoints}

case class BackendClient(
    private val config: BackendClientConfig,
//    private val interpreter: SttpClientInterpreter,
//    private val backend: SttpBackend[Task, ZioStreams & WebSockets]
) {
  private val backend     = FetchZioBackend()
  private val interpreter = SttpClientInterpreter()

  private def requestZIO[I, E <: Throwable, O](
      endpoint: Endpoint[Unit, I, E, O, Any],
  )(payload: I): Task[O] = {
    backend
      .send(request(endpoint)(payload))
      .map(_.body)
      .absolve
  }

  private def request[I, E, O](
      endpoint: Endpoint[Unit, I, E, O, Any],
  ): I => Request[Either[E, O]] = {
//  ): I => Request[Either[E, O], Any] = {
    interpreter.toRequestThrowDecodeFailures(endpoint, Some(config.baseUri))
  }

}

object BackendClient {

  object notes {
    private val noteEndpoints: NoteEndpoints = new NoteEndpoints {}

    def getAll(consumer: NotesResponse => Unit) =
      performRequest(noteEndpoints.getAllEndpoint)(())(consumer)

    def getById(id: Long)(consumer: NoteResponse => Unit) =
      performRequest(noteEndpoints.getByIdEndpoint)(id)(consumer)
  }

  object users {
    private val userEndpoints: UserEndpoints = new UserEndpoints {}

    def getAll(consumer: UsersResponse => Unit) =
      performRequest(userEndpoints.getAllEndpoint)(())(consumer)

    def getById(id: Long)(consumer: UserResponse => Unit) =
      performRequest(userEndpoints.getByIdEndpoint)(id)(consumer)
  }

  private val layer = BackendClientConfig.layer >>> ZLayer.derive[BackendClient]

  private def performRequest[I, E <: Throwable, O](
      endpoint: Endpoint[Unit, I, E, O, Any],
  )(payload: I)(consumer: O => Unit): Fiber.Runtime[Throwable, O] = {
    Unsafe.unsafe { case given Unsafe =>
      Runtime.default.unsafe.fork(
        ZIO
          .serviceWithZIO[BackendClient](
            _.requestZIO(endpoint)(payload).tap(value => ZIO.attempt(consumer(value))),
          )
          .provide(layer),
      )
    }
  }
}
