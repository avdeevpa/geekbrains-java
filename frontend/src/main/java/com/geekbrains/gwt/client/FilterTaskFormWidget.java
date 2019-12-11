package com.geekbrains.gwt.client;

import com.geekbrains.gwt.common.dtos.UserDTO;
import com.geekbrains.gwt.common.entities.Task;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilterTaskFormWidget extends Composite {
    private UserClient userClient;

    @UiField
    FormPanel form;

    @UiField
    TextBox idText;

    @UiField
    TextBox captionText;

    @UiField(provided = true)
    ValueListBox<String> ownerList = new ValueListBox<String>(new Renderer<String>() {
        @Override
        public String render(String object) {
            return object;
        }

        @Override
        public void render(String object, Appendable appendable)
                throws IOException {
            if (object != null) {
                render(object);
            }
        }
    });

    @UiField(provided = true)
    ValueListBox<String> assignedList = new ValueListBox<String>(new Renderer<String>() {
        @Override
        public String render(String object) {
            return object;
        }

        @Override
        public void render(String object, Appendable appendable)
                throws IOException {
            if (object != null) {
                render(object);
            }
        }
    });

    @UiField(provided = true)
    ValueListBox<String> statusList = new ValueListBox<String>(new Renderer<String>() {
        @Override
        public String render(String object) {
            return object;
        }

        @Override
        public void render(String object, Appendable appendable)
        throws IOException {
            if (object != null) {
                render(object);
            }
        }
    });

    @UiField
    TextBox descriprionText;

    private TaskTableWidget taskTableWidget;

    @UiTemplate("FilterTaskForm.ui.xml")
    interface AddItemFormBinder extends UiBinder<Widget, FilterTaskFormWidget> {
    }

    private static FilterTaskFormWidget.AddItemFormBinder uiBinder = GWT.create(FilterTaskFormWidget.AddItemFormBinder.class);

    public FilterTaskFormWidget(TaskTableWidget itemsTableWidget) {
        this.initWidget(uiBinder.createAndBindUi(this));
        this.form.setAction(Defaults.getServiceRoot().concat("tasks"));

        List<String> statusVals = Stream.of(Task.Status.values())
                .map(Task.Status::name)
                .collect(Collectors.toList());
        this.statusList.setValue("");
        this.statusList.setAcceptableValues(statusVals);

        userClient = GWT.create(UserClient.class);

        userClient.getInitiators(new MethodCallback<List<UserDTO>>() {
            @Override
            public void onFailure(Method method, Throwable throwable) {
                GWT.log(throwable.toString());
                GWT.log(throwable.getMessage());
                Window.alert("Невозможно получить список Initiators: Сервер не отвечает");
            }

            @Override
            public void onSuccess(Method method, List<UserDTO> i) {
                GWT.log("Received " + i.size() + " Initiators");
                ownerList.setValue("");
                List<String> ownerVals = i.stream()
                        .map(user -> user.getUsername())
                        .collect(Collectors.toList());
                ownerList.setAcceptableValues(ownerVals);
            }
        });

        userClient.getExecutors(new MethodCallback<List<UserDTO>>() {
            @Override
            public void onFailure(Method method, Throwable throwable) {
                GWT.log(throwable.toString());
                GWT.log(throwable.getMessage());
                Window.alert("Невозможно получить список Initiators: Сервер не отвечает");
            }

            @Override
            public void onSuccess(Method method, List<UserDTO> i) {
                GWT.log("Received " + i.size() + " Initiators");
                assignedList.setValue("");
                List<String> ownerVals = i.stream()
                        .map(user -> user.getUsername())
                        .collect(Collectors.toList());
                assignedList.setAcceptableValues(ownerVals);
            }
        });

        this.taskTableWidget = itemsTableWidget;
    }

    @UiHandler("btnSubmit")
    public void submitClick(ClickEvent event) {
        taskTableWidget.update(
                this.idText.getValue(),
                this.captionText.getValue(),
                this.ownerList.getValue(),
                this.assignedList.getValue(),
                this.statusList.getValue(),
                this.descriprionText.getValue()
        );
    }

    @UiHandler("btnClear")
    public void clearClick(ClickEvent event) {
        this.idText.setValue("");
        this.captionText.setValue("");
        this.ownerList.setValue("");
        this.assignedList.setValue("");
        this.statusList.setValue("");
        this.descriprionText.setValue("");
    }
}