/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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

package de.symeda.sormas.backend.sormastosormas.share;

import java.io.Serializable;

import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.backend.sormastosormas.share.sharerequest.ShareRequestPreviews;

public class ShareRequestData implements Serializable {

	private static final long serialVersionUID = -6551795143417885187L;

	private String requestUuid;

	private ShareRequestPreviews previews;

	private SormasToSormasOriginInfoDto originInfo;

	public ShareRequestData() {
	}

	public ShareRequestData(String uuid, ShareRequestPreviews previews, SormasToSormasOriginInfoDto originInfo) {
		this.requestUuid = uuid;
		this.previews = previews;
		this.originInfo = originInfo;
	}

	public String getRequestUuid() {
		return requestUuid;
	}

	public ShareRequestPreviews getPreviews() {
		return previews;
	}

	public SormasToSormasOriginInfoDto getOriginInfo() {
		return originInfo;
	}
}
