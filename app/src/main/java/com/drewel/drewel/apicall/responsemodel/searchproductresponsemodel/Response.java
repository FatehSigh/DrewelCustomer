
package com.drewel.drewel.apicall.responsemodel.searchproductresponsemodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Response {

    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("data")
    @Expose
    private Data data;
    @SerializedName("is_deactivate")
    @Expose
    private String isDeactivate;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getIsDeactivate() {
        return isDeactivate;
    }

    public void setIsDeactivate(String isDeactivate) {
        this.isDeactivate = isDeactivate;
    }

}
