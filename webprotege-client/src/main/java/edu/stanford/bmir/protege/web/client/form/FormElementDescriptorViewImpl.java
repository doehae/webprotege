package edu.stanford.bmir.protege.web.client.form;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import edu.stanford.bmir.protege.web.client.form.FormElementDescriptorPresenter.RemoveFormElementDescriptorHandler;
import edu.stanford.bmir.protege.web.client.primitive.PrimitiveDataEditor;
import edu.stanford.bmir.protege.web.client.ui.Counter;
import edu.stanford.bmir.protege.web.shared.entity.OWLPropertyData;
import edu.stanford.bmir.protege.web.shared.form.field.ElementRun;
import edu.stanford.bmir.protege.web.shared.form.field.Optionality;
import edu.stanford.bmir.protege.web.shared.form.field.Repeatability;
import edu.stanford.bmir.protege.web.shared.lang.LanguageMap;
import org.semanticweb.owlapi.model.OWLProperty;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-16
 */
public class FormElementDescriptorViewImpl extends Composite implements FormElementDescriptorView {

    @Nonnull
    private FieldTypeChangedHandler fieldTypeChangedHander = () -> {};

    interface FormElementDescriptorEditorViewImplUiBinder extends UiBinder<HTMLPanel, FormElementDescriptorViewImpl> {

    }

    private static FormElementDescriptorEditorViewImplUiBinder ourUiBinder = GWT.create(
            FormElementDescriptorEditorViewImplUiBinder.class);

    @UiField
    SimplePanel fieldEditorContainer;

    @UiField
    TextBox elementIdField;

    @UiField
    RadioButton optionalRadio;

    @UiField
    RadioButton requiredRadio;

    @UiField(provided = true)
    LanguageMapEditor labelEditor;

    @UiField(provided = true)
    LanguageMapEditor helpEditor;

    @UiField
    ListBox typesComboBox;

    @UiField
    RadioButton nonRepeatableRadio;

    @UiField
    RadioButton repeatableVerticallyRadio;

    @UiField
    RadioButton repeatableHorizontallyRadio;

    @UiField
    Label numberField;

    @UiField
    RadioButton elementRunStartRadio;

    @UiField
    RadioButton elementRunContinueRadio;

    @UiField(provided = true)
    PrimitiveDataEditor propertyBindingField;

    @UiField(provided = true)
    protected static Counter counter = new Counter();

    @UiField
    Button removeButton;

    RemoveFormElementDescriptorHandler removeHandler = () -> {};

    @Inject
    public FormElementDescriptorViewImpl(LanguageMapEditor labelEditor,
                                         LanguageMapEditor helpEditor,
                                         PrimitiveDataEditor propertyBindingField) {
        counter.increment();
        this.labelEditor = labelEditor;
        this.helpEditor = helpEditor;
        this.propertyBindingField = propertyBindingField;
        initWidget(ourUiBinder.createAndBindUi(this));
        typesComboBox.addChangeHandler(event -> fieldTypeChangedHander.handleFieldTypeChanged());
        removeButton.addClickHandler(event -> removeHandler.handleRemoveFormElementDescriptor());

    }

    @Nonnull
    @Override
    public AcceptsOneWidget getFieldEditorContainer() {
        return fieldEditorContainer;
    }

    @Override
    public void setNumber(int number) {
        numberField.setText(Integer.toString(number));
    }

    @Override
    public void setFormElementId(@Nonnull String id) {
        elementIdField.setText(id);
    }

    @Nonnull
    @Override
    public String getFormElementId() {
        return elementIdField.getText();
    }


    @Override
    public void clearOwlProperty() {
        propertyBindingField.clearValue();
    }

    @Override
    public void setHelp(@Nonnull LanguageMap help) {
        helpEditor.setValue(help);
    }

    @Nonnull
    @Override
    public LanguageMap getHelp() {
        return helpEditor.getValue().orElse(LanguageMap.empty());
    }

    @Override
    public void setLabel(@Nonnull LanguageMap label) {
        labelEditor.setValue(label);
    }

    @Nonnull
    @Override
    public LanguageMap getLabel() {
        return labelEditor.getValue().orElse(LanguageMap.empty());
    }

    @Override
    public void setElementRun(ElementRun elementRun) {
        if(elementRun == ElementRun.CONTINUE) {
            elementRunContinueRadio.setValue(true);
        }
        else {
            elementRunStartRadio.setValue(true);
        }
    }

    @Override
    public ElementRun getElementRun() {
        if(elementRunContinueRadio.getValue()) {
            return ElementRun.CONTINUE;
        }
        else {
            return ElementRun.START;
        }
    }

    @Override
    public void setOptionality(@Nonnull Optionality optionality) {
        GWT.log("[FormElementDescriptorViewImpl] setOptionality");
        if(optionality == Optionality.OPTIONAL) {
            optionalRadio.setValue(true);
        }
        else {
            requiredRadio.setValue(true);
        }
    }

    @Nonnull
    @Override
    public Optionality getOptionality() {
        return optionalRadio.getValue() ? Optionality.OPTIONAL : Optionality.REQUIRED;
    }

    @Override
    public void setOwlProperty(@Nonnull OWLPropertyData property) {
        propertyBindingField.setValue(property);
    }

    @Nonnull
    @Override
    public Optional<OWLPropertyData> getOwlProperty() {
        return propertyBindingField.getValue()
                .filter(property -> property instanceof OWLPropertyData)
                .map(property -> (OWLPropertyData) property);
    }

    @Override
    public void setRepeatability(@Nonnull Repeatability repeatability) {
        if(repeatability == Repeatability.REPEATABLE_VERTICALLY) {
            repeatableVerticallyRadio.setValue(true);
        }
        else if(repeatability == Repeatability.REPEATABLE_HORIZONTALLY) {
            repeatableHorizontallyRadio.setValue(true);
        }
        else {
            nonRepeatableRadio.setValue(true);
        }
    }

    @Nonnull
    @Override
    public Repeatability getRepeatability() {
        if(repeatableVerticallyRadio.getValue()) {
            return Repeatability.REPEATABLE_VERTICALLY;
        }
        else if(repeatableHorizontallyRadio.getValue()) {
            return Repeatability.REPEATABLE_HORIZONTALLY;
        }
        else {
            return Repeatability.NON_REPEATABLE;
        }
    }

    @Nonnull
    @Override
    public String getFieldType() {
        return typesComboBox.getSelectedValue();
    }

    @Override
    public void setFieldType(@Nonnull String fieldType) {
        for(int i = 0; i < typesComboBox.getItemCount(); i++) {
            if(typesComboBox.getValue(i).equals(fieldType)) {
                typesComboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    @Override
    public void addAvailableFieldType(@Nonnull String value,
                                      @Nonnull String label) {
        typesComboBox.addItem(label, value);
    }

    @Override
    public void setRemoveFormElementDescriptorHandler(RemoveFormElementDescriptorHandler handler) {
        this.removeHandler = checkNotNull(handler);
    }

    @Override
    public void setFieldTypeChangedHandler(FieldTypeChangedHandler handler) {
        this.fieldTypeChangedHander = checkNotNull(handler);
    }
}