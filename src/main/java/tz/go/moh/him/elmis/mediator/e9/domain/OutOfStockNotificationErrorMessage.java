package tz.go.moh.him.elmis.mediator.e9.domain;

import tz.go.moh.him.mediator.core.domain.ErrorMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutOfStockNotificationErrorMessage {
    private List<ErrorMessage> errorMessages = new ArrayList<>();
    private Map<String, List<ErrorMessage>> itemsErrorMessages = new HashMap<>();

    public List<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<ErrorMessage> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public Map<String, List<ErrorMessage>> getItemsErrorMessages() {
        return itemsErrorMessages;
    }

    public void setItemsErrorMessages(Map<String, List<ErrorMessage>> itemsErrorMessages) {
        this.itemsErrorMessages = itemsErrorMessages;
    }
}
