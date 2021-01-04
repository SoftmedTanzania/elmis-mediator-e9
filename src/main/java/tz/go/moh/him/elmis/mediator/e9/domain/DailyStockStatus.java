package tz.go.moh.him.elmis.mediator.e9.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class DailyStockStatus {
    @SerializedName("Plant")
    @JsonProperty("Plant")
    private String plant;

    @SerializedName("PartNum")
    @JsonProperty("PartNum")
    private String partNum;

    @SerializedName("UOM")
    @JsonProperty("UOM")
    private String oum;

    @SerializedName("PartDescription")
    @JsonProperty("PartDescription")
    private String partDescription;

    @SerializedName("OnHandQty")
    @JsonProperty("OnHandQty")
    private String onHandQty;

    @SerializedName("Date")
    @JsonProperty("Date")
    private String date;

    @SerializedName("MonthOfStock")
    @JsonProperty("MonthOfStock")
    private String monthOfStock;

    @SerializedName("IL_IDNumber")
    @JsonProperty("IL_IDNumber")
    private String ilTransactionId;

    public String getPlant() {
        return plant;
    }

    public void setPlant(String plant) {
        this.plant = plant;
    }

    public String getPartNum() {
        return partNum;
    }

    public void setPartNum(String partNum) {
        this.partNum = partNum;
    }

    public String getOum() {
        return oum;
    }

    public void setOum(String oum) {
        this.oum = oum;
    }

    public String getPartDescription() {
        return partDescription;
    }

    public void setPartDescription(String partDescription) {
        this.partDescription = partDescription;
    }

    public String getOnHandQty() {
        return onHandQty;
    }

    public void setOnHandQty(String onHandQty) {
        this.onHandQty = onHandQty;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMonthOfStock() {
        return monthOfStock;
    }

    public void setMonthOfStock(String monthOfStock) {
        this.monthOfStock = monthOfStock;
    }

    public String getIlTransactionId() {
        return ilTransactionId;
    }

    public void setIlTransactionId(String ilTransactionId) {
        this.ilTransactionId = ilTransactionId;
    }
}
