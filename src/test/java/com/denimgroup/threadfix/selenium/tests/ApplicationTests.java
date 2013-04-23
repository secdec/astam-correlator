////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2013 Denim Group, Ltd.
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.selenium.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.selenium.pages.ApplicationAddPage;
import com.denimgroup.threadfix.selenium.pages.ApplicationDetailPage;
import com.denimgroup.threadfix.selenium.pages.ApplicationEditPage;
import com.denimgroup.threadfix.selenium.pages.DefectTrackerIndexPage;
import com.denimgroup.threadfix.selenium.pages.LoginPage;
import com.denimgroup.threadfix.selenium.pages.TeamDetailPage;
import com.denimgroup.threadfix.selenium.pages.TeamIndexPage;
import com.denimgroup.threadfix.selenium.pages.WafAddPage;
import com.denimgroup.threadfix.selenium.pages.WafDetailPage;
import com.denimgroup.threadfix.selenium.pages.WafIndexPage;

public class ApplicationTests extends BaseTest {
	private WebDriver driver;
	private static LoginPage loginPage;
	private ApplicationAddPage applicationAddPage;
	private ApplicationDetailPage applicationDetailPage;
	private TeamDetailPage teamDetailPage;
	private TeamIndexPage teamIndexPage;
	private ApplicationEditPage applicationEditPage;
	private WafAddPage wafAddPage;
	private WafIndexPage wafIndexPage;
	private WafDetailPage wafDetailPage;
	
	@Before
	public void init() {
		super.init();
		driver = super.getDriver();
		loginPage = LoginPage.open(driver);
	}
	
	@Test 
	public void testCreateBasicApplication() {
		String teamName = "testCreateApplicationOrgw" + getRandomString(3);
		String appName = "testCreateApplicationAppw" + getRandomString(3);
		String urlText = "http://testurl.com";
		
		teamIndexPage = loginPage.login("user", "password")
				.clickOrganizationHeaderLink()
				.clickAddTeamButton()
				.setTeamName(teamName)
				.addNewTeam()
				.addNewApplication(teamName, appName, urlText, "Low");
										
		applicationDetailPage = teamIndexPage.clickOrganizationHeaderLink()
											.expandTeamRowByName(teamName)
											.clickViewAppLink(appName, teamName);

		
		assertTrue("The name was not preserved correctly.", applicationDetailPage.getNameText().contains(appName));
		
		teamIndexPage = applicationDetailPage.clickOrganizationHeaderLink();
		
		assertTrue("The organization was not preserved correctly.", 
				teamIndexPage.teamAddedToTable(teamName));
		
		//cleanup
		loginPage = teamIndexPage.expandTeamRowByName(teamName)
										.clickViewAppLink(appName, teamName)
										.clickActionButton()
										.clickDeleteLink()
										.clickDeleteButton()
										.logout();
		
	}
	
