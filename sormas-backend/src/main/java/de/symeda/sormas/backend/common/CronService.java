/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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
package de.symeda.sormas.backend.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.symeda.sormas.api.common.CoreEntityType;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.feature.FeatureTypeProperty;
import de.symeda.sormas.api.importexport.ImportExportUtils;
import de.symeda.sormas.api.task.TaskType;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.backend.caze.CaseFacadeEjb.CaseFacadeEjbLocal;
import de.symeda.sormas.backend.common.ConfigFacadeEjb.ConfigFacadeEjbLocal;
import de.symeda.sormas.backend.contact.ContactFacadeEjb.ContactFacadeEjbLocal;
import de.symeda.sormas.backend.deletionconfiguration.CoreEntityDeletionService;
import de.symeda.sormas.backend.document.DocumentFacadeEjb.DocumentFacadeEjbLocal;
import de.symeda.sormas.backend.event.EventFacadeEjb.EventFacadeEjbLocal;
import de.symeda.sormas.backend.feature.FeatureConfigurationFacadeEjb.FeatureConfigurationFacadeEjbLocal;
import de.symeda.sormas.backend.immunization.ImmunizationFacadeEjb;
import de.symeda.sormas.backend.infrastructure.central.CentralInfraSyncFacade;
import de.symeda.sormas.backend.labmessage.LabMessageFacadeEjb.LabMessageFacadeEjbLocal;
import de.symeda.sormas.backend.report.WeeklyReportFacadeEjb.WeeklyReportFacadeEjbLocal;
import de.symeda.sormas.backend.systemevent.SystemEventFacadeEjb.SystemEventFacadeEjbLocal;
import de.symeda.sormas.backend.task.TaskFacadeEjb.TaskFacadeEjbLocal;

@Singleton
@RunAs(UserRight._SYSTEM)
public class CronService {

	public static final int TASK_UPDATE_INTERVAL = 10;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@EJB
	private ConfigFacadeEjbLocal configFacade;
	@EJB
	private ContactFacadeEjbLocal contactFacade;
	@EJB
	private WeeklyReportFacadeEjbLocal weeklyReportFacade;
	@EJB
	private TaskFacadeEjbLocal taskFacade;
	@EJB
	private FeatureConfigurationFacadeEjbLocal featureConfigurationFacade;
	@EJB
	private CaseFacadeEjbLocal caseFacade;
	@EJB
	private EventFacadeEjbLocal eventFacade;
	@EJB
	private DocumentFacadeEjbLocal documentFacade;
	@EJB
	private SystemEventFacadeEjbLocal systemEventFacade;
	@EJB
	private LabMessageFacadeEjbLocal labMessageFacade;
	@EJB
	private ImmunizationFacadeEjb.ImmunizationFacadeEjbLocal immunizationFacade;
	@EJB
	private CentralInfraSyncFacade centralInfraSyncFacade;
	@EJB
	private CoreEntityDeletionService coreEntityDeletionService;

	@Schedule(hour = "*", minute = "*/" + TASK_UPDATE_INTERVAL, second = "0", persistent = false)
	public void sendNewAndDueTaskMessages() {
		taskFacade.sendNewAndDueTaskMessages();
	}

	@Schedule(hour = "*", minute = "*/2", second = "0", persistent = false)
	public void calculateCaseCompletion() {
		long timeStart = DateHelper.startTime();
		int casesUpdated = caseFacade.updateCompleteness();
		logger.debug("calculateCaseCompletion finished. {} cases, {} s", casesUpdated, DateHelper.durationSeconds(timeStart));
	}

	@Schedule(hour = "1", minute = "0", second = "0", persistent = false)
	public void deleteAllExpiredFeatureConfigurations() {

		// Remove all feature configurations whose end dates have been reached
		featureConfigurationFacade.deleteAllExpiredFeatureConfigurations(new Date());
		logger.info("Deleted expired feature configurations");
	}

	@Schedule(hour = "1", minute = "5", second = "0", persistent = false)
	public void generateAutomaticTasks() {

		if (featureConfigurationFacade.isTaskGenerationFeatureEnabled(TaskType.CONTACT_FOLLOW_UP)) {
			contactFacade.generateContactFollowUpTasks();
		}
		if (featureConfigurationFacade.isTaskGenerationFeatureEnabled(TaskType.WEEKLY_REPORT_GENERATION)) {
			weeklyReportFacade.generateSubmitWeeklyReportTasks();
		}
	}

