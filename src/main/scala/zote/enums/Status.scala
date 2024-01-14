package zote.enums

import io.getquill.MappedEncoding
import zio.json.{JsonDecoder, JsonEncoder}

enum Status {
    case Draft
    case Ongoing
    case Complete
}

object Status {

    given JsonEncoder[Status] = JsonEncoder[String].contramap(_.toString)

    given JsonDecoder[Status] = JsonDecoder[String].map(Status.valueOf)

    given MappedEncoding[Status, String] = MappedEncoding(_.toString)

    given MappedEncoding[String, Status] = MappedEncoding(Status.valueOf)
}