package de.symeda.sormas.ui.login;

import com.vaadin.ui.*;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.ui.utils.ButtonHelper;

import java.util.List;

public class DefaultPasswordOtherScreen extends Window {

    public DefaultPasswordOtherScreen(Runnable onContinue, List<UserDto> otherUsersWithDefaultPassword) {
        setCaption("Default passwords");
        setWidth(600.0f, Unit.PIXELS);
        final VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        content.setWidthFull();
        Label otherUsersWithDefaultPasswordLabel = new Label("You have other users using the default password.");
        otherUsersWithDefaultPasswordLabel.setWidthFull();
        content.addComponent(otherUsersWithDefaultPasswordLabel);
        Label defaultPasswordNotAllowedLabel = new Label("The use of the default password is not allowed. Click the buttons below to request a new password for them:");
        defaultPasswordNotAllowedLabel.setWidthFull();
        content.addComponent(defaultPasswordNotAllowedLabel);

        // generate all button
        // copy paste sicherstellen
        Grid<UserDto> userGrid = new Grid<>();
        userGrid.setWidthFull();
        userGrid.setHeightFull();
        userGrid.setItems(otherUsersWithDefaultPassword);
        userGrid.addComponentColumn(user -> new Label(user.getUserName())).setCaption("UserName");
        userGrid.addComponentColumn(user -> {
            HorizontalLayout layout = new HorizontalLayout();
            Label newPasswordLabel = new Label();
            Button generatePasswordButton = new Button("Generate", (Button.ClickListener) clickEvent -> {
                clickEvent.getButton().setVisible(false);
                // copy to clipboard
                newPasswordLabel.setValue("new password: " + FacadeProvider.getUserFacade().resetPassword(user.getUuid()));
            });
            layout.addComponent(generatePasswordButton);
            layout.addComponent(newPasswordLabel);
            return layout;
        });
        content.addComponent(userGrid);
        content.addComponent(ButtonHelper.createButtonWithCaption(null, "Continue", (Button.ClickListener) clickEvent -> {
            close();
            onContinue.run();
        }));

        setModal(true);
        setResizable(false);
        setClosable(false);
        setContent(content);
        setDraggable(false);

    }
}