	@Test 
	public void testCreateBasicApplicationValidation() {
		String orgName = "testCreateApplicationOrg2a";
		String appName = null;
		String urlText = "htnotaurl.com";
		
		StringBuilder stringBuilder = new StringBuilder("");
		for (int i = 0; i < Application.NAME_LENGTH + 50; i++) { stringBuilder.append('i'); }
		String longInputName = stringBuilder.toString();
		
		stringBuilder = new StringBuilder("");
		for (int i = 0; i < Application.URL_LENGTH + 50; i++) { stringBuilder.append('i'); }
		String longInputUrl = "http://" + stringBuilder.toString();
		
		String emptyError = "This field cannot be blank";
		
		String emptyString = "";
		String whiteSpace = "     ";
		
		//set up an organization
		
		teamIndexPage = loginPage.login("user", "password")
										.clickOrganizationHeaderLink()
										.clickAddTeamButton()
										.addNewTeam()
										.expandTeamRowByName(orgName)
										.addNewApplication(orgName, emptyString, emptyString, "Low");
		
		assertTrue("The correct error did not appear for the name field.", 
				teamIndexPage.getNameErrorMessage().contains(emptyError));
		
		teamIndexPage = teamIndexPage.closeModal()
													.clickOrganizationHeaderLink()
													.expandTeamRowByName(orgName)
													.addNewApplication(orgName, whiteSpace, whiteSpace, "Low");
		
		
		assertTrue("The correct error did not appear for the name field.", 
				teamIndexPage.getNameErrorMessage().contains(emptyError));
		assertTrue("The correct error did not appear for the url field.", 
				teamIndexPage.getUrlErrorMessage().contains("Not a valid URL"));
		
		// Test URL format
		teamIndexPage = teamIndexPage.closeModal()
													.clickOrganizationHeaderLink()
													.expandTeamRowByName(orgName)
													.addNewApplication(orgName, "dummyApp", urlText, "Low");
		
		assertTrue("The correct error did not appear for the url field.", 
				teamIndexPage.getUrlErrorMessage().contains("Not a valid URL"));

		// Test browser field length limits
		applicationDetailPage = teamIndexPage.closeModal()
				.clickOrganizationHeaderLink()
				.expandTeamRowByName(orgName)
				.addNewApplication(orgName, longInputName, longInputUrl, "Low")
				.clickOrganizationHeaderLink()
				.expandTeamRowByName(orgName)
				.clickApplicationDetailLink("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");

		
		assertTrue("The length limit was incorrect for name.", 
				applicationDetailPage.getNameText().length() == Application.NAME_LENGTH);
		assertTrue("The length limit was incorrect for url.", 
				applicationDetailPage.clickDetailsLink().getUrlText().length() == Application.URL_LENGTH);
		
		appName = applicationDetailPage.getNameText();
		teamIndexPage = applicationDetailPage.clickOrganizationHeaderLink();
		
		// Test name duplication check
		teamIndexPage = applicationDetailPage.clickOrganizationHeaderLink()
												.expandTeamRowByName(orgName)
												.addNewApplication(orgName, appName, "http://dummyurl", "Low");
		
		assertTrue("The duplicate message didn't appear correctly.", 
				teamIndexPage.getNameErrorMessage().contains("That name is already taken."));

		//cleanup
		loginPage = teamIndexPage.closeModal()
										.clickOrganizationHeaderLink()
										.expandTeamRowByName(orgName)
										.clickViewTeamLink()
										.clickDeleteButton()
										.logout();
	}
	
	@Test
	public void testEditBasicApplication() {
		String orgName = "testCreateApplicationOrg21";
		String appName1 = "testCreateApplicationApp21";
		String urlText1 = "http://testurl.com";
		String appName2 = "testCreateApplicationApp22";
		String urlText2 = "http://testurl.com352";
		
		// set up an organization
		teamIndexPage = loginPage.login("user", "password")
				.clickOrganizationHeaderLink()
				.clickAddTeamButton()
				.addNewTeam(orgName)
				.expandTeamRowByName(orgName)
				.addNewApplication(orgName, appName1, urlText1, "Low");
	
		
		applicationDetailPage = teamIndexPage.clickOrganizationHeaderLink()
													.expandTeamRowByName(orgName)
													.clickApplicationDetailLink(appName1);

		assertTrue("The name was not preserved correctly.", 
					appName1.equals(applicationDetailPage.getNameText()));
		assertTrue("The URL was not preserved correctly.", 
					urlText1.equals(applicationDetailPage.clickDetailsLink().getUrlText()));
		
		applicationDetailPage = applicationDetailPage.clickEditLink()
													.setNameInput(appName2)
													.setUrlInput(urlText2)
													.clickUpdateApplicationButton();
		
		assertTrue("The name was not preserved correctly.", 
				appName2.equals(applicationDetailPage.getNameText()));
		assertTrue("The URL was not preserved correctly.", 
				urlText2.equals(applicationDetailPage.clickDetailsLink().getUrlText()));	
		
		// ensure that the application is present in the organization's app table.
		teamIndexPage = applicationDetailPage.clickOrganizationHeaderLink()
													.expandTeamRowByName(orgName);
		
		assertTrue("The application does not appear in the organization page.", 
				teamIndexPage.isAppPresent(appName2));
		
		//cleanup
		loginPage = teamIndexPage.clickViewTeamLink()
										.clickDeleteButton()
										.logout();
	}
	
