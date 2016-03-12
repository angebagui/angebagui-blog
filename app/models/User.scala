package models

import javax.inject.Inject

import common.persistence.Model
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by mackbookpro on 02/10/15.
 */
case class User(id: Option[Long], name: String, email :String, password: String, createdAt: Long, updatedAt: Long)

case class UserForm(name: String,email: String, password: String)

case class LoginForm(email: String, password: String)

class UserService @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Model[User]{

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  val db = dbConfig.db

  import dbConfig.driver.api._

  private val Users = TableQuery[UsersTable]

  override def delete(id: Long): Future[Int] = db.run(Users.filter(_.id === id).delete)

  override def byId(id: Long): Future[Option[User]] = db.run(Users.filter(_.id === id).result.headOption)

  override def listAll: Future[Seq[User]] = db.run(Users.sortBy(_.createdAt.desc)result)

  def insertOrUpdate(user: User): Future[Option[Long]] =
    db.run( (Users returning Users.map(_.id)).insertOrUpdate(user)).map{
      case Some(x) => Some(x)
      case None => user.id
    }

  def authentication(email: String, password: String): Future[Option[User]] =
    db.run(Users.filter( user=> (user.email === email) && (user.password === password)).result.headOption)

  def findByEmail(email: String): Future[User] =
  db.run(Users.filter( _.email === email ).result.head)

  private class UsersTable(tag: Tag) extends Table[User](tag, "user"){
    def id = column[Long]("id",O.PrimaryKey, O.AutoInc)
    def name =  column[String]("name")
    def email =  column[String]("email")
    def password= column[String]("password")
    def createdAt = column[Long]("created_at")
    def updatedAt = column[Long]("updated_at")

    override def *  = (id.?, name,email,password,createdAt,updatedAt)<>(User.tupled, User.unapply)
  }
}