package zote

import zio.*
import zote.dto.form.{LabelForm, NoteForm, NotePersonForm, PersonForm}
import zote.enums.{NotePersonRole, NoteStatus}
import zote.services.{LabelService, NoteService, PersonService}

trait InitHelper {
  def initDb(): Task[Unit]
}

object InitHelper {
  def initDb() = ZIO.serviceWithZIO[InitHelper](_.initDb())
}

case class InitHelperImpl(
    private val noteService: NoteService,
    private val personService: PersonService,
    private val labelService: LabelService
) extends InitHelper {
  override def initDb(): Task[Unit] = {
    for {
      label1 <- labelService.create(LabelForm(name = "Red"))
      label2 <- labelService.create(LabelForm(name = "Green"))
      label3 <- labelService.create(LabelForm(name = "Blue"))

      person1 <- personService.create(PersonForm(name = "Ala"))
      person2 <- personService.create(PersonForm(name = "Ela"))
      person3 <- personService.create(PersonForm(name = "Ola"))
      person4 <- personService.create(PersonForm(name = "Ula"))

      note1 <- noteService.create(
        NoteForm(
          title = "Title 1",
          message = "Message 1",
          status = NoteStatus.Draft,
          assignees = Set(
            NotePersonForm(personId = person1.id, role = NotePersonRole.Owner)
          ),
          parentId = None,
          labels = Set(label1.id)
        )
      )
      note2 <- noteService.create(
        NoteForm(
          title = "Title 2",
          message = "Message 2",
          status = NoteStatus.Ongoing,
          assignees = Set(
            NotePersonForm(personId = person1.id, role = NotePersonRole.Owner),
            NotePersonForm(
              personId = person2.id,
              role = NotePersonRole.Maintainer
            ),
            NotePersonForm(
              personId = person3.id,
              role = NotePersonRole.Observer
            )
          ),
          parentId = Some(note1.header.id),
          labels = Set(label1.id, label2.id, label3.id)
        )
      )
      note3 <- noteService.create(
        NoteForm(
          title = "Title 3",
          message = "Message 3",
          status = NoteStatus.Ongoing,
          assignees = Set(
            NotePersonForm(personId = person3.id, role = NotePersonRole.Owner)
          ),
          parentId = Some(note2.header.id),
          labels = Set(label2.id)
        )
      )
      note4 <- noteService.create(
        NoteForm(
          title = "Title 4",
          message = "Message 4",
          status = NoteStatus.Complete,
          assignees = Set(
            NotePersonForm(personId = person4.id, role = NotePersonRole.Owner)
          ),
          parentId = Some(note2.header.id),
          labels = Set(label3.id)
        )
      )
      note5 <- noteService.create(
        NoteForm(
          title = "Title 5",
          message = "Message 5",
          status = NoteStatus.Draft,
          assignees = Set(),
          parentId = None,
          labels = Set()
        )
      )
    } yield ()
  }
}

object InitHelperImpl {
  lazy val layer = ZLayer.derive[InitHelperImpl]
}
