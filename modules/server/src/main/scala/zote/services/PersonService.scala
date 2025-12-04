package zote.services

import zio.*
import zote.Ids.PersonId
import zote.db.QuillContext
import zote.db.model.PersonEntity
import zote.db.repositories.{NotePersonRepository, PersonRepository}
import zote.dto.Person
import zote.dto.form.PersonForm

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
      personEntity <- toPersonEntity(personForm)
      personEntity <- personRepository.upsert(personEntity)
      person       <- toPerson(personEntity)
    } yield person
  }

  override def update(id: PersonId, personForm: PersonForm): Task[Person] =
    transaction {
      for {
        personEntity <- personRepository.getById(id)
        personEntity <- toPersonEntity(personForm, personEntity)
        personEntity <- personRepository.upsert(personEntity)
        person       <- toPerson(personEntity)
      } yield person
    }

  inline private def toPersonEntity(
      personForm: PersonForm,
      inline personEntity: PersonEntity | Unit = (),
  ): Task[PersonEntity] = {
    inline personEntity match {
      case personEntity: PersonEntity => ZIO.succeed(personEntity.copy(name = personForm.name))
      case _                          => ZIO.succeed(PersonEntity(name = personForm.name))
    }
  }

  override def delete(id: PersonId): Task[Unit] = transaction {
    for {
      _                  <- personRepository.getById(id)
      notePersonEntities <- notePersonRepository.findAllByPersonId(id)
      _ <- ZIO.foreachDiscard(notePersonEntities) { notePersonEntity =>
        notePersonRepository.delete(notePersonEntity.noteId, notePersonEntity.personId)
      }
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
