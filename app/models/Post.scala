package models

import javax.inject.Inject

import common.persistence.Model
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by mackbookpro on 30/09/15.
 */
case class Post(
                 val id: Option[Long],
                 var title: String,
                 var subtitle: String,
                 var content: String,
                 var cover: String,
                 val author: String,
                 val createdAt: Long,
                 var updatedAt: Long
               )
case class PostData(
                     val title: String,
                     val subtitle: String,
                     val content: String,
                     val cover: String
                   )

class PostService @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends Model[Post]{

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  val db = dbConfig.db

  import dbConfig.driver.api._

  private val Posts = TableQuery[PostsTable]

  override def delete(id: Long): Future[Int] = db.run(Posts.filter(_.id === id).delete)

  override def byId(id: Long): Future[Option[Post]] = db.run(Posts.filter(_.id === id).result.headOption)
  def byId(id: Option[Long]): Future[Option[Post]] = db.run(Posts.filter(_.id === id).result.headOption)

  override def listAll: Future[Seq[Post]] = db.run(Posts.sortBy(_.createdAt.desc)result)

  def insertOrUpdate(post: Post): Future[Option[Long]] =
    db.run( (Posts returning Posts.map(_.id)).insertOrUpdate(post)).map{
      case Some(x) => Some(x)
      case None => post.id
    }
  def setPostCover(postId: Long, coverString: String) : Future[Int]=
    db.run(Posts.filter(_.id === postId).result.head).flatMap{ post =>
      db.run(Posts.insertOrUpdate(post.copy(cover= coverString)))

    }

  private class PostsTable(tag: Tag) extends Table[Post](tag, "post"){
    def id = column[Long]("id",O.PrimaryKey, O.AutoInc)
    def title =  column[String]("title")
    def subtitle =  column[String]("subtitle")
    def content= column[String]("content")
    def cover = column[String]("cover")
    def author = column[String]("author")
    def createdAt = column[Long]("created_at")
    def updatedAt = column[Long]("updated_at")

    override def *  = (id.?, title,subtitle,content,cover,author,createdAt,updatedAt)<>(Post.tupled, Post.unapply)
  }
}