package graphql
import sangria.schema._
import grpc.ProductsGrpcClient
import example.products.products.{GetProductReply}

import scala.concurrent.{ExecutionContext, Future}

object ProductSchema {

  def createSchema(client: ProductsGrpcClient)(implicit ec: ExecutionContext): Schema[Unit, Unit] = {

    // 🔹 Tipos GraphQL basados en los .proto
    val ProductType: ObjectType[Unit, GetProductReply] = ObjectType(
      "Product",
      fields[Unit, GetProductReply](
        Field("id", StringType, resolve = _.value.id),
        Field("name", StringType, resolve = _.value.name),
        Field("price", IntType, resolve = _.value.price),
        Field("order", StringType, resolve = _.value.order)
      )
    )

    val QueryType = ObjectType("Query", fields[Unit, Unit](
      Field("products", ListType(ProductType),
        description = Some("Lista de todos los productos"),
        resolve = _ => client.listProducts().map(_.products)
      ),
      Field("product", OptionType(ProductType),
        description = Some("Obtener producto por ID"),
        arguments = Argument("id", StringType) :: Nil,
        resolve = ctx => client.getProduct(ctx.arg[String]("id")).map(Some(_))
      )
    ))

    val MutationType = ObjectType("Mutation", fields[Unit, Unit](
      Field("addProduct", StringType,
        arguments = List(
          Argument("name", StringType),
          Argument("price", IntType),
          Argument("order", StringType)
        ),
        resolve = ctx =>
          client.addProduct(
            ctx.arg[String]("name"),
            ctx.arg[Int]("price"),
            ctx.arg[String]("order")
          ).map(_.message)
      )
    ))

    Schema(QueryType, Some(MutationType))
  }
}
