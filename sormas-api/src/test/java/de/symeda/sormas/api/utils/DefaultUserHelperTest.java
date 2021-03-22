package de.symeda.sormas.api.utils;

import de.symeda.sormas.api.user.UserDto;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultUserHelperTest {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASS = "sadmin";
    private static final String DEFAUTL_SURV_SUP_USER_PASS = "SurvSup";
    private static final String DEFAUTL_CASE_SUP_USER_PASS = "CaseSup";
    private static final String DEFAUTL_CONT_SUP_USER_PASS = "ContSup";
    private static final String DEFAUTL_POE_SUP_USER_PASS = "PoeSup";
    private static final String DEFAUTL_LAB_OFF_USER_PASS = "LabOff";
    private static final String DEFAUTL_EVE_OFF_USER_PASS = "EveOff";
    private static final String DEFAUTL_NAT_USER_USER_PASS = "NatUser";
    private static final String DEFAUTL_NAT_CLIN_USER_PASS = "NatClin";
    private static final String DEFAUTL_SURV_OFF_USER_PASS = "SurvOff";
    private static final String DEFAUTL_HOSP_INF_USER_PASS = "HospInf";
    private static final String DEFAUTL_POE_INF_USER_PASS = "PoeInf";

    @Test
    public void isDefaultUser() {
        assertTrue(DefaultUserHelper.isDefaultUser(DEFAULT_ADMIN_USERNAME));
        assertTrue(DefaultUserHelper.isDefaultUser(DEFAUTL_SURV_SUP_USER_PASS));
        assertTrue(DefaultUserHelper.isDefaultUser(DEFAUTL_CASE_SUP_USER_PASS));
        assertTrue(DefaultUserHelper.isDefaultUser(DEFAUTL_CONT_SUP_USER_PASS));
        assertTrue(DefaultUserHelper.isDefaultUser(DEFAUTL_POE_SUP_USER_PASS));
        assertTrue(DefaultUserHelper.isDefaultUser(DEFAUTL_LAB_OFF_USER_PASS));
        assertTrue(DefaultUserHelper.isDefaultUser(DEFAUTL_EVE_OFF_USER_PASS));
        assertTrue(DefaultUserHelper.isDefaultUser(DEFAUTL_NAT_USER_USER_PASS));
        assertTrue(DefaultUserHelper.isDefaultUser(DEFAUTL_NAT_CLIN_USER_PASS));
        assertTrue(DefaultUserHelper.isDefaultUser(DEFAUTL_SURV_OFF_USER_PASS));
        assertTrue(DefaultUserHelper.isDefaultUser(DEFAUTL_HOSP_INF_USER_PASS));
        assertTrue(DefaultUserHelper.isDefaultUser(DEFAUTL_POE_INF_USER_PASS));
    }

    @Test
    public void getDefaultPassword() {
        assertEquals(DEFAULT_ADMIN_PASS, DefaultUserHelper.getDefaultPassword(DEFAULT_ADMIN_USERNAME).orElse(""));
        assertEquals(DEFAUTL_SURV_SUP_USER_PASS, DefaultUserHelper.getDefaultPassword(DEFAUTL_SURV_SUP_USER_PASS).orElse(""));
        assertEquals(DEFAUTL_CASE_SUP_USER_PASS, DefaultUserHelper.getDefaultPassword(DEFAUTL_CASE_SUP_USER_PASS).orElse(""));
        assertEquals(DEFAUTL_CONT_SUP_USER_PASS, DefaultUserHelper.getDefaultPassword(DEFAUTL_CONT_SUP_USER_PASS).orElse(""));
        assertEquals(DEFAUTL_POE_SUP_USER_PASS, DefaultUserHelper.getDefaultPassword(DEFAUTL_POE_SUP_USER_PASS).orElse(""));
        assertEquals(DEFAUTL_LAB_OFF_USER_PASS, DefaultUserHelper.getDefaultPassword(DEFAUTL_LAB_OFF_USER_PASS).orElse(""));
        assertEquals(DEFAUTL_EVE_OFF_USER_PASS, DefaultUserHelper.getDefaultPassword(DEFAUTL_EVE_OFF_USER_PASS).orElse(""));
        assertEquals(DEFAUTL_NAT_USER_USER_PASS, DefaultUserHelper.getDefaultPassword(DEFAUTL_NAT_USER_USER_PASS).orElse(""));
        assertEquals(DEFAUTL_NAT_CLIN_USER_PASS, DefaultUserHelper.getDefaultPassword(DEFAUTL_NAT_CLIN_USER_PASS).orElse(""));
        assertEquals(DEFAUTL_SURV_OFF_USER_PASS, DefaultUserHelper.getDefaultPassword(DEFAUTL_SURV_OFF_USER_PASS).orElse(""));
        assertEquals(DEFAUTL_HOSP_INF_USER_PASS, DefaultUserHelper.getDefaultPassword(DEFAUTL_HOSP_INF_USER_PASS).orElse(""));
        assertEquals(DEFAUTL_POE_INF_USER_PASS, DefaultUserHelper.getDefaultPassword(DEFAUTL_POE_INF_USER_PASS).orElse(""));
    }

    @Test
    public void usesDefaultPassword() {
        String seed = UUID.randomUUID().toString();
        String randomPass = UUID.randomUUID().toString();
        assertTrue(DefaultUserHelper.usesDefaultPassword(DEFAULT_ADMIN_USERNAME, PasswordHelper.encodePassword(DEFAULT_ADMIN_PASS, seed), seed));
        assertFalse(DefaultUserHelper.usesDefaultPassword(DEFAULT_ADMIN_USERNAME, PasswordHelper.encodePassword(randomPass, seed), seed));

        assertTrue(DefaultUserHelper.usesDefaultPassword(DEFAUTL_SURV_SUP_USER_PASS, PasswordHelper.encodePassword(DEFAUTL_SURV_SUP_USER_PASS, seed), seed));
        assertFalse(DefaultUserHelper.usesDefaultPassword(DEFAUTL_SURV_SUP_USER_PASS, PasswordHelper.encodePassword(randomPass, seed), seed));

        assertTrue(DefaultUserHelper.usesDefaultPassword(DEFAUTL_CASE_SUP_USER_PASS, PasswordHelper.encodePassword(DEFAUTL_CASE_SUP_USER_PASS, seed), seed));
        assertFalse(DefaultUserHelper.usesDefaultPassword(DEFAUTL_CASE_SUP_USER_PASS, PasswordHelper.encodePassword(randomPass, seed), seed));

        assertTrue(DefaultUserHelper.usesDefaultPassword(DEFAUTL_CONT_SUP_USER_PASS, PasswordHelper.encodePassword(DEFAUTL_CONT_SUP_USER_PASS, seed), seed));
        assertFalse(DefaultUserHelper.usesDefaultPassword(DEFAUTL_CONT_SUP_USER_PASS, PasswordHelper.encodePassword(randomPass, seed), seed));

        assertTrue(DefaultUserHelper.usesDefaultPassword(DEFAUTL_POE_SUP_USER_PASS, PasswordHelper.encodePassword(DEFAUTL_POE_SUP_USER_PASS, seed), seed));
        assertFalse(DefaultUserHelper.usesDefaultPassword(DEFAUTL_POE_SUP_USER_PASS, PasswordHelper.encodePassword(randomPass, seed), seed));

        assertTrue(DefaultUserHelper.usesDefaultPassword(DEFAUTL_LAB_OFF_USER_PASS, PasswordHelper.encodePassword(DEFAUTL_LAB_OFF_USER_PASS, seed), seed));
        assertFalse(DefaultUserHelper.usesDefaultPassword(DEFAUTL_LAB_OFF_USER_PASS, PasswordHelper.encodePassword(randomPass, seed), seed));

        assertTrue(DefaultUserHelper.usesDefaultPassword(DEFAUTL_EVE_OFF_USER_PASS, PasswordHelper.encodePassword(DEFAUTL_EVE_OFF_USER_PASS, seed), seed));
        assertFalse(DefaultUserHelper.usesDefaultPassword(DEFAUTL_EVE_OFF_USER_PASS, PasswordHelper.encodePassword(randomPass, seed), seed));

        assertTrue(DefaultUserHelper.usesDefaultPassword(DEFAUTL_NAT_USER_USER_PASS, PasswordHelper.encodePassword(DEFAUTL_NAT_USER_USER_PASS, seed), seed));
        assertFalse(DefaultUserHelper.usesDefaultPassword(DEFAUTL_NAT_USER_USER_PASS, PasswordHelper.encodePassword(randomPass, seed), seed));

        assertTrue(DefaultUserHelper.usesDefaultPassword(DEFAUTL_NAT_CLIN_USER_PASS, PasswordHelper.encodePassword(DEFAUTL_NAT_CLIN_USER_PASS, seed), seed));
        assertFalse(DefaultUserHelper.usesDefaultPassword(DEFAUTL_NAT_CLIN_USER_PASS, PasswordHelper.encodePassword(randomPass, seed), seed));

        assertTrue(DefaultUserHelper.usesDefaultPassword(DEFAUTL_SURV_OFF_USER_PASS, PasswordHelper.encodePassword(DEFAUTL_SURV_OFF_USER_PASS, seed), seed));
        assertFalse(DefaultUserHelper.usesDefaultPassword(DEFAUTL_SURV_OFF_USER_PASS, PasswordHelper.encodePassword(randomPass, seed), seed));

        assertTrue(DefaultUserHelper.usesDefaultPassword(DEFAUTL_HOSP_INF_USER_PASS, PasswordHelper.encodePassword(DEFAUTL_HOSP_INF_USER_PASS, seed), seed));
        assertFalse(DefaultUserHelper.usesDefaultPassword(DEFAUTL_HOSP_INF_USER_PASS, PasswordHelper.encodePassword(randomPass, seed), seed));

        assertTrue(DefaultUserHelper.usesDefaultPassword(DEFAUTL_POE_INF_USER_PASS, PasswordHelper.encodePassword(DEFAUTL_POE_INF_USER_PASS, seed), seed));
        assertFalse(DefaultUserHelper.usesDefaultPassword(DEFAUTL_POE_INF_USER_PASS, PasswordHelper.encodePassword(randomPass, seed), seed));
    }

    @Test
    public void currentUserUsesDefaultPassword() {
        List<UserDto> defaultDtos = new ArrayList<>();
        UserDto admin = new UserDto();
        admin.setUserName("admin");
        defaultDtos.add(admin);
        UserDto randomUser = new UserDto();
        randomUser.setUserName(UUID.randomUUID().toString());

        assertTrue(DefaultUserHelper.currentUserUsesDefaultPassword(defaultDtos, admin));
        assertFalse(DefaultUserHelper.currentUserUsesDefaultPassword(defaultDtos, randomUser));
        defaultDtos.remove(admin);
        assertFalse(DefaultUserHelper.currentUserUsesDefaultPassword(defaultDtos, admin));
    }

    @Test
    public void otherUsersWithDefaultPassword() {
        List<UserDto> defaultDtos = new ArrayList<>();
        UserDto admin = new UserDto();
        admin.setUserName("admin");
        defaultDtos.add(admin);
        UserDto randomUser = new UserDto();
        randomUser.setUserName(UUID.randomUUID().toString());

        assertTrue(DefaultUserHelper.otherUsersWithDefaultPassword(defaultDtos, randomUser));
        assertFalse(DefaultUserHelper.otherUsersWithDefaultPassword(defaultDtos,admin));
        defaultDtos.add(randomUser);
        assertTrue(DefaultUserHelper.otherUsersWithDefaultPassword(defaultDtos,admin));
        assertTrue(DefaultUserHelper.otherUsersWithDefaultPassword(defaultDtos, randomUser));
        assertFalse(DefaultUserHelper.otherUsersWithDefaultPassword(new ArrayList<>(), admin));
    }

    @Test
    public void getDefaultUserNames(){
        assertEquals(12,DefaultUserHelper.getDefaultUserNames().size());
        Set<String> defaultNames = DefaultUserHelper.getDefaultUserNames();
        assertTrue(defaultNames.contains(DEFAULT_ADMIN_USERNAME));
        assertTrue(defaultNames.contains(DEFAUTL_SURV_SUP_USER_PASS));
        assertTrue(defaultNames.contains(DEFAUTL_CASE_SUP_USER_PASS));
        assertTrue(defaultNames.contains(DEFAUTL_CONT_SUP_USER_PASS));
        assertTrue(defaultNames.contains(DEFAUTL_POE_SUP_USER_PASS));
        assertTrue(defaultNames.contains(DEFAUTL_LAB_OFF_USER_PASS));
        assertTrue(defaultNames.contains(DEFAUTL_EVE_OFF_USER_PASS));
        assertTrue(defaultNames.contains(DEFAUTL_NAT_USER_USER_PASS));
        assertTrue(defaultNames.contains(DEFAUTL_NAT_CLIN_USER_PASS));
        assertTrue(defaultNames.contains(DEFAUTL_SURV_OFF_USER_PASS));
        assertTrue(defaultNames.contains(DEFAUTL_HOSP_INF_USER_PASS));
        assertTrue(defaultNames.contains(DEFAUTL_POE_INF_USER_PASS));
    }
}