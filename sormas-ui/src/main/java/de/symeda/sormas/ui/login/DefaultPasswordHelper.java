 package de.symeda.sormas.ui.login;

import com.vaadin.ui.UI;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.user.UserDto;

import java.util.List;

public class DefaultPasswordHelper {
    static List<UserDto> getUsersWithDefaultPassword() {
        return FacadeProvider.getUserFacade().getUsersWithDefaultPassword();
    }


    static LoginScreen.LoginListener getInterceptionLoginListener(LoginScreen.LoginListener originalLoginListener, UI vaadinUI) {
        return () -> {
            List<UserDto> usersWithDefaultPassword = DefaultPasswordHelper.getUsersWithDefaultPassword();
            if (usersWithDefaultPassword.isEmpty()) {
                originalLoginListener.loginSuccessful();
            } else {
                vaadinUI.addWindow(new DefaultPasswordScreen(originalLoginListener, usersWithDefaultPassword));
            }
        };
    }
}
