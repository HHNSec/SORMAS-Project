package de.symeda.sormas.ui.login;

import com.vaadin.ui.UI;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.utils.DefaultPasswordHelper;

import java.util.List;

public class DefaultPasswordUIHelper {
    static List<UserDto> getUsersWithDefaultPassword() {
        return FacadeProvider.getUserFacade().getUsersWithDefaultPassword();
    }


    static LoginScreen.LoginListener getInterceptionLoginListener(LoginScreen.LoginListener originalLoginListener, UI vaadinUI) {
        return () -> {
            List<UserDto> usersWithDefaultPassword = DefaultPasswordUIHelper.getUsersWithDefaultPassword();
            if (usersWithDefaultPassword.isEmpty()) {
                originalLoginListener.loginSuccessful();
            } else {
                UserDto currentUser = FacadeProvider.getUserFacade().getCurrentUser();
                boolean currentUserUsesDefaultPassword = DefaultPasswordHelper.currentUserUsesDefaultPassword(usersWithDefaultPassword, currentUser);
                boolean otherUsersWithDefaultPassword = DefaultPasswordHelper.otherUsersWithDefaultPassword(usersWithDefaultPassword, currentUser);
                if (currentUserUsesDefaultPassword) {
                    vaadinUI.addWindow(new DefaultPasswordOwnScreen(otherUsersWithDefaultPassword ? () -> {
                        vaadinUI.addWindow(new DefaultPasswordOtherScreen(originalLoginListener::loginSuccessful, without(usersWithDefaultPassword, currentUser)));
                    } : originalLoginListener::loginSuccessful, currentUser));
                } else {
                    vaadinUI.addWindow(new DefaultPasswordOtherScreen(originalLoginListener::loginSuccessful, without(usersWithDefaultPassword, currentUser)));
                }
            }
        };
    }

    private static <T> List<T> without(List<T> list, T element) {
        list.remove(element);
        return list;
    }
}
