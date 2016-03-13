package controllers

import javax.inject.{Inject, Singleton}

import models._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi

import scala.concurrent.{Future, ExecutionContext}

@Singleton
class Application @Inject()(userService: UserService, postService: PostService, messageService: MessageService, val messagesApi: MessagesApi) (implicit exec: ExecutionContext)extends Controller with I18nSupport {


  val userLoginForm = Form(
    mapping(
      "email" -> email,
      "password" -> text
    )(LoginForm.apply)(LoginForm.unapply)
  )
  val postForm = Form(
    mapping(
      "title" -> text,
      "subtitle" -> text,
      "content" -> text,
      "cover" -> text
    )(PostData.apply)(PostData.unapply)
  )

  def index = Action.async{
    postService.listAll.map{ posts =>
      Ok(views.html.index(posts))
    }

  }

  def about = Action {
    Ok(views.html.about())
  }
  def  contact = Action {implicit request =>
    Ok(views.html.contact())
  }
   def postMessage = Action.async{implicit request =>
      request.body.asFormUrlEncoded match {
        case Some(fields) => {
          val firstName = fields.get("firstName").get.head
          val lastName = fields.get("lastName").get.head
          val email = fields.get("email").get.head
          val phone = fields.get("phone").get.head
          val message = fields.get("message").get.head
          messageService.insertOrUpdate(Message(Some(0),firstName,lastName,email,phone,message,System.currentTimeMillis(),System.currentTimeMillis())).map{
            case Some(messageId) => {
             Ok("")
            }
            case None => {
             Ok("")
            }
          }
        }
        case None => {
         Future(NoContent)
        }
      }

   }

  def showPost(id:Long) = Action.async {
    postService.byId(id).map{
      case Some(post) =>{
        Ok(views.html.post(post))
      }
      case None =>{
        NoContent
      }
    }

  }

