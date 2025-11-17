package me.calebeoliveira.admin.product;

import io.micronaut.serde.annotation.Serdeable;
import me.calebeoliveira.product.Product;

@Serdeable
record UpdateProductRequest(String name, Product.Type type) {
}
