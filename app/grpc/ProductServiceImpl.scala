package grpc

import example.products.products.{AddProductReply, AddProductRequest, GetProductReply, GetProductRequest, ListProductsReply, ListProductsRequest, ProductsGrpc}

import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

class ProductImpl(implicit ec: ExecutionContext) extends ProductsGrpc.Products {
  private val products = ListBuffer.empty[GetProductReply]

  override def addProduct(req: AddProductRequest): Future[AddProductReply] = {
   val product = GetProductReply(UUID.randomUUID().toString(), req.name, req.price, req.order)
    products+= product
    val msg = s"Producto agregado: ${req.name} (precio: ${req.price}, orden: ${req.order})"
    Future.successful(AddProductReply(msg))
  }

  override def getProduct(req: GetProductRequest): Future[GetProductReply] = {
    val found = products.find(_.id == req.id)
      .getOrElse(GetProductReply("", "No encontrado", 0, ""))
    Future.successful(found)
  }

  override def listProducts(request: ListProductsRequest): Future[ListProductsReply] = {
    Future.successful(ListProductsReply(products.toSeq))
  }
}