	@Test 
	public void testEditBasicApplicationValidation() {
		String orgName = "testCreateApplicationOrg312";
		String appName2 = "testApp23";
		String appName = "testApp17";
		String validUrlText = "http://test.com";
		String urlText = "htnotaurl.com";
		
		StringBuilder stringBuilder = new StringBuilder("");
		for (int i = 0; i < Application.NAME_LENGTH + 50; i++) { stringBuilder.append('i'); }
		String longInputName = stringBuilder.toString();
		
		stringBuilder = new StringBuilder("");
		for (int i = 0; i < Application.URL_LENGTH + 50; i++) { stringBuilder.append('i'); }
		String longInputUrl = "http://" + stringBuilder.toString();
		
		String emptyError = "This field cannot be blank";
		
		String emptyString = "";
		String whiteSpace = "     ";
		
		//set up an organization,
		//add an application for duplicate checking,
		//add an application for normal testing,
		// and Test a submission with no changes
		applicationDetailPage = loginPage.login("user", "password")
										.clickOrganizationHeaderLink()
										.clickAddTeamButton()
										.addNewTeam(orgName)
										.expandTeamRowByName(orgName)
										.addNewApplication(orgName, appName2, validUrlText, "Low")
										.clickOrganizationHeaderLink()
										.expandTeamRowByName(orgName)
										.addNewApplication(orgName, appName, validUrlText, "Low")
										.clickOrganizationHeaderLink()
										.expandTeamRowByName(orgName)
										.clickApplicationDetailLink(appName)
										.clickEditLink()
										.clickUpdateApplicationButton();
		
		
		
		assertTrue("The name was not preserved correctly.", 
				appName.equals(applicationDetailPage.getNameText()));
		assertTrue("The URL was not preserved correctly.", 
				validUrlText.equals(applicationDetailPage.clickDetailsLink().getUrlText()));

		// Test blank input		
		applicationEditPage = applicationDetailPage.clickEditLink()
												   .setNameInput(emptyString)
												   .setUrlInput(emptyString)
												   .clickUpdateApplicationButtonInvalid();
		
		assertTrue("The correct error did not appear for the name field.", 
				applicationEditPage.getNameError().equals(emptyError));
		
		// Test whitespace input
		applicationEditPage = applicationEditPage.setNameInput(whiteSpace)
												 .setUrlInput(whiteSpace)
												 .clickUpdateApplicationButtonInvalid();

		assertTrue("The correct error did not appear for the name field.", 
				applicationEditPage.getNameError().equals(emptyError));
		assertTrue("The correct error did not appear for the url field.", 
				applicationEditPage.getUrlError().equals("Not a valid URL"));
		
		// Test URL format
		applicationEditPage = applicationEditPage.setNameInput("dummyName")
				 								 .setUrlInput(urlText)
				 								 .clickUpdateApplicationButtonInvalid();

		assertTrue("The correct error did not appear for the url field.", 
				applicationEditPage.getUrlError().equals("Not a valid URL"));

		// Test name duplication check
		applicationEditPage = applicationEditPage.setNameInput(appName2)
												 .setUrlInput("http://dummyurl")
												 .clickUpdateApplicationButtonInvalid();

		assertTrue("The duplicate message didn't appear correctly.", 
				applicationEditPage.getNameError().equals("That name is already taken."));

		// Test browser field length limits
		applicationDetailPage = applicationEditPage.setNameInput(longInputName)
												   .setUrlInput(longInputUrl)
												   .clickUpdateApplicationButton();

		assertTrue("The length limit was incorrect for name.", 
				applicationDetailPage.getNameText().length() == Application.NAME_LENGTH);
		assertTrue("The length limit was incorrect for url.", 
				applicationDetailPage.clickDetailsLink().getUrlText().length() == Application.URL_LENGTH);
				
		//cleanup
		loginPage = applicationDetailPage.clickDeleteLink()
										.clickTextLinkInApplicationsTableBody(appName2)
										.clickDeleteLink()
										.clickDeleteButton()
										.logout();
	}
	