  def login () = Action.async{implicit request=>
    request.session.get("connected").map{ connected =>
      userService.findByEmail(connected).map{ user =>
          Redirect(routes.Application.dindex())
      }
    }.getOrElse{
      Future(Ok(views.html.login(userLoginForm)))
    }
  }
  def postLogin () = Action.async{ implicit request =>
    userLoginForm.bindFromRequest.fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        Logger.debug("Error Login")
        Future(BadRequest(views.html.login(formWithErrors)))
      },
      loginForm => {
        userService.authentication(loginForm.email,loginForm.password).map{
          case Some(user) =>{
           Redirect(routes.Application.dindex).withSession("connected" -> user.email)
          }
          case None => {
            Ok(views.html.login(userLoginForm)).flashing("message" -> "Désolé mot de passe ou login incorrect")
          }
        }

      }
    )
  }

  def dindex = Action.async{implicit request=>
    request.session.get("connected").map{ connected =>
      userService.findByEmail(connected).map{ user =>
        Ok(views.html.dindex(user))
      }
    }.getOrElse{
      Future(Unauthorized("Oops, you are not connected"))
    }
  }
  def logout() = Action{implicit request=>
    Redirect("/login").withNewSession
  }
  def allPost = Action.async{implicit request=>
    request.session.get("connected").map{ connected =>
      userService.findByEmail(connected).flatMap{ user =>
        postService.listAll.map( posts =>
          Ok(views.html.dpost(user,posts))
        )
      }
    }.getOrElse{
      Future(Unauthorized("Oops, you are not connected"))
    }
  }

  def createPost = Action.async{ implicit request =>
    request.session.get("connected").map{ connected =>
      userService.findByEmail(connected).map{ user =>

          Ok(views.html.addpost(user,postForm))
      }
    }.getOrElse{
      Future(Unauthorized("Oops, you are not connected"))
    }
  }
  def storePost = Action.async{ implicit request =>
      postForm.bindFromRequest.fold(
        formWithErrors => {
          // binding failure, you retrieve the form containing errors:
          Logger.debug("Error Login")
          request.session.get("connected").map{ connected =>
            userService.findByEmail(connected).map{ user =>

              BadRequest(views.html.addpost(user,postForm))
            }
          }.getOrElse{
            Future(Unauthorized("Oops, you are not connected"))
          }
        },
        postData => {
          /* binding success, you get the actual value. */

          postService.insertOrUpdate(Post(Some(0),postData.title,postData.subtitle,postData.content,postData.cover,"Ange Bagui",System.currentTimeMillis(),System.currentTimeMillis())).map{
            case Some(post) => {
              Redirect(routes.Application.allPost()).flashing("message" -> "Post saved successfully")
            }
            case None =>{
              Redirect(routes.Application.allPost()).flashing("error" -> "Post non saved")
            }
          }
        }
      )
  }
  def editPost(id:Long) =  Action.async{ implicit request =>
    request.session.get("connected").map{ connected =>
      userService.findByEmail(connected).flatMap{ user =>

        postService.byId(id).map{
          case Some(post) => {
            val fillPostForm = postForm.fill(PostData(post.title,post.subtitle,post.content,post.cover))
            Ok(views.html.editpost(user,post.id, fillPostForm))
          }
          case None => NotFound

        }

      }
    }.getOrElse{
      Future(Unauthorized("Oops, you are not connected"))
    }
  }

  def deletePost(id: Long) = Action.async{ implicit request =>
    request.session.get("connected").map{ connected =>
      userService.findByEmail(connected).flatMap{ user =>
        postService.delete(id).map{ postId =>
          Redirect(routes.Application.allPost()).flashing("message" -> "Post delete successfully")
        }

      }
    }.getOrElse{
      Future(Unauthorized("Oops, you are not connected"))
    }

  }

  def updatePost = Action.async{ implicit request =>
    postForm.bindFromRequest.fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        Logger.debug("Error Login")
        request.session.get("connected").map{ connected =>
          userService.findByEmail(connected).map{ user =>

            BadRequest(views.html.addpost(user,postForm))
          }
        }.getOrElse{
          Future(Unauthorized("Oops, you are not connected"))
        }
      },
      postData => {
        /* binding success, you get the actual value. */
          request.body.asFormUrlEncoded match {
          case Some(fields) => {
              val postId = fields.get("postId").map(_.head.toLong)
              postService.byId(postId).flatMap{
                case Some(postFound) => {
                  postService.insertOrUpdate(postFound.copy(title = postData.title, subtitle = postData.subtitle,content = postData.content, cover = postData.cover,updatedAt=System.currentTimeMillis())).map{
                    case Some(post) => {
                      Redirect(routes.Application.allPost()).flashing("message" -> "Post updated successfully")
                    }
                    case None =>{
                      Redirect(routes.Application.allPost()).flashing("error" -> "Post non updated")
                    }
                  }
                }
                case None => {
                  Future(NotFound)
                }

            }
          }
          case None => Future(Redirect(routes.Application.allPost()).flashing("error" -> "Post non updated"))
        }

      }
    )
  }



  def allMessage = Action.async{ implicit request =>
    request.session.get("connected").map { connected =>
      userService.findByEmail(connected).flatMap { user =>
         messageService.listAll.map { messages =>
          Ok(views.html.dmessage(user, messages))
        }
      }
    }.getOrElse{
      Future(Unauthorized("Oops, you are not connected"))
    }
  }
  def deleteMessage(id: Long) = Action.async{ implicit request =>
    request.session.get("connected").map{ connected =>
      userService.findByEmail(connected).flatMap{ user =>
        messageService.delete(id).map{ postId =>
          Redirect(routes.Application.allMessage()).flashing("message" -> "Message delete successfully")
        }

      }
    }.getOrElse{
      Future(Unauthorized("Oops, you are not connected"))
    }
  }

  def createUser(name: String,email: String, password: String) = Action.async{ implicit request =>
    userService.insertOrUpdate(User(Some(0),name,email,password,System.currentTimeMillis(),System.currentTimeMillis())).map{
      case Some(user) =>{
        Ok("")
      }
      case None => {
        Ok("")
      }
    }
  }

}
