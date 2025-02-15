package de.symeda.sormas.backend.user;

import static de.symeda.sormas.api.user.UserRole.ADMIN;
import static de.symeda.sormas.api.user.UserRole.CASE_OFFICER;
import static de.symeda.sormas.api.user.UserRole.CASE_SUPERVISOR;
import static de.symeda.sormas.api.user.UserRole.COMMUNITY_INFORMANT;
import static de.symeda.sormas.api.user.UserRole.COMMUNITY_OFFICER;
import static de.symeda.sormas.api.user.UserRole.CONTACT_OFFICER;
import static de.symeda.sormas.api.user.UserRole.CONTACT_SUPERVISOR;
import static de.symeda.sormas.api.user.UserRole.DISTRICT_OBSERVER;
import static de.symeda.sormas.api.user.UserRole.EVENT_OFFICER;
import static de.symeda.sormas.api.user.UserRole.HOSPITAL_INFORMANT;
import static de.symeda.sormas.api.user.UserRole.NATIONAL_OBSERVER;
import static de.symeda.sormas.api.user.UserRole.NATIONAL_USER;
import static de.symeda.sormas.api.user.UserRole.POE_INFORMANT;
import static de.symeda.sormas.api.user.UserRole.POE_SUPERVISOR;
import static de.symeda.sormas.api.user.UserRole.STATE_OBSERVER;
import static de.symeda.sormas.api.user.UserRole.SURVEILLANCE_OFFICER;
import static de.symeda.sormas.api.user.UserRole.SURVEILLANCE_SUPERVISOR;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.ValidationException;

import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.facility.FacilityReferenceDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.symeda.sormas.api.AuthProvider;
import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserCriteria;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserFacade;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.backend.AbstractBeanTest;
import de.symeda.sormas.backend.TestDataCreator.RDCF;
import de.symeda.sormas.backend.infrastructure.region.Region;
import de.symeda.sormas.backend.infrastructure.region.RegionFacadeEjb;
import de.symeda.sormas.backend.infrastructure.region.RegionService;

public class UserFacadeEjbTest extends AbstractBeanTest {

	@InjectMocks
	private UserFacadeEjb userFacadeEjb;

	@Mock
	private UserService userService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetUsersByRegionAndRight() {
		RDCF rdcf = creator.createRDCF();
		RegionReferenceDto region = rdcf.region;
		RegionFacadeEjb.RegionFacadeEjbLocal regionFacade = (RegionFacadeEjb.RegionFacadeEjbLocal) getRegionFacade();

		List<UserReferenceDto> result = getUserFacade().getUsersByRegionAndRights(region, UserRight.LAB_MESSAGES);

		assertTrue(result.isEmpty());

		// Has LAB_MASSAGES right
		UserDto natUser = creator.createUser(rdcf, NATIONAL_USER);
		// Does not have LAB_MASSAGES right
		creator.createUser(rdcf, "Some", "User", POE_INFORMANT);
		result = getUserFacade().getUsersByRegionAndRights(region, UserRight.LAB_MESSAGES);

		assertThat(result, contains(equalTo(natUser.toReference())));

		UserDto natUser2 = creator.createUser(rdcf, "Nat", "User2", NATIONAL_USER);
		result = getUserFacade().getUsersByRegionAndRights(region, UserRight.LAB_MESSAGES);

		assertThat(result, hasSize(2));
		assertThat(result, hasItems(equalTo(natUser.toReference()), equalTo(natUser2.toReference())));

	}

	/**
	 * Testing with some users that might not be selected DISTINCT because of 1:n relations.
	 */
	@Test
	public void testGetIndexList() {

		// 1 region, 2 districts
		RDCF rdcf = creator.createRDCF();
		Region region = getBean(RegionService.class).getByUuid(rdcf.region.getUuid());
		creator.createDistrict("district 2", region);

		// user with a 2 districts region, 2 user roles
		creator.createUser(rdcf, "my", "User", CASE_OFFICER, CONTACT_OFFICER);

		// some other users to be filtered out
		creator.createUser(rdcf, "Some", "User", SURVEILLANCE_SUPERVISOR, CONTACT_SUPERVISOR);
		creator.createUser(rdcf, "Other", "User", SURVEILLANCE_OFFICER, DISTRICT_OBSERVER);

		List<UserDto> result;

		// 1. Check that query works without filter: All 3 distinct users are found (+ admin by default)
		result = getUserFacade().getIndexList(new UserCriteria(), 0, 100, null);
		assertThat(result, hasSize(4));
		assertThat(result.stream().map(EntityDto::getUuid).collect(Collectors.toSet()), hasSize(4));

		// 2. Check that only the expected user is found
		result = getUserFacade().getIndexList(new UserCriteria().freeText("myUser"), 0, 100, null);
		assertThat(result, hasSize(1));
		assertThat(result.get(0).getUserName(), equalTo("myUser"));
	}

	/**
	 * Test that the filtering on text fields and sorting be Location.address do not cause a database exception.
	 */
	@Test
	public void testGetIndexListFilteredAndOrderedByAddress() {
		List<UserDto> result =
			getUserFacade().getIndexList(new UserCriteria().freeText("min"), 0, 100, Collections.singletonList(new SortProperty(UserDto.ADDRESS)));

		assertThat(result, hasSize(1));
		assertThat(result.get(0).getUserName(), equalTo("admin"));
	}

