/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.api.user;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.contact.ContactReferenceDto;
import de.symeda.sormas.api.event.EventReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.task.TaskContextIndex;
import de.symeda.sormas.api.travelentry.TravelEntryReferenceDto;
import de.symeda.sormas.api.utils.SortProperty;

@Remote
public interface UserFacade {

	UserDto getByUuid(String uuid);

	UserDto saveUser(@Valid UserDto dto);

	boolean isLoginUnique(String uuid, String userName);

	String resetPassword(String uuid);

	List<UserDto> getAllAfter(Date date);

	UserDto getByUserName(String userName);

	List<UserReferenceDto> getUsersByRegionAndRights(RegionReferenceDto regionRef, UserRight... userRights);

	List<UserReferenceDto> getUsersWithSuperiorJurisdiction(UserDto user);

	List<UserDto> getIndexList(UserCriteria userCriteria, Integer first, Integer max, List<SortProperty> sortProperties);

	Page<UserDto> getIndexPage(UserCriteria userCriteria, int offset, int size, List<SortProperty> sortProperties);

	long count(UserCriteria userCriteria);

	/**
	 * @param district
	 * @param userRights
	 *            rights of the users by district
	 * @return
	 */
	List<UserReferenceDto> getUserRefsByDistrict(DistrictReferenceDto district, UserRight... userRights);

	List<UserReferenceDto> getUserRefsByDistricts(List<DistrictReferenceDto> districts, UserRight... userRights);

	List<UserReferenceDto> getAllUserRefs(boolean includeInactive);

	List<UserDto> getUsersByAssociatedOfficer(UserReferenceDto associatedOfficer, UserRight... userRights);

	List<String> getAllUuids();

	List<UserDto> getByUuids(List<String> uuids);

	UserDto getCurrentUser();

	UserReferenceDto getCurrentUserAsReference();

	Set<UserRight> getValidLoginRights(String userName, String password);

	void removeUserAsSurveillanceAndContactOfficer(String userUuid);

	UserSyncResult syncUser(String userUuid);

	List<UserDto> getUsersWithDefaultPassword();

	void enableUsers(List<String> userUuids);

	void disableUsers(List<String> userUuids);

	List<UserReferenceDto> getUsersHavingCaseInJurisdiction(CaseReferenceDto caseReferenceDto);

	List<UserReferenceDto> getUsersHavingContactInJurisdiction(ContactReferenceDto contactReferenceDto);

	List<UserReferenceDto> getUsersHavingEventInJurisdiction(EventReferenceDto event);

	List<UserReferenceDto> getUsersHavingTravelEntryInJurisdiction(TravelEntryReferenceDto travelEntryReferenceDto);

	List<UserReferenceWithTaskNumbersDto> getAssignableUsersWithTaskNumbers(@NotNull TaskContextIndex taskContextIndex);
}
