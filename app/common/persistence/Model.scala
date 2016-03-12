package common.persistence

import scala.concurrent.Future
/**
  * Created by angebagui on 11/03/2016.
  */
trait Model[A] {

  def delete(id: Long): Future[Int]

  def byId(id: Long): Future[Option[A]]

  def listAll: Future[Seq[A]]
}

