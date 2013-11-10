package uk.co.manifesto.wcs.datamanager.sample;

import java.io.File;

import uk.co.manifesto.wcs.datamanager.ExcelUserManager;
import uk.co.manifesto.wcs.datamanager.rest.RestClient;
import uk.co.manifesto.wcs.datamanager.rest.exception.RestConnectionException;

public class ExcelUserManagerRunner {
	public static void main(String[] args) throws RestConnectionException {

		ExcelUserManager userManager = new ExcelUserManager(new RestClient("http://localhost:9080/cs/", "fwadmin", "xceladmin"));
		userManager.createUsersFromSpreadsheet(new File("src/main/resources/TestUsers.xlsx"));
		
	}
}
