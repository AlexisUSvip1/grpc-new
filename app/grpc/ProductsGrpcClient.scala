package grpc
import example.products.products.{AddProductReply, AddProductRequest, GetProductReply, GetProductRequest, ListProductsReply, ListProductsRequest, ProductsGrpc}
import io.grpc.ManagedChannelBuilder

import scala.concurrent.{ExecutionContext, Future}

class ProductsGrpcClient(implicit ec: ExecutionContext) {
  private val channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build()
  private val stub = ProductsGrpc.stub(channel)

  def addProduct(name: String, price: Int, order: String): Future[AddProductReply] = {
    val req = AddProductRequest(name, price, order)
    stub.addProduct(req)
  }

  def listProducts(): Future[ListProductsReply] = {
    stub.listProducts(ListProductsRequest())
  }

  def getProduct(id: String): Future[GetProductReply] = {
    stub.getProduct(GetProductRequest(id))
  }
}
