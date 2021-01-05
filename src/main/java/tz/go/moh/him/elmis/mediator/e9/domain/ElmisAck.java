package tz.go.moh.him.elmis.mediator.e9.domain;

import com.google.gson.annotations.SerializedName;

public class ElmisAck {
    int imported;

    int updated;

    int ignored;

    String status;

    @SerializedName(value = "iL_TransactionIDNumber")
    String iLTransactionIDNumber;

    @SerializedName(value = "il_TransactionIDNumber")
    String ilTransactionIDNumber;

    public int getImported() {
        return imported;
    }

    public int getUpdated() {
        return updated;
    }

    public int getIgnored() {
        return ignored;
    }

    public String getStatus() {
        return status;
    }

    public String getiLTransactionIDNumber() {
        return iLTransactionIDNumber;
    }

    public String getIlTransactionIDNumber() {
        return ilTransactionIDNumber;
    }
}
