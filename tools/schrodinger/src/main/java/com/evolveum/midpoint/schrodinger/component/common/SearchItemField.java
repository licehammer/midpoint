package com.evolveum.midpoint.schrodinger.component.common;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import com.evolveum.midpoint.schrodinger.MidPoint;
import com.evolveum.midpoint.schrodinger.component.Component;
import com.evolveum.midpoint.schrodinger.util.Schrodinger;

public class SearchItemField<T> extends Component<T> {

    public SearchItemField(T parent, SelenideElement parentElement) {
        super(parent, parentElement);
    }

    public T inputValue(String input) {
        if (getParentElement() == null){
            return getParent();
        }
        SelenideElement inputField = getParentElement().parent().$x(".//input[@" + Schrodinger.DATA_S_ID + "='input']")
                .waitUntil(Condition.appears, MidPoint.TIMEOUT_DEFAULT_2_S);
        if(!input.equals(inputField.getValue())) {
            inputField.setValue(input);
        }
        return getParent();
    }

    public T inputRefOid(String oid) {
        if (getParentElement() == null){
            return getParent();
        }
        getParentElement().$x(".//a[@" + Schrodinger.DATA_S_ID + "='editReferenceButton']")
                .waitUntil(Condition.appears, MidPoint.TIMEOUT_DEFAULT_2_S).click();
        getParentElement().parent().$x(".//a[@" + Schrodinger.DATA_S_ID + "='editReferenceButton']")
                .waitUntil(Condition.appears, MidPoint.TIMEOUT_DEFAULT_2_S);
        SelenideElement inputField = getParentElement().parent().$x(".//input[@" + Schrodinger.DATA_S_ID + "='oid']")
                .waitUntil(Condition.appears, MidPoint.TIMEOUT_DEFAULT_2_S);
        if(!oid.equals(inputField.getValue())) {
            inputField.setValue(oid);
        }
        SelenideElement confirmButton = getParentElement().$x(".//a[@" + Schrodinger.DATA_S_ID + "='confirmButton']");
        confirmButton.waitUntil(Condition.appears, MidPoint.TIMEOUT_DEFAULT_2_S).click();
        confirmButton.waitUntil(Condition.hidden, MidPoint.TIMEOUT_DEFAULT_2_S);
        return getParent();
    }
}
