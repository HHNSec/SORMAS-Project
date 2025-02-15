/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.app.backend.event;

import java.util.List;

import de.symeda.sormas.api.PushResult;
import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.event.EventParticipantDto;
import de.symeda.sormas.api.event.EventParticipantReferenceDto;
import de.symeda.sormas.api.person.PersonReferenceDto;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.person.Person;
import de.symeda.sormas.app.backend.person.PersonDependentDtoHelper;
import de.symeda.sormas.app.backend.person.PersonDtoHelper;
import de.symeda.sormas.app.backend.region.District;
import de.symeda.sormas.app.backend.region.DistrictDtoHelper;
import de.symeda.sormas.app.backend.region.Region;
import de.symeda.sormas.app.backend.region.RegionDtoHelper;
import de.symeda.sormas.app.backend.sormastosormas.SormasToSormasOriginInfoDtoHelper;
import de.symeda.sormas.app.backend.user.UserDtoHelper;
import de.symeda.sormas.app.rest.NoConnectionException;
import de.symeda.sormas.app.rest.RetroProvider;
import retrofit2.Call;

public class EventParticipantDtoHelper extends PersonDependentDtoHelper<EventParticipant, EventParticipantDto> {

	private PersonDtoHelper personHelper = new PersonDtoHelper();

	private SormasToSormasOriginInfoDtoHelper sormasToSormasOriginInfoDtoHelper = new SormasToSormasOriginInfoDtoHelper();

	@Override
	protected Class<EventParticipant> getAdoClass() {
		return EventParticipant.class;
	}

	@Override
	protected Class<EventParticipantDto> getDtoClass() {
		return EventParticipantDto.class;
	}

	@Override
	protected Call<List<EventParticipantDto>> pullAllSince(long since, Integer size, String lastSynchronizedUuid)  throws NoConnectionException {
		return RetroProvider.getEventParticipantFacade().pullAllSince(since, size, lastSynchronizedUuid);
	}

	@Override
	protected Call<List<EventParticipantDto>> pullByUuids(List<String> uuids) throws NoConnectionException {
		return RetroProvider.getEventParticipantFacade().pullByUuids(uuids);
	}

	@Override
	protected Call<List<PushResult>> pushAll(List<EventParticipantDto> eventParticipantDtos) throws NoConnectionException {
		return RetroProvider.getEventParticipantFacade().pushAll(eventParticipantDtos);
	}

	@Override
	public void fillInnerFromDto(EventParticipant target, EventParticipantDto source) {
		if (source.getReportingUser() != null) {
			target.setReportingUser(DatabaseHelper.getUserDao().queryUuid(source.getReportingUser().getUuid()));
		} else {
			target.setReportingUser(null);
		}

		if (source.getEvent() != null) {
			target.setEvent(DatabaseHelper.getEventDao().queryUuid(source.getEvent().getUuid()));
		} else {
			target.setEvent(null);
		}

		if (source.getPerson() != null) {
			target.setPerson(DatabaseHelper.getPersonDao().queryUuid(source.getPerson().getUuid()));
		} else {
			target.setPerson(null);
		}

		target.setResponsibleRegion(DatabaseHelper.getRegionDao().getByReferenceDto(source.getRegion()));
		target.setResponsibleDistrict(DatabaseHelper.getDistrictDao().getByReferenceDto(source.getDistrict()));
		target.setInvolvementDescription(source.getInvolvementDescription());
		target.setResultingCaseUuid(source.getResultingCase() != null ? source.getResultingCase().getUuid() : null);
		target.setVaccinationStatus(source.getVaccinationStatus());

		target.setSormasToSormasOriginInfo(
			sormasToSormasOriginInfoDtoHelper.fillOrCreateFromDto(target.getSormasToSormasOriginInfo(), source.getSormasToSormasOriginInfo()));
		target.setOwnershipHandedOver(source.isOwnershipHandedOver());

		target.setPseudonymized(source.isPseudonymized());
	}

	@Override
	public void fillInnerFromAdo(EventParticipantDto target, EventParticipant source) {

		if (source.getReportingUser() != null) {
			target.setReportingUser(UserDtoHelper.toReferenceDto(source.getReportingUser()));
		} else {
			target.setReportingUser(null);
		}

		if (source.getEvent() != null) {
			Event event = DatabaseHelper.getEventDao().queryForId(source.getEvent().getId());
			target.setEvent(EventDtoHelper.toReferenceDto(event));
		} else {
			target.setEvent(null);
		}

		if (source.getPerson() != null) {
			Person person = DatabaseHelper.getPersonDao().queryForId(source.getPerson().getId());
			target.setPerson(personHelper.adoToDto(person));
		} else {
			target.setPerson(null);
		}

		if (source.getResponsibleRegion() != null) {
			Region region = DatabaseHelper.getRegionDao().queryForId(source.getResponsibleRegion().getId());
			target.setRegion(RegionDtoHelper.toReferenceDto(region));
		} else {
			target.setRegion(null);
		}

		if (source.getResponsibleDistrict() != null) {
			District district = DatabaseHelper.getDistrictDao().queryForId(source.getResponsibleDistrict().getId());
			target.setDistrict(DistrictDtoHelper.toReferenceDto(district));
		} else {
			target.setDistrict(null);
		}

		// Resulting case is never set to null from within the app because it
		if (source.getResultingCaseUuid() != null) {
			target.setResultingCase(new CaseReferenceDto(source.getResultingCaseUuid()));
		} else {
			target.setResultingCase(null);
		}

		target.setInvolvementDescription(source.getInvolvementDescription());
		target.setVaccinationStatus(source.getVaccinationStatus());

		if (source.getSormasToSormasOriginInfo() != null) {
			target.setSormasToSormasOriginInfo(sormasToSormasOriginInfoDtoHelper.adoToDto(source.getSormasToSormasOriginInfo()));
		}

		target.setPseudonymized(source.isPseudonymized());
	}

    @Override
    protected long getApproximateJsonSizeInBytes() {
        return EventParticipantDto.APPROXIMATE_JSON_SIZE_IN_BYTES;
    }

    public static EventParticipantReferenceDto toReferenceDto(EventParticipant ado) {
		if (ado == null) {
			return null;
		}
		EventParticipantReferenceDto dto = new EventParticipantReferenceDto(ado.getUuid());

		return dto;
	}

	@Override
	protected PersonReferenceDto getPerson(EventParticipantDto dto) {
		return dto.getPerson().toReference();
	}
}
