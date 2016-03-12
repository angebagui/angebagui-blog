package models


import javax.inject.Inject

import common.persistence.Model
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by mackbookpro on 01/10/15.
 */
case class Message(val id:Option[Long], val firstName: String, val lastName: String, val email: String, val phone: String, val message: String, val createdAt: Long, var updatedAt: Long)

case class MessageForm(val firstName: String, val lastName: String, val email: String, val phone: String, val message: String)
class MessageService @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Model[Message]{

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  val db = dbConfig.db

  import dbConfig.driver.api._

  private val Messages = TableQuery[MessagesTable]

  override def delete(id: Long): Future[Int] = db.run(Messages.filter(_.id === id).delete)

  override def byId(id: Long): Future[Option[Message]] = db.run(Messages.filter(_.id === id).result.headOption)

  override def listAll: Future[Seq[Message]] = db.run(Messages.sortBy(_.createdAt.desc)result)

  def insertOrUpdate(message: Message): Future[Option[Long]] =
    db.run( (Messages returning Messages.map(_.id)).insertOrUpdate(message)).map{
      case Some(x) => Some(x)
      case None => message.id
    }


  private class MessagesTable(tag: Tag) extends Table[Message](tag, "message"){
    def id = column[Long]("id",O.PrimaryKey, O.AutoInc)
    def firstName =  column[String]("first_name")
    def lastName =  column[String]("last_name")
    def email = column[String]("email")
    def phone = column[String]("phone")
    def message = column[String]("message")
    def createdAt = column[Long]("created_at")
    def updatedAt = column[Long]("updated_at")

    override def *  = (id.?, firstName, lastName,email,phone,message,createdAt,updatedAt)<>(Message.tupled, Message.unapply)
  }
}