	@Schedule(hour = "1", minute = "10", second = "0", persistent = false)
	public void cleanUpTemporaryFiles() {

		Date now = new Date();
		File exportFolder = new File(configFacade.getTempFilesPath());
		int numberOfDeletedFiles = 0;
		for (final File fileEntry : exportFolder.listFiles()) {
			// Skip the file if it's a directory or not a temporary sormas file
			if (!fileEntry.isFile() || (!fileEntry.getName().startsWith(ImportExportUtils.TEMP_FILE_PREFIX))) {
				continue;
			}

			try {
				BasicFileAttributes fileAttributes = Files.readAttributes(fileEntry.toPath(), BasicFileAttributes.class);
				if (now.getTime() - fileAttributes.creationTime().toMillis() >= 1000 * 60 * 120) {
					fileEntry.delete();
					numberOfDeletedFiles++;
				}
			} catch (IOException e) {
				logger.info("Error deleting a file in CronService. The file in question was " + fileEntry.getAbsolutePath(), e);
			}
		}

		logger.info("Deleted " + numberOfDeletedFiles + " export files");
	}

	@Schedule(hour = "1", minute = "15", second = "0", persistent = false)
	public void archiveCases() {

		final int daysAfterCaseGetsArchived = featureConfigurationFacade
			.getProperty(FeatureType.AUTOMATIC_ARCHIVING, CoreEntityType.CASE, FeatureTypeProperty.THRESHOLD_IN_DAYS, Integer.class);
		final int daysAfterContactsGetsArchived = featureConfigurationFacade
			.getProperty(FeatureType.AUTOMATIC_ARCHIVING, CoreEntityType.CONTACT, FeatureTypeProperty.THRESHOLD_IN_DAYS, Integer.class);
		if (daysAfterCaseGetsArchived < daysAfterContactsGetsArchived) {
			logger.warn(
				FeatureTypeProperty.THRESHOLD_IN_DAYS + " for " + CoreEntityType.CONTACT + " [{}] should be <= the one for " + CoreEntityType.CASE
					+ " [{}]",
				daysAfterContactsGetsArchived,
				daysAfterCaseGetsArchived);
		}
		if (daysAfterCaseGetsArchived >= 1) {
			caseFacade.archiveAllArchivableCases(daysAfterCaseGetsArchived);
		}
	}

	@Schedule(hour = "1", minute = "20", second = "0", persistent = false)
	public void archiveEvents() {

		final int daysAfterEventsGetsArchived = featureConfigurationFacade
			.getProperty(FeatureType.AUTOMATIC_ARCHIVING, CoreEntityType.EVENT, FeatureTypeProperty.THRESHOLD_IN_DAYS, Integer.class);
		final int daysAfterEventParticipantsGetsArchived = featureConfigurationFacade
			.getProperty(FeatureType.AUTOMATIC_ARCHIVING, CoreEntityType.EVENT_PARTICIPANT, FeatureTypeProperty.THRESHOLD_IN_DAYS, Integer.class);
		if (daysAfterEventsGetsArchived < daysAfterEventParticipantsGetsArchived) {
			logger.warn(
				FeatureTypeProperty.THRESHOLD_IN_DAYS + " for " + CoreEntityType.EVENT_PARTICIPANT + " [{}] should be <= the one for "
					+ CoreEntityType.EVENT + " [{}]",
				daysAfterEventParticipantsGetsArchived,
				daysAfterEventsGetsArchived);
		}
		if (daysAfterEventsGetsArchived >= 1) {
			eventFacade.archiveAllArchivableEvents(daysAfterEventsGetsArchived);
		}
	}

	@Schedule(hour = "1", minute = "25", second = "0", persistent = false)
	public void cleanupDeletedDocuments() {
		documentFacade.cleanupDeletedDocuments();
	}

	@Schedule(hour = "1", minute = "30", second = "0", persistent = false)
	public void deleteSystemEvents() {
		int daysAfterSystemEventGetsDeleted = configFacade.getDaysAfterSystemEventGetsDeleted();
		if (daysAfterSystemEventGetsDeleted >= 1) {
			systemEventFacade.deleteAllDeletableSystemEvents(daysAfterSystemEventGetsDeleted);
		}
	}

	@Schedule(hour = "1", minute = "35", second = "0", persistent = false)
	public void fetchLabMessages() {
		if (featureConfigurationFacade.isFeatureEnabled(FeatureType.LAB_MESSAGES)) {
			labMessageFacade.fetchAndSaveExternalLabMessages(null);
		}
	}

	@Schedule(hour = "1", minute = "40", second = "0", persistent = false)
	public void updateImmunizationStatuses() {
		if (featureConfigurationFacade.isFeatureEnabled(FeatureType.IMMUNIZATION_STATUS_AUTOMATION)) {
			immunizationFacade.updateImmunizationStatuses();
		}
	}

	@Schedule(hour = "1", minute = "50", persistent = false)
	public void syncInfraWithCentral() {
		centralInfraSyncFacade.syncAll();
	}

	@Schedule(hour = "1", minute = "55", persistent = false)
	public void deleteExpiredEntities() {
		coreEntityDeletionService.executeAutomaticDeletion();
	}

	@Schedule(hour = "2", minute = "10", persistent = false)
	public void permanentDeleteEntities() {
		coreEntityDeletionService.executePermanentDeletion();
	}
}
