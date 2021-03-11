package de.symeda.sormas.ui.login;

import com.vaadin.ui.*;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.user.UserDto;

import java.util.List;

public class DefaultPasswordScreen extends Window {

    private final LoginScreen.LoginListener loginListener;
    private final UserDto currentUser;
    private final List<UserDto> usersWithDefaultPassword;

    public DefaultPasswordScreen(LoginScreen.LoginListener loginListener, List<UserDto> usersWithDefaultPassword) {
        this.loginListener = loginListener;
        currentUser = FacadeProvider.getUserFacade().getCurrentUser();
        this.usersWithDefaultPassword = usersWithDefaultPassword;
        buildUI();
    }


    public void buildUI() {
        setCaption("Change password");
        setWidth(600.0f, Unit.PIXELS);
        final FormLayout content = new FormLayout();
        content.setMargin(true);

        boolean currentUserUsesDefaultPassword = usersWithDefaultPassword.contains(currentUser);
        boolean otherUsersWithDefaultPassword = (usersWithDefaultPassword.contains(currentUser) && usersWithDefaultPassword.size() > 1)
                || (!usersWithDefaultPassword.contains(currentUser) && usersWithDefaultPassword.size() > 0);


        VerticalLayout otherUserPasswordLayout = new VerticalLayout();

        if (currentUserUsesDefaultPassword) {
            otherUserPasswordLayout.setVisible(false);
            VerticalLayout ownPasswordLayout = new VerticalLayout();
            content.addComponent(ownPasswordLayout);
            ownPasswordLayout.addComponent(new Label("The use of the default password is not allowed. Click the following button to request a new password:"));
            Label newPasswordLabel = new Label();
            ownPasswordLayout.addComponent(new Button("New password", (Button.ClickListener) clickEvent -> {
                newPasswordLabel.setValue("Your new password: " + FacadeProvider.getUserFacade().resetPassword(currentUser.getUuid()));
            }));
            ownPasswordLayout.addComponent(newPasswordLabel);
            ownPasswordLayout.addComponent(new Label("Please note: This password will be shown only once!"));
            ownPasswordLayout.addComponent(new Button("Continue", (Button.ClickListener) clickEvent -> {
                if (otherUsersWithDefaultPassword) {
                    ownPasswordLayout.setVisible(false);
                    otherUserPasswordLayout.setVisible(true);
                } else {
                    finish();
                }
            }));

        }



        if (otherUsersWithDefaultPassword) {
            content.addComponent(otherUserPasswordLayout);
            otherUserPasswordLayout.addComponent(new Label("You have other users using the default password."));
            otherUserPasswordLayout.addComponent(new Label("The use of the default password is not allowed. Click the buttons below to request a new password for them:"));

            usersWithDefaultPassword.remove(currentUser);

            Grid<UserDto> userGrid = new Grid<>();
            userGrid.setItems(usersWithDefaultPassword);
            userGrid.addComponentColumn(user -> new Label(user.getUserName())).setCaption("UserName");
            userGrid.addComponentColumn(user -> {
                HorizontalLayout layout = new HorizontalLayout();
                Label newPasswordLabel = new Label();
                layout.addComponent(new Button("New password", (Button.ClickListener) clickEvent -> {
                    newPasswordLabel.setValue("Your new password: " + FacadeProvider.getUserFacade().resetPassword(user.getUuid()));
                }));
                layout.addComponent(newPasswordLabel);
                return layout;
            });
            otherUserPasswordLayout.addComponent(userGrid);
            otherUserPasswordLayout.addComponent(new Button("Continue", (Button.ClickListener) clickEvent -> {
                finish();
            }));

        }



        setModal(true);
        setResizable(false);
        setClosable(false);
        setContent(content);
        setDraggable(false);
    }

    private void finish() {
        close();
        loginListener.loginSuccessful();
    }

}
