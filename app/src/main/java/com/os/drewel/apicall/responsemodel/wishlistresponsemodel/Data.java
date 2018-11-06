
package com.os.drewel.apicall.responsemodel.wishlistresponsemodel;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.os.drewel.apicall.responsemodel.Product;

public class Data {

    @SerializedName("Products")
    @Expose
    private List<Product> products = null;
    @SerializedName("Product")
    @Expose
    private List<Product> product = null;

    public List<Product> getProduct() {
        return product;
    }

    public void setProduct(List<Product> product) {
        this.product = product;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

}
