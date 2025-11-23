package zote.services

import com.softwaremill.quicklens.*
import zio.*
import zote.Ids.PersonId
import zote.db.QuillContext
import zote.db.model.PersonEntity
import zote.db.repositories.{NotePersonRepository, PersonRepository}
import zote.dto.Person
import zote.dto.form.PersonForm
import zote.dto.validation.Validator

trait PersonService {
  def getAll: Task[List[Person]]

  def getById(id: PersonId): Task[Person]

  def create(personForm: PersonForm): Task[Person]

  def update(id: PersonId, personForm: PersonForm): Task[Person]

  def delete(id: PersonId): Task[Unit]
}

case class PersonServiceImpl(
    private val personRepository: PersonRepository,
    private val notePersonRepository: NotePersonRepository,
    private val quillContext: QuillContext,
) extends PersonService {

  import quillContext.*

  override def getAll: Task[List[Person]] = transaction {
    personRepository.findAll.flatMap { personEntities =>
      ZIO.foreachPar(personEntities)(toPerson)
    }
  }

  override def getById(id: PersonId): Task[Person] = transaction {
    personRepository.getById(id).flatMap(toPerson)
  }

  override def create(personForm: PersonForm): Task[Person] = transaction {
    for {
      _ <- Validator.validateZIO(personForm)
      personEntity <- personRepository.upsert {
        PersonEntity(name = personForm.name)
      }
      person <- toPerson(personEntity)
    } yield person
  }

  override def update(id: PersonId, personForm: PersonForm): Task[Person] =
    transaction {
      for {
        _            <- Validator.validateZIO(personForm)
        personEntity <- personRepository.getById(id)
        personEntity <- personRepository.upsert {
          personEntity.modify(_.name).setTo(personForm.name)
        }
        person <- toPerson(personEntity)
      } yield person
    }

  override def delete(id: PersonId): Task[Unit] = transaction {
    for {
      _                  <- personRepository.getById(id)
      notePersonEntities <- notePersonRepository.findAllByPersonId(id)
      _ <- notePersonRepository
        .deleteAll(notePersonEntities.map(entity => (entity.noteId, entity.personId)))
        .unless(notePersonEntities.isEmpty)
      _ <- personRepository.delete(id)
    } yield ()
  }

  private def toPerson(personEntity: PersonEntity) = {
    ZIO.succeed {
      Person(
        id = personEntity.id,
        name = personEntity.name,
      )
    }
  }
}

object PersonServiceImpl {
  lazy val layer = ZLayer.derive[PersonServiceImpl]
}
