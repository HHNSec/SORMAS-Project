/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2022 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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
 */

package org.sormas.e2etests.steps.web.application.cases;

import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.CASE_EXPORT_BUTTON;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.CONFIGURATION_NAME_INPUT;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.CUSTOM_CASE_DELETE_BUTTON;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.CUSTOM_CASE_EXPORT_BUTTON;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.CUSTOM_CASE_EXPORT_DOWNLOAD_BUTTON;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.EXPORT_CONFIGURATION_DATA_DISEASE_CHECKBOX;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.EXPORT_CONFIGURATION_DATA_FIRST_NAME_CHECKBOX;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.EXPORT_CONFIGURATION_DATA_LAST_NAME_CHECKBOX;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.NEW_EXPORT_CONFIGURATION_BUTTON;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.NEW_EXPORT_CONFIGURATION_SAVE_BUTTON;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import cucumber.api.java8.En;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.sormas.e2etests.entities.pojo.csv.CustomCaseExportCSV;
import org.sormas.e2etests.enums.DiseasesValues;
import org.sormas.e2etests.helpers.WebDriverHelpers;
import org.sormas.e2etests.state.ApiState;
import org.testng.asserts.SoftAssert;

@Slf4j
public class CaseImportExportSteps implements En {

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  String configurationName;

  @Inject
  public CaseImportExportSteps(
      WebDriverHelpers webDriverHelpers, ApiState apiState, SoftAssert softly) {

    When(
        "I click on the Export case button",
        () -> {
          TimeUnit.SECONDS.sleep(2); // Wait for filter
          webDriverHelpers.clickOnWebElementBySelector(CASE_EXPORT_BUTTON);
        });

    When(
        "I click on the Custom Case Export button",
        () -> webDriverHelpers.clickOnWebElementBySelector(CUSTOM_CASE_EXPORT_BUTTON));

    When(
        "I click on the New Export Configuration button in Custom Case Export popup",
        () -> webDriverHelpers.clickOnWebElementBySelector(NEW_EXPORT_CONFIGURATION_BUTTON));

    When(
        "I fill Configuration Name field with ([^\"]*)",
        (String confName) -> {
          configurationName = confName + LocalDate.now().toString();
          webDriverHelpers.fillInWebElement(CONFIGURATION_NAME_INPUT, configurationName);
        });

    When(
        "I select specific data to export in Export Configuration",
        () -> {
          webDriverHelpers.clickOnWebElementBySelector(EXPORT_CONFIGURATION_DATA_DISEASE_CHECKBOX);
          webDriverHelpers.clickOnWebElementBySelector(
              EXPORT_CONFIGURATION_DATA_FIRST_NAME_CHECKBOX);
          webDriverHelpers.clickOnWebElementBySelector(
              EXPORT_CONFIGURATION_DATA_LAST_NAME_CHECKBOX);
          webDriverHelpers.clickOnWebElementBySelector(NEW_EXPORT_CONFIGURATION_SAVE_BUTTON);
        });

    When(
        "I download created custom case export file",
        () -> {
          webDriverHelpers.clickOnWebElementBySelector(CUSTOM_CASE_EXPORT_DOWNLOAD_BUTTON);
          TimeUnit.SECONDS.sleep(5); // wait for download
        });

    When(
        "I check if downloaded data generated by custom case option is correct",
        () -> {
          CustomCaseExportCSV reader =
              parseCustomCaseExport(
                  "./downloads/sormas_f\u00E4lle_" + LocalDate.now().format(formatter) + "_.csv");
          softly.assertEquals(
              reader.getDisease(),
              DiseasesValues.getCaptionForName(apiState.getCreatedCase().getDisease()),
              "Diseases are not equal");
          softly.assertEquals(
              reader.getFirstName(),
              apiState.getLastCreatedPerson().getFirstName(),
              "First names are not equal");
          softly.assertEquals(
              reader.getLastName(),
              apiState.getLastCreatedPerson().getLastName(),
              "Last names are not equal");
          softly.assertAll();
        });

    When(
        "I delete created custom case export file",
        () -> webDriverHelpers.clickOnWebElementBySelector(CUSTOM_CASE_DELETE_BUTTON));
  }

  public CustomCaseExportCSV parseCustomCaseExport(String fileName) {
    List<String[]> r = null;
    String[] values = new String[] {};
    CustomCaseExportCSV builder = null;
    CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
    try (CSVReader reader =
        new CSVReaderBuilder(new FileReader(fileName))
            .withCSVParser(csvParser)
            .withSkipLines(3) // parse only data
            .build()) {
      r = reader.readAll();
    } catch (IOException e) {
      log.error("IOException parseCustomCaseExport: ", e);
    } catch (CsvException e) {
      log.error("CsvException parseCustomCaseExport: ", e);
    }
    try {
      for (int i = 0; i < r.size(); i++) {
        values = r.get(i);
      }
      builder =
          CustomCaseExportCSV.builder()
              .disease(values[0])
              .firstName(values[1])
              .lastName(values[2])
              .build();
    } catch (NullPointerException e) {
      log.error("Null pointer exception parseCustomCaseExport: ", e);
    }
    return builder;
  }
}
