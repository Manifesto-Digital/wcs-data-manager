package uk.co.manifesto.wcs.datamanager.rest.sample;

import java.util.ArrayList;
import java.util.List;

import uk.co.manifesto.wcs.datamanager.rest.RestClient;
import uk.co.manifesto.wcs.datamanager.rest.exception.RestConnectionException;

import com.fatwire.rest.beans.User;
import com.fatwire.rest.beans.UserBean;
import com.fatwire.rest.beans.UserSite;
import com.fatwire.rest.beans.UsersBean;
import com.google.common.base.Joiner;
import com.sun.jersey.api.client.ClientResponse;

public class RestClientRunner {

	public static void main(String[] args) throws RestConnectionException {
		RestClient client = new RestClient("http://localhost:9080/cs/", "fwadmin","xceladmin");
		ClientResponse response = client.get("/users");
		if (response.getStatus() == 200) {
			UsersBean users = response.getEntity(UsersBean.class);
			for (User user : users.getUsers()) {
				ClientResponse userResponse = client.get(user.getHref());
				if (userResponse.getStatus() == 200) {
					UserBean userBean = userResponse.getEntity(UserBean.class);
					System.out.println(userBean.getName());
					System.out.println(userBean.getId());
					List<String> siteNames = new ArrayList<String>();
					for (UserSite userSite : userBean.getSites()) {
						siteNames.add(userSite.getSite() + "=>" + Joiner.on(",").join(userSite.getRoles()));
					}
					System.out.println(Joiner.on(" || ").join(siteNames));
					System.out.println(Joiner.on(",").join(userBean.getAcls()));
				}
			}
		}
	}

}
