package de.symeda.sormas.api.utils;

import de.symeda.sormas.api.user.UserDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DefaultUserHelper {

    // default usernames and passwords
    public static final DataHelper.Pair<String, String> ADMIN_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("admin", "sadmin");
    public static final DataHelper.Pair<String, String> SURV_SUP_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("SurvSup", "SurvSup");
    public static final DataHelper.Pair<String, String> CASE_SUP_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("CaseSup", "CaseSup");
    public static final DataHelper.Pair<String, String> CONT_SUP_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("ContSup", "ContSup");
    public static final DataHelper.Pair<String, String> POE_SUP_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("PoeSup", "PoeSup");
    public static final DataHelper.Pair<String, String> LAB_OFF_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("LabOff", "LabOff");
    public static final DataHelper.Pair<String, String> EVE_OFF_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("EveOff", "EveOff");
    public static final DataHelper.Pair<String, String> NAT_USER_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("NatUser", "NatUser");
    public static final DataHelper.Pair<String, String> NAT_CLIN_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("NatClin", "NatClin");
    public static final DataHelper.Pair<String, String> SURV_OFF_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("SurvOff", "SurvOff");
    public static final DataHelper.Pair<String, String> HOSP_INF_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("HospInf", "HospInf");
    public static final DataHelper.Pair<String, String> POE_INF_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("PoeInf", "PoeInf");



    private static final Map<String, String> defaultUsersWithPassword = new HashMap<>();

    static {
        addPairToUsernameAndPasswordMap(
                ADMIN_USERNAME_AND_PASSWORD,
                SURV_SUP_USERNAME_AND_PASSWORD,
                CASE_SUP_USERNAME_AND_PASSWORD,
                CONT_SUP_USERNAME_AND_PASSWORD,
                POE_SUP_USERNAME_AND_PASSWORD,
                LAB_OFF_USERNAME_AND_PASSWORD,
                EVE_OFF_USERNAME_AND_PASSWORD,
                NAT_USER_USERNAME_AND_PASSWORD,
                NAT_CLIN_USERNAME_AND_PASSWORD,
                SURV_OFF_USERNAME_AND_PASSWORD,
                HOSP_INF_USERNAME_AND_PASSWORD,
                POE_INF_USERNAME_AND_PASSWORD
        );
    }

    public static boolean isDefaultUser(String username) {
        return defaultUsersWithPassword.containsKey(username);
    }

    public static Optional<String> getDefaultPassword(String username) {
        return Optional.ofNullable(defaultUsersWithPassword.get(username));
    }

    public static Set<String> getDefaultUserNames() {
        return defaultUsersWithPassword.keySet();
    }

    public static boolean usesDefaultPassword(String username, String passwordHash, String seed) {
        return getDefaultPassword(username)
                .filter(s -> passwordHash.equals(PasswordHelper.encodePassword(
                s, seed))).isPresent();
    }

    public static boolean currentUserUsesDefaultPassword(List<UserDto> allUsersWithDefaultPassword, UserDto currentUser) {
        return allUsersWithDefaultPassword.contains(currentUser);
    }

    public static boolean otherUsersWithDefaultPassword(List<UserDto> allUsersWithDefaultPassword, UserDto currentUser) {
        return (allUsersWithDefaultPassword.contains(currentUser) && allUsersWithDefaultPassword.size() > 1)
                || (!allUsersWithDefaultPassword.contains(currentUser) && allUsersWithDefaultPassword.size() > 0);
    }



    // internal helpers
    @SafeVarargs
    private static void addPairToUsernameAndPasswordMap(DataHelper.Pair<String, String> ... usernameAndPasswordPairs ) {
        for (DataHelper.Pair<String, String> usernameAndPasswordPair : usernameAndPasswordPairs) {
            defaultUsersWithPassword.put(usernameAndPasswordPair.getElement0(), usernameAndPasswordPair.getElement1());
        }
    }

}