	@Test
	public void testAddWafAtApplicationCreationTimeAndDelete() {
		String wafName = "appCreateTimeWaf1";
		String type = "Snort";
		String orgName = "appCreateTimeWafOrg2";
		String appName = "appCreateTimeWafName2";
		String appUrl = "http://testurl.com";
		
		wafIndexPage = loginPage.login("user", "password").clickWafsHeaderLink()
														.clickAddWafLink()
														.createNewWaf(wafName, type);

		// Add Application with WAF
		applicationDetailPage = wafIndexPage.clickOrganizationHeaderLink()
										.clickAddTeamButton()
										.addNewTeam(orgName)
										.expandTeamRowByName(orgName)
										.addNewApplication(orgName, appName, appUrl, "Low")
										.clickOrganizationHeaderLink()
										.expandTeamRowByName(orgName)
										.clickApplicationDetailLink(appName)
										.clickActionButton()
										.clickShowDetails()
										.clickAddWaf()
										.addWaf(wafName);
		
		assertTrue("The WAF was not added correctly.", 
				applicationDetailPage.getWafText().equals(wafName));	
		// Check that it also appears on the WAF page.
		wafDetailPage = applicationDetailPage.clickWafsHeaderLink()
											.clickRules(wafName);
		
		assertTrue("The WAF was not added correctly.", 
				wafDetailPage.isTextPresentInApplicationsTableBody(appName));
		
		// Attempt to delete the WAF and ensure that it is a failure because the Application is still there
		// If the page goes elsewhere, this call will fail.
		wafIndexPage = wafDetailPage.clickWafsHeaderLink()
								.clickDeleteWaf(1);
		
		// Delete app and org and make sure the Application doesn't appear in the WAFs table.
		wafDetailPage = wafIndexPage.clickOrganizationHeaderLink()
								.expandTeamRowByName(orgName)
								.clickViewTeamLink()
								.clickDeleteButton()
								.clickWafsHeaderLink()
								.clickRules(wafName);
		
		assertFalse("The Application was not removed from the WAF correctly.", 
				wafDetailPage.isTextPresentInApplicationsTableBody(appName));
		
		loginPage = wafDetailPage.clickWafsHeaderLink().clickDeleteWaf(wafName).logout();
		
	}
	
