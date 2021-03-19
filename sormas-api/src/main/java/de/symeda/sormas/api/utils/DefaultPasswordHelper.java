package de.symeda.sormas.api.utils;

import de.symeda.sormas.api.user.UserDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultPasswordHelper {

    private static final Map<String, String> defaultUsersWithPassword = new HashMap<>();

    static {
        defaultUsersWithPassword.put("admin", "sadmin");
        defaultUsersWithPassword.put("SurvSup", "SurvSup");
        defaultUsersWithPassword.put("CaseSup", "CaseSup");
        defaultUsersWithPassword.put("ContSup", "ContSup");
        defaultUsersWithPassword.put("PoeSup", "PoeSup");
        defaultUsersWithPassword.put("LabOff", "LabOff");
        defaultUsersWithPassword.put("EveOff", "EveOff");
        defaultUsersWithPassword.put("NatUser", "NatUser");
        defaultUsersWithPassword.put("NatClin", "NatClin");
        defaultUsersWithPassword.put("SurvOff", "SurvOff");
        defaultUsersWithPassword.put("HospInf", "HospInf");
        defaultUsersWithPassword.put("PoeInf", "PoeInf");
    }

    public static boolean isDefaultUser(String username) {
        return defaultUsersWithPassword.containsKey(username);
    }

    public static Optional<String> getDefaultPassword(String username) {
        return Optional.ofNullable(defaultUsersWithPassword.get(username));
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

}
