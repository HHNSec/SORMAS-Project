package de.symeda.sormas.ui.login;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.ui.utils.ButtonHelper;

public class DefaultPasswordOwnScreen extends Window {
    public DefaultPasswordOwnScreen(Runnable onContinue, UserDto currentUser) {
        setCaption("Change password");
        setWidth(600.0f, Unit.PIXELS);
        final VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        content.setWidthFull();

        Label introductionLabel = new Label("The use of the default password is not allowed. Click the following button to request a new password:");
        introductionLabel.setWidthFull();
        introductionLabel.setStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
        content.addComponent(introductionLabel);
        Label newPasswordLabel = new Label();
        content.addComponent(new Button("New password", (Button.ClickListener) clickEvent -> {
            newPasswordLabel.setValue("Your new password: " + FacadeProvider.getUserFacade().resetPassword(currentUser.getUuid()));
        }));
        content.addComponent(newPasswordLabel);
        Label onlyOnceLabel = new Label("Please note: This password will be shown only once!");
        onlyOnceLabel.setWidthFull();
        content.addComponent(onlyOnceLabel);
        content.addComponent(ButtonHelper.createButtonWithCaption(null, "Continue", (Button.ClickListener) clickEvent -> {
            close();
            onContinue.run();
        }, ValoTheme.BUTTON_PRIMARY));

        setModal(true);
        setResizable(false);
        setClosable(false);
        setContent(content);
        setDraggable(false);

    }
}