	@Test
	public void testGetValidLoginRoles() {
		AuthProvider authProvider = mock(AuthProvider.class);

		MockedStatic<AuthProvider> mockAuthProvider = mockStatic(AuthProvider.class);
		Mockito.when(AuthProvider.getProvider(any())).thenReturn(authProvider);

		RDCF rdcf = creator.createRDCF();
		UserDto user = creator.createUser(rdcf, SURVEILLANCE_SUPERVISOR);
		String password = getUserFacade().resetPassword(user.getUuid());

		Set<UserRight> validLoginRights = getUserFacade().getValidLoginRights(user.getUserName(), password);
		assertThat(
			validLoginRights,
			containsInAnyOrder(getUserRoleConfigFacade().getEffectiveUserRights(SURVEILLANCE_SUPERVISOR).toArray(new UserRight[] {})));

		user.setActive(false);
		getUserFacade().saveUser(user);

		validLoginRights = getUserFacade().getValidLoginRights(user.getUserName(), password);
		assertThat(validLoginRights, nullValue());

		//Important: release static mock.
		mockAuthProvider.closeOnDemand();
	}

	@Test
	public void testGetExistentDefaultUsers() {
		Set<User> defaultUsers = UserTestHelper.generateDefaultUsers(true);
		Set<User> randomUsers = UserTestHelper.generateRandomUsers(30);

		List<User> testUsers = new ArrayList<>();
		testUsers.addAll(defaultUsers);
		testUsers.addAll(randomUsers);

		for (User user : testUsers) {
			Mockito.when(userService.getCurrentUser()).thenReturn(user);
			Mockito.when(userService.getAllDefaultUsers()).thenReturn(Collections.singletonList(user));
			if (defaultUsers.contains(user)) {
				assertEquals(1, userFacadeEjb.getUsersWithDefaultPassword().size());
				assertEquals(UserFacadeEjb.toDto(user), userFacadeEjb.getUsersWithDefaultPassword().get(0));
			} else {
				assertEquals(0, userFacadeEjb.getUsersWithDefaultPassword().size());
			}

			Mockito.when(userService.getAllDefaultUsers()).thenReturn(testUsers);
			if (user.hasAnyUserRole(ADMIN)) {
				assertEquals(defaultUsers.size(), userFacadeEjb.getUsersWithDefaultPassword().size());
				for (User defUser : defaultUsers) {
					assertTrue(userFacadeEjb.getUsersWithDefaultPassword().contains(UserFacadeEjb.toDto(defUser)));
				}
				for (User randomUser : randomUsers) {
					assertFalse(userFacadeEjb.getUsersWithDefaultPassword().contains(UserFacadeEjb.toDto(randomUser)));
				}

			} else if (defaultUsers.contains(user)) {
				assertEquals(1, userFacadeEjb.getUsersWithDefaultPassword().size());
				assertEquals(UserFacadeEjb.toDto(user), userFacadeEjb.getUsersWithDefaultPassword().get(0));
			} else {
				assertEquals(0, userFacadeEjb.getUsersWithDefaultPassword().size());
			}
		}
	}

	@Test
	public void testGetExistentDefaultUsersUpperCase() {
		Set<User> defaultUsers = UserTestHelper.generateDefaultUsers(true);
		for (User user : defaultUsers) {
			user.setUserName(user.getUserName().toUpperCase());
			Mockito.when(userService.getCurrentUser()).thenReturn(user);
			Mockito.when(userService.getAllDefaultUsers()).thenReturn(Collections.singletonList(user));
			assertEquals(0, userFacadeEjb.getUsersWithDefaultPassword().size());
		}
	}

	@Test
	public void testGetByUserName() {
		final UserFacade userFacade = getUserFacade();
		assertNull(userFacade.getByUserName("HansPeter"));

		RDCF rdcf = creator.createRDCF();
		UserDto user = creator.createUser(rdcf, "Hans", "Peter", SURVEILLANCE_OFFICER);

		assertEquals(user, userFacade.getByUserName("HANSPETER"));
		assertEquals(user, userFacade.getByUserName("hanspeter"));
		assertEquals(user, userFacade.getByUserName("HansPeter"));
		assertEquals(user, userFacade.getByUserName("hansPETER"));
	}

	@Test
	public void testLoginUnique() {
		final UserFacade userFacade = getUserFacade();
		assertNull(userFacade.getByUserName("HansPeter"));

		RDCF rdcf = creator.createRDCF();
		creator.createUser(rdcf, "Hans", "Peter", SURVEILLANCE_OFFICER);

		assertTrue(userFacade.isLoginUnique(String.valueOf(UUID.randomUUID()), "MarieLisa"));
		assertFalse(userFacade.isLoginUnique(String.valueOf(UUID.randomUUID()), "HansPeter"));
		assertFalse(userFacade.isLoginUnique(String.valueOf(UUID.randomUUID()), "hanspeter"));
	}

	@Test
	public void testFailOnSavingDuplicateUser() {
		final UserFacade userFacade = getUserFacade();
		assertNull(userFacade.getByUserName("HansPeter"));

		RDCF rdcf = creator.createRDCF();
		creator.createUser(rdcf, "Hans", "Peter", SURVEILLANCE_OFFICER);
		assertThrows("User name is not unique!", ValidationException.class, () -> creator.createUser(rdcf, "Hans", "Peter", SURVEILLANCE_OFFICER));
		assertThrows("User name is not unique!", ValidationException.class, () -> creator.createUser(rdcf, "hans", "peter", SURVEILLANCE_OFFICER));
		assertThrows("User name is not unique!", ValidationException.class, () -> creator.createUser(rdcf, "HANS", "PETER", SURVEILLANCE_OFFICER));
	}
}
