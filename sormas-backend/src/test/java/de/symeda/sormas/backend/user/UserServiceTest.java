package de.symeda.sormas.backend.user;

import de.symeda.sormas.api.AuthProvider;
import de.symeda.sormas.api.utils.PasswordHelper;
import de.symeda.sormas.backend.AbstractBeanTest;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class UserServiceTest extends AbstractBeanTest {

    @Test
    public void getAllDefaultUsers() {
        AuthProvider authProvider = mock(AuthProvider.class);

        MockedStatic<AuthProvider> mockAuthProvider = mockStatic(AuthProvider.class);
        mockAuthProvider.when(AuthProvider::getProvider).thenReturn(authProvider);
        when(authProvider.isUsernameCaseSensitive()).thenReturn(true);

        User user = getUserService().getByUserName("admin");

        user.setSeed(PasswordHelper.createPass(16));
        user.setPassword(PasswordHelper.encodePassword("sadmin", user.getSeed()));
        getEntityManager().merge(user);

        List<User> result = getUserService().getAllDefaultUsers();
        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getUserName());

        Set<User> defaultUsers = UserTestHelper.generateDefaultUsers(false);
        Set<User> randomUsers = UserTestHelper.generateRandomUsers(10);
        Set<User> testUsers = new HashSet<>();
        testUsers.addAll(defaultUsers);
        testUsers.addAll(randomUsers);

        for (User u : testUsers) {
            getEntityManager().persist(u);
        }

        // Default users size + 1 because one default admin is created by the AbstractBeanTest
        assertEquals(defaultUsers.size() + 1, getUserService().getAllDefaultUsers().size());

    }
}