	@Test
	public void testSwitchWafs() {
		//TODO
		String wafName1 = "firstWaf";
		String wafName2 = "wafToSwitch";
		String type1 = "Snort";
		String type2 = "mod_security";
		String orgName = "switchWafOrg";
		String appName = "switchWafApp";
		String appUrl = "http://testurl.com";
		
		// create WAFs and set up the application with one
		// then switch to the other one and verify that the switch has been made.
		applicationDetailPage = loginPage.login("user", "password")
										 .clickWafsHeaderLink()
										 .clickAddWafLink()
										 .createNewWaf(wafName1, type1)
										 .clickAddWafLink()
										 .createNewWaf(wafName2, type2)	
										 .clickOrganizationHeaderLink()
										 .clickAddTeamButton()
										 .addNewTeam(orgName)
										 .expandTeamRowByName(orgName)
										 .addNewApplication(orgName, appName, appUrl, "Low")
										 .clickOrganizationHeaderLink()
										 .expandTeamRowByName(orgName)
										 .clickApplicationDetailLink(appName)
										 .clickActionButton()
										 .clickShowDetails()
										 .clickAddWaf()
										 .addWaf(wafName1)
										 .clickOrganizationHeaderLink()
										 .expandTeamRowByName(orgName)
										 .clickApplicationDetailLink(appName)
										 .clickActionButton()
										 .clickShowDetails()
										 .clickEditWaf()
										 .addWaf(wafName2)
										 .clickOrganizationHeaderLink()
										 .expandTeamRowByName(orgName)
										 .clickApplicationDetailLink(appName)
										 .clickActionButton()
										 .clickShowDetails();
								
		assertTrue("The edit didn't change the application's WAF.", 
				applicationDetailPage.getWafText().contains(wafName2));
		
		//cleanup
		loginPage = applicationDetailPage.clickOrganizationHeaderLink()
										.expandTeamRowByName(orgName)
										.clickViewTeamLink()
										.clickDeleteButton()
										.clickWafsHeaderLink()
										.clickDeleteWaf(1)
										.clickDeleteWaf(1)
										.logout();
	}
	@Ignore //redundant with testAddWafAtApplicationCreationTimeAndDelete()
	@Test
	public void testAddWafAtApplicationEditTime() {
		String wafName = "appCreateTimeWaf";
		String type = "mod_security";
		String orgName = "appCreateTimeWafOrg";
		String appName = "appCreateTimeWafName";
		String appUrl = "http://testurl.com";
		
		// Add Application with WAF
		applicationDetailPage = loginPage.login("user", "password")
										 .clickWafsHeaderLink()
										 .clickAddWafLink()
										 .createNewWaf(wafName, type)
										 .clickOrganizationHeaderLink()
										 .clickAddTeamButton()
										 .addNewTeam(orgName)
										 .expandTeamRowByName(orgName)
										 .addNewApplication(orgName, appName, appUrl, "Low")
										 .clickOrganizationHeaderLink()
										 .expandTeamRowByName(orgName)
										 .clickApplicationDetailLink(appName)
										 .clickActionButton()
										 .clickShowDetails()
										 .clickAddWaf()
										 .addWaf(wafName);

		
		assertTrue("The WAF was not added correctly.", 
				applicationDetailPage.getWafText().equals(wafName));
		
		// Check that it also appears on the WAF page.
		wafDetailPage = applicationDetailPage.clickWafsHeaderLink()
											.clickRules(wafName);
		
		assertTrue("The WAF was not added correctly.", 
				wafDetailPage.isTextPresentInApplicationsTableBody(appName));
		
		// Delete app and org and make sure the Application doesn't appear in the WAFs table.

		loginPage = wafDetailPage.clickOrganizationHeaderLink()
				 				.expandTeamRowByName(orgName)
								.clickViewTeamLink()
								.clickDeleteButton()
								.clickWafsHeaderLink()
								.clickWafsHeaderLink()
								.clickDeleteWaf(wafName)
								.logout();
	}
	@Ignore //defect tracker can not be added at application creation time anymore
	@Test
	public void testAddDefectTrackerAtApplicationCreationTimeAndDelete() {
		/*String dtUrl = DefectTrackerIndexPage.DT_URL;
		String orgName = "AppDTCreationTimeOrg";
		String appName = "AppDTCreationTimeApp";
		String appUrl = "http://testUrl.com";
		String dtName = "DG Bugzilla 1234";
		String dtType = "Bugzilla";
		String dtUserName = "mcollins@denimgroup.com";
		String dtPassword = "bugzilla";
		String projectName = "TestProduct";
		
		// create stuff
		applicationDetailPage = loginPage.login("user", "password")
										 .clickConfigurationHeaderLink()
										 .clickDefectTrackersLink()
										 .clickAddDefectTrackerLink()
										 .setDefectTrackerTypeSelect(dtType)
										 .setNameInput(dtName)
										 .setUrlInput(dtUrl)
										 .clickAddDefectTrackerButton()
										 .clickOrganizationHeaderLink()
										 .clickAddTeamButton()
										 .setNameInput(orgName)
										 .clickSubmitButtonValid()
										 .clickAddApplicationLink()
										 .setNameInput(appName)
										 .setUrlInput(appUrl)
										 .setDefectTrackerIdSelect(dtName + " (" + dtType + ")")
										 .setUserNameInput(dtUserName)
										 .setPasswordInput(dtPassword)
										 .clickJsonLink()
										 .waitForJsonResult()
										 .setProjectListSelect(projectName)
										 .clickAddApplicationButton();
		
		assertTrue("The Defect Tracker was not added correctly.", 
				applicationDetailPage.getDefectTrackerText().equals(dtName));
		
		// delete stuff
		loginPage = applicationDetailPage.clickDeleteLink()
										 .clickDeleteButton()
										 .clickConfigurationHeaderLink()
										 .clickDefectTrackersLink()
										 .clickTextLinkInDefectTrackerTableBody(dtName)
										 .clickDeleteButton()
										 .logout();*/
	}
	
	@Test
	public void testSwitchDefectTrackers() {
		String dtUrl = DefectTrackerIndexPage.DT_URL;
		String orgName = "AppDTCreationTimeOrg";
		String appName = "AppDTCreationTimeApp";
		String appUrl = "http://testUrl.com";
		String dtName1 = "DG Bugzilla 6347";
		String dtType1 = "Bugzilla";
		String dtName2 = "DG Bugzilla 2";
		String dtType2 = "Bugzilla";
		String dtUserName = "mcollins@denimgroup.com";
		String dtPassword = "bugzilla";
		String projectName = "TestProduct";
		
		// create stuff
		applicationDetailPage = loginPage.login("user", "password")
										 .clickDefectTrackersLink()
										 .clickAddDefectTrackerButton()
										 .setDefectTrackerTypeSelect(dtType1)
										 .setNameInput(dtName1)
										 .setUrlInput(dtUrl)
										 .clickAddDefectTrackerButton()
										 .clickBackToListLink()
										 .clickAddDefectTrackerLink()
										 .setDefectTrackerTypeSelect(dtType2)
										 .setNameInput(dtName2)
										 .setUrlInput(dtUrl)
										 .clickAddDefectTrackerButton()
										 .clickOrganizationHeaderLink()
										 .clickAddTeamButton()
										 .setNameInput(orgName)
										 .clickSubmitButtonValid()
										 .clickAddApplicationLink()
										 .setNameInput(appName)
										 .setUrlInput(appUrl)
										 .setDefectTrackerIdSelect(dtName1 + " (" + dtType1 + ")")
										 .setUserNameInput(dtUserName)
										 .setPasswordInput(dtPassword)
										 .clickJsonLink()
										 .waitForJsonResult()
										 .setProjectListSelect(projectName)
										 .clickAddApplicationButton()
										 .clickEditLink()
										 .setDefectTrackerIdSelect(dtName2 + " (" + dtType2 + ")")
										 .setUserNameInput(dtUserName)
										 .setPasswordInput(dtPassword)
										 .clickJsonLink()
										 .waitForJsonResult()
										 .setProjectListSelect(projectName)
										 .clickUpdateApplicationButtonPopup();
		
		assertTrue("The Defect Tracker was not added correctly.", 
				applicationDetailPage.getDefectTrackerText().equals(dtName2));
		
		// delete stuff
		loginPage = applicationDetailPage.clickDeleteLink()
										 .clickDeleteButton()
										 .clickConfigurationHeaderLink()
										 .clickDefectTrackersLink()
										 .clickTextLinkInDefectTrackerTableBody(dtName1)
										 .clickDeleteButton()
										 .clickTextLinkInDefectTrackerTableBody(dtName2)
										 .clickDeleteButton()
										 .logout();
	}
	
	@Test
	public void testAddBothAtApplicationCreationTimeAndDelete() {
		String dtUrl = DefectTrackerIndexPage.DT_URL;
		String orgName = "AppBothCreationTimeOrg";
		String appName = "AppBothCreationTimeApp";
		String appUrl = "http://testUrl3.com";
		String dtName = "DG Bugzilla 2373468";
		String dtType = "Bugzilla";
		String dtUserName = "mcollins@denimgroup.com";
		String dtPassword = "bugzilla";
		String projectName = "TestProduct";
		String wafName = "DG mod_security";
		String wafType = "mod_security";
		
		// create stuff
		applicationDetailPage = loginPage.login("user", "password")
										 .clickWafsHeaderLink()
										 .clickAddWafLink()
										 .setTypeSelect(wafType)
										 .setNameInput(wafName)
										 .clickAddWafButton()
										 .clickConfigurationHeaderLink()
										 .clickDefectTrackersLink()
										 .clickAddDefectTrackerLink()
										 .setDefectTrackerTypeSelect(dtType)
										 .setNameInput(dtName)
										 .setUrlInput(dtUrl)
										 .clickAddDefectTrackerButton()
										 .clickOrganizationHeaderLink()
										 .clickAddTeamButton()
										 .setNameInput(orgName)
										 .clickSubmitButtonValid()
										 .clickAddApplicationLink()
										 .setNameInput(appName)
										 .setUrlInput(appUrl)
										 .setDefectTrackerIdSelect(dtName + " (" + dtType + ")")
										 .setUserNameInput(dtUserName)
										 .setPasswordInput(dtPassword)
										 .clickJsonLink()
										 .waitForJsonResult()
										 .setProjectListSelect(projectName)
										 .setWafSelect(wafName)
										 .clickAddApplicationButton();
		
		assertTrue("The Defect Tracker was not added correctly.", 
				applicationDetailPage.getDefectTrackerText().equals(dtName));
		assertTrue("The WAF was not added correctly.", 
				applicationDetailPage.getWafText().equals(wafName));
		
		// delete stuff
		loginPage = applicationDetailPage.clickDeleteLink()
										 .clickDeleteButton()
										 .clickWafsHeaderLink()
										 .clickTextLinkInWafTableBody(wafName)
										 .clickDeleteButton()
										 .clickConfigurationHeaderLink()
										 .clickDefectTrackersLink()
										 .clickTextLinkInDefectTrackerTableBody(dtName)
										 .clickDeleteButton()
										 .logout();
	}
	
	@Test
	public void testApplicationAddPageDefectTrackerValidation() {
		String dtUrl = DefectTrackerIndexPage.DT_URL;
		String orgName = "AppDTValidationOrg56";
		String appName = "AppDTValidationApp56";
		String appUrl = "http://testUrl3.com";
		String dtName = "DG Bugzilla 21";
		String dtType = "Bugzilla";
		String dtUserName = "mcollins@denimgroup.com";
		String dtPassword = "bugzilla";
		String badUserName = "not a bugzilla user";
		String badPassword = " not the right password";
		
		// set up everything except the defect tracker
		applicationAddPage = loginPage.login("user", "password")
									 .clickConfigurationHeaderLink()
									 .clickDefectTrackersLink()
									 .clickAddDefectTrackerLink()
									 .setDefectTrackerTypeSelect(dtType)
									 .setNameInput(dtName)
									 .setUrlInput(dtUrl)
									 .clickAddDefectTrackerButton()
									 .clickOrganizationHeaderLink()
									 .clickAddTeamButton()
									 .setNameInput(orgName)
									 .clickSubmitButtonValid()
									 .clickAddApplicationLink()
									 .setNameInput(appName)
									 .setUrlInput(appUrl);
		
		// make sure the other fields are disabled
		assertFalse("DT username field was enabled with no tracker selected.", 
				applicationAddPage.isUserNameFieldEnabled());
		assertFalse("DT password field was enabled with no tracker selected.", 
				applicationAddPage.isPasswordFieldEnabled());
		assertFalse("DT product select was enabled with no tracker selected.", 
				applicationAddPage.isProductSelectEnabled());
		
		// pick a tracker and make sure the username and password are enabled
		applicationAddPage.setDefectTrackerIdSelect(dtName + " (" + dtType + ")");
		assertTrue("DT username field was not enabled when the tracker was selected.", 
				applicationAddPage.isUserNameFieldEnabled());
		assertTrue("DT password field was not enabled when the tracker was selected.", 
				applicationAddPage.isPasswordFieldEnabled());
		assertFalse("DT product select was enabled when the tracker was selected.", 
				applicationAddPage.isProductSelectEnabled());
		
		// select nothing again and make sure the fields are disabled
		applicationAddPage.setDefectTrackerIdSelect("<none>");
		assertFalse("DT username field was enabled with no tracker selected.", 
				applicationAddPage.isUserNameFieldEnabled());
		assertFalse("DT password field was enabled with no tracker selected.", 
				applicationAddPage.isPasswordFieldEnabled());
		assertFalse("DT product select was enabled with no tracker selected.", 
				applicationAddPage.isProductSelectEnabled());
		
		// check that the correct error message is returned 
		// and that the product select is not enabled
		String resultString = applicationAddPage.setDefectTrackerIdSelect(dtName + " (" + dtType + ")")
												  .setUserNameInput(badUserName)
												  .setPasswordInput(badPassword)
												  .clickJsonLink()
												  .getJsonResultText();
		assertTrue("DT JSON check returned the wrong message on error.", 
				resultString.equals("Invalid username / password combination"));
		assertFalse("DT product select was enabled after failed authentication.", 
				applicationAddPage.isProductSelectEnabled());
		
		// check that the product select is enabled on successful validation
		resultString = applicationAddPage.setUserNameInput(dtUserName)
										 .setPasswordInput(dtPassword)
										 .clickJsonLink()
										 .getSecondJsonResultText();
		assertTrue("DT JSON check returned the wrong message on success.", 
				resultString.equals("Connection successful"));
		assertTrue("DT product select was not enabled after authentication.", 
				applicationAddPage.isProductSelectEnabled());
		
		// we don't actually need to add the app because that's covered elsewhere
		// clean up
		loginPage = applicationAddPage.clickCancelLink()
									  .clickDeleteButton()
									  .clickConfigurationHeaderLink()
									  .clickDefectTrackersLink()
									  .clickTextLinkInDefectTrackerTableBody(dtName)
									  .clickDeleteButton()
									  .logout();
	}
	
	@Test
	public void testApplicationEditPageDefectTrackerValidation() {
		String dtUrl = DefectTrackerIndexPage.DT_URL;
		String orgName = "AppDTEditValidationOrg";
		String appName = "AppDTEditValidationApp";
		String appUrl = "http://testUrl3.com";
		String dtName = "DG Bugzilla 584383";
		String dtType = "Bugzilla";
		String dtUserName = "mcollins@denimgroup.com";
		String dtPassword = "bugzilla";
		String badUserName = "not a bugzilla user";
		String badPassword = " not the right password";
		
		// set up everything except the defect tracker
		applicationEditPage = loginPage.login("user", "password")
									 .clickConfigurationHeaderLink()
									 .clickDefectTrackersLink()
									 .clickAddDefectTrackerLink()
									 .setDefectTrackerTypeSelect(dtType)
									 .setNameInput(dtName)
									 .setUrlInput(dtUrl)
									 .clickAddDefectTrackerButton()
									 .clickOrganizationHeaderLink()
									 .clickAddTeamButton()
									 .setNameInput(orgName)
									 .clickSubmitButtonValid()
									 .clickAddApplicationLink()
									 .setNameInput(appName)
									 .setUrlInput(appUrl)
									 .clickAddApplicationButton()
									 .clickEditLink();
		
		// make sure the other fields are disabled
		assertFalse("DT username field was enabled with no tracker selected.", 
				applicationEditPage.isUserNameFieldEnabled());
		assertFalse("DT password field was enabled with no tracker selected.", 
				applicationEditPage.isPasswordFieldEnabled());
		assertFalse("DT product select was enabled with no tracker selected.", 
				applicationEditPage.isProductSelectEnabled());
		
		// pick a tracker and make sure the username and password are enabled
		applicationEditPage.setDefectTrackerIdSelect(dtName + " (" + dtType + ")");
		assertTrue("DT username field was not enabled when the tracker was selected.", 
				applicationEditPage.isUserNameFieldEnabled());
		assertTrue("DT password field was not enabled when the tracker was selected.", 
				applicationEditPage.isPasswordFieldEnabled());
		assertFalse("DT product select was enabled when the tracker was selected.", 
				applicationEditPage.isProductSelectEnabled());
		
		// select nothing again and make sure the fields are disabled
		applicationEditPage.setDefectTrackerIdSelect("<none>");
		assertFalse("DT username field was enabled with no tracker selected.", 
				applicationEditPage.isUserNameFieldEnabled());
		assertFalse("DT password field was enabled with no tracker selected.", 
				applicationEditPage.isPasswordFieldEnabled());
		assertFalse("DT product select was enabled with no tracker selected.", 
				applicationEditPage.isProductSelectEnabled());
		
		// check that the correct error message is returned 
		// and that the product select is not enabled
		String resultString = applicationEditPage.setDefectTrackerIdSelect(dtName + " (" + dtType + ")")
												  .setUserNameInput(badUserName)
												  .setPasswordInput(badPassword)
												  .clickJsonLink()
												  .getJsonResultText();
		assertTrue("DT JSON check returned the wrong message on error: " + resultString, 
				resultString.equals("Invalid username / password combination"));
		assertFalse("DT product select was enabled after failed authentication.", 
				applicationEditPage.isProductSelectEnabled());
		
		// check that the product select is enabled on successful validation
		resultString = applicationEditPage.setUserNameInput(dtUserName)
										 .setPasswordInput(dtPassword)
										 .clickJsonLink()
										 .getSecondJsonResultText();
		assertTrue("DT JSON check returned the wrong message on success.", 
				resultString.equals("Connection successful"));
		assertTrue("DT product select was not enabled after authentication.", 
				applicationEditPage.isProductSelectEnabled());
		
		// we don't actually need to add the app because that's covered elsewhere
		// clean up
		loginPage = applicationEditPage.clickCancelLink()
									  .clickDeleteLink()
									  .clickDeleteButton()
									  .clickConfigurationHeaderLink()
									  .clickDefectTrackersLink()
									  .clickTextLinkInDefectTrackerTableBody(dtName)
									  .clickDeleteButton()
									  .logout();
	}
	
	public void sleep(int num) {
		try {
			Thread.sleep(num);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
