package uk.co.manifesto.wcs.datamanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import uk.co.manifesto.wcs.datamanager.rest.RestClient;
import uk.co.manifesto.wcs.datamanager.rest.exception.RestConnectionException;

import com.fatwire.rest.beans.ErrorBean;
import com.fatwire.rest.beans.UserBean;
import com.fatwire.rest.beans.UserSite;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientResponse;


public class ExcelUserManager {
	
	private static final int USER_SITES = 3;
	private static final int ACLS = 2;
	private static final int PASSWORD = 1;
	private static final int NAME = 0;
	
	private Workbook workBook;
	private RestClient restClient;
	
	public ExcelUserManager(RestClient restClient) {
		this.restClient = restClient;
	}


	public void createUsersFromSpreadsheet(File spreadsheet) throws RestConnectionException {
		loadWorkbook(spreadsheet);
		for (UserBean user : getUsersFromWorkbook()) {
			ClientResponse userPut = restClient.put(user, "/users/"+user.getName());
			if (userPut.getStatus() == 200) {
				System.out.println(String.format("The user: %s was successfully added",user.getName()));
			} else if (userPut.getStatus() == 500){
				ErrorBean error = userPut.getEntity(ErrorBean.class);
				System.out.println(String.format("The user: %s could not be added because %s",user.getName(), error.getMessage()));
			}
		}
	}


	private List<UserBean> getUsersFromWorkbook() {
		List<UserBean> users = new ArrayList<UserBean>();
		Sheet sheet = workBook.getSheetAt(0);
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			
			String name = row.getCell(NAME).getStringCellValue();
			String password = row.getCell(PASSWORD).getStringCellValue();
			List<String> acls = getAcls(row.getCell(ACLS).getStringCellValue());
			List<UserSite> userSites = getUserSites(row.getCell(USER_SITES).getStringCellValue()); 
			
			UserBean newUser = buildUser(name, password, acls, userSites);
			users.add(newUser);
		}
		return users;
	}


	private UserBean buildUser(String name, String password, List<String> acls, List<UserSite> siteUsers) {
		UserBean newUser = new UserBean();
		newUser.setName(name);
		newUser.setPassword(password);
		newUser.getAcls().addAll(acls);
		newUser.getSites().addAll(siteUsers);
		return newUser;
	}

	private List<UserSite> getUserSites(String stringCellValue) {
		List<UserSite> userSitesToReturn = new ArrayList<UserSite>();
		String[] siteRole = stringCellValue.split("=>");
		UserSite userSite = new UserSite();
		userSite.setSite(siteRole[0]);
		userSite.getRoles().addAll(Lists.newArrayList(Splitter.on(',').split(siteRole[1])));
		userSitesToReturn.add(userSite);
		return userSitesToReturn;
	}


	private List<String> getAcls(String stringCellValue) {
		return Lists.newArrayList(Splitter.on(',').split(stringCellValue));
	}


	private void loadWorkbook(File spreadsheet) {
		try {
			OPCPackage pkg = OPCPackage.open(spreadsheet);
			workBook = new XSSFWorkbook(pkg);
			pkg.close();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
