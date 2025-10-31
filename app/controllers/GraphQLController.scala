package controllers

import javax.inject._
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import sangria.execution._
import sangria.parser.QueryParser
import play.api.libs.json._
import sangria.marshalling.playJson._
import graphql.{ProductSchema, UserSchema}
import grpc.{ProductsGrpcClient, UserGrpcClient}
import sangria.schema._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class GraphQLController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  private val productClient = new ProductsGrpcClient()
  private val userClient = new UserGrpcClient()

  private val productSchema = ProductSchema.createSchema(productClient)
  private val userSchema = UserSchema.createSchema(userClient)


  private val mergedQuery = ObjectType(
    "Query",
    fields[Unit, Unit](
      (productSchema.query.fields ++ userSchema.query.fields)
        .map(_.asInstanceOf[Field[Unit, Unit]]): _*
    )
  )

  // === Fusionar las mutations ===
  private val mergedMutation = ObjectType(
    "Mutation",
    fields[Unit, Unit](
      (productSchema.mutation.get.fields ++ userSchema.mutation.get.fields)
        .map(_.asInstanceOf[Field[Unit, Unit]]): _*
    )
  )
  // === Crear el schema combinado ===
  private val mergedSchema = Schema(mergedQuery, Some(mergedMutation))

  // === Endpoint principal /graphql ===
  def graphql: Action[JsValue] = Action.async(parse.json) { request =>
    val query = (request.body \ "query").as[String]
    val operation = (request.body \ "operationName").asOpt[String]
    val variables = (request.body \ "variables").toOption
      .flatMap(_.asOpt[JsObject])
      .getOrElse(Json.obj())

    QueryParser.parse(query) match {
      case Success(ast) =>
        Executor.execute(
            schema = mergedSchema,
            queryAst = ast,
            variables = variables,
            operationName = operation
          ).map(Ok(_))
          .recover { case error =>
            BadRequest(Json.obj("error" -> error.getMessage))
          }

      case Failure(error) =>
        Future.successful(BadRequest(Json.obj("error" -> error.getMessage)))
    }
  }
}