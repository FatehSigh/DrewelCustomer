
package com.os.drewel.apicall.responsemodel.searchproductresponsemodel;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.os.drewel.apicall.responsemodel.Product;

public class Data {

    @SerializedName("Product")
    @Expose
    private List<Product> product = null;

    public List<Product> getProduct() {
        return product;
    }

    public void setProduct(List<Product> product) {
        this.product = product;
    }

}
