package zote.services

import com.softwaremill.quicklens.*
import zio.*
import zote.db.QuillContext
import zote.db.model.PersonEntity
import zote.db.repositories.{NotePersonRepository, PersonRepository}
import zote.dto.{Person, PersonForm}
import zote.exceptions.NotFoundException
import zote.utils.ZIOUtils.*

trait PersonService {
    def getAll: Task[List[Person]]

    def getById(id: Long): Task[Person]

    def getByIdIn(ids: Seq[Long]): Task[List[Person]]

    def create(personForm: PersonForm): Task[Person]

    def update(id: Long, personForm: PersonForm): Task[Person]

    def delete(id: Long): Task[Unit]

    def validatePersonsExist(personIds: Set[Long]): Task[Unit]
}

object PersonService {
    lazy val layer = ZLayer.derive[PersonServiceImpl]
}

case class PersonServiceImpl(
    private val personRepository: PersonRepository,
    private val notePersonRepository: NotePersonRepository,
    private val quillContext: QuillContext
) extends PersonService {

    import quillContext.postgres.*

    override def getAll: Task[List[Person]] = transaction {
        personRepository.findAll.flatMap(toDtos)
    }

    override def getById(id: Long): Task[Person] = transaction {
        getEntityById(id).flatMap(toDto)
    }

    override def getByIdIn(ids: Seq[Long]): Task[List[Person]] = transaction {
        personRepository.findByIdIn(ids).flatMap(toDtos)
    }

    override def create(personForm: PersonForm): Task[Person] = validateAndUpsert(personForm) {
        PersonEntity(name = personForm.name).asZIO
    }

    override def update(id: Long, personForm: PersonForm): Task[Person] = validateAndUpsert(personForm) {
        getEntityById(id).map(_
            .modify(_.name).setTo(personForm.name)
        )
    }

    private def validateAndUpsert(personForm: PersonForm)(f: => Task[PersonEntity]): Task[Person] = transaction {
        for {
            _ <- PersonForm.validateZIO(personForm)
            personEntity <- f
            personEntity <- personRepository.upsert(personEntity)
            person <- toDto(personEntity)
        } yield person
    }

    override def delete(id: Long): Task[Unit] = transaction {
        for {
            _ <- getEntityById(id)
            _ <- personRepository.delete(id)
        } yield ()
    }

    override def validatePersonsExist(personIds: Set[Long]): Task[Unit] = {
        for {
            existingPersonIds <- personRepository.findByIdIn(personIds.toSeq).map(_.map(_.id))
            missingPersonIds <- (personIds -- existingPersonIds).asZIO
            _ <- ZIO.unless(missingPersonIds.isEmpty) {
                ZIO.fail(NotFoundException(s"Persons ids: ${missingPersonIds.mkString(", ")} not found"))
            }
        } yield ()
    }

    private def getEntityById(id: Long): Task[PersonEntity] = {
        personRepository.findById(id).someOrFail(NotFoundException(s"Person id: $id not found"))
    }

    private def toDtos(personEntities: Seq[PersonEntity]): Task[List[Person]] = {
        personEntities.map { personEntity =>
            Person(
                id = personEntity.id,
                name = personEntity.name,
            )
        }.toList.asZIO
    }

    private def toDto(personEntity: PersonEntity): Task[Person] = {
        toDtos(List(personEntity)).map(_.head)
    }
}
