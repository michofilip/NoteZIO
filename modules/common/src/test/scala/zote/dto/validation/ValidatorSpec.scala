package zote.dto.validation

import com.softwaremill.quicklens.modify
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO}
import zote.dto.form.{LabelForm, NoteForm, PersonForm}
import zote.enums.NoteStatus

object ValidatorSpec extends ZIOSpecDefault {

  private val labelForm = LabelForm(
    name = "name"
  )

  private val noteForm = NoteForm(
    title = "title",
    message = "message",
    status = NoteStatus.Draft,
    assignees = Set.empty,
    parentId = None,
    labels = Set.empty
  )

  private val personForm = PersonForm(
    name = "name"
  )

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("Validator")(
      suite("provides function 'validateZIO' that")(
        suite("for LabelForm")(
          test("returns unit if the LabelForm is validated successfully") {
            for {
              result <- Validator
                .validateZIO(labelForm)
                .foldZIO(_ => ZIO.succeed(false), _ => ZIO.succeed(true))
            } yield assertTrue(result)
          },
          test("returns validation error if the LabelForm name is blank") {
            for {
              result <- Validator.validateZIO {
                labelForm
                  .modify(_.name)
                  .setTo("")
              }.flip
            } yield assertTrue {
              result.messages.contains("Name cannot be blank")
            }
          },
          test("returns validation error if the LabelForm name is too long") {
            for {
              result <- Validator.validateZIO {
                labelForm
                  .modify(_.name)
                  .setTo(
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
                  )
              }.flip
            } yield assertTrue {
              result.messages.contains(
                "Name cannot be longer then 50 characters"
              )
            }
          }
        ),
        suite("for NoteForm")(
          test("returns unit if the NoteForm is validated successfully") {
            for {
              result <- Validator
                .validateZIO(noteForm)
                .foldZIO(_ => ZIO.succeed(false), _ => ZIO.succeed(true))
            } yield assertTrue(result)
          },
          test("returns validation error if the NoteForm title is blank") {
            for {
              result <- Validator.validateZIO {
                noteForm
                  .modify(_.title)
                  .setTo("")
              }.flip
            } yield assertTrue {
              result.messages.contains("Title cannot be blank")
            }
          },
          test("returns validation error if the NoteForm title is too long") {
            for {
              result <- Validator.validateZIO {
                noteForm
                  .modify(_.title)
                  .setTo(
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
                  )
              }.flip
            } yield assertTrue {
              result.messages.contains(
                "Title cannot be longer then 50 characters"
              )
            }
          },
          test("returns validation error if the NoteForm message is blank") {
            for {
              result <- Validator.validateZIO {
                noteForm
                  .modify(_.message)
                  .setTo("")
              }.flip
            } yield assertTrue {
              result.messages.contains("Message cannot be blank")
            }
          },
          test(
            "returns validation error with multiple messages if the NoteForm is invalid in multiple ways"
          ) {
            for {
              result <- Validator.validateZIO {
                noteForm
                  .modify(_.title)
                  .setTo("")
                  .modify(_.message)
                  .setTo("")
              }.flip
            } yield assertTrue {
              result.messages.size == 2 &&
              result.messages.contains("Title cannot be blank") &&
              result.messages.contains("Message cannot be blank")
            }
          }
        ),
        suite("for PersonForm")(
          test("returns unit if the PersonForm is validated successfully") {
            for {
              result <- Validator
                .validateZIO(personForm)
                .foldZIO(_ => ZIO.succeed(false), _ => ZIO.succeed(true))
            } yield assertTrue(result)
          },
          test("returns validation error if the PersonForm name is blank") {
            for {
              result <- Validator.validateZIO {
                personForm
                  .modify(_.name)
                  .setTo("")
              }.flip
            } yield assertTrue {
              result.messages.contains("Name cannot be blank")
            }
          },
          test("returns validation error if the PersonForm name is too short") {
            for {
              result <- Validator.validateZIO {
                personForm
                  .modify(_.name)
                  .setTo("X")
              }.flip
            } yield assertTrue {
              result.messages.contains(
                "Name cannot be shorter then 2 characters"
              )
            }
          },
          test("returns validation error if the PersonForm name is too long") {
            for {
              result <- Validator.validateZIO {
                personForm
                  .modify(_.name)
                  .setTo(
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                      "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                      "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
                      "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
                  )
              }.flip
            } yield assertTrue {
              result.messages.contains(
                "Name cannot be longer then 255 characters"
              )
            }
          },
          test(
            "returns validation error with multiple messages if the PersonForm is invalid in multiple ways"
          ) {
            for {
              result <- Validator.validateZIO {
                personForm
                  .modify(_.name)
                  .setTo("")
              }.flip
            } yield assertTrue {
              result.messages.size == 2 &&
              result.messages.contains("Name cannot be blank") &&
              result.messages.contains(
                "Name cannot be shorter then 2 characters"
              )
            }
          }
        )
      )
    )
  }
}
