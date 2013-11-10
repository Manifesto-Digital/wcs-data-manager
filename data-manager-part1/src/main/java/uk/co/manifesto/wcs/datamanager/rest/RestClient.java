package uk.co.manifesto.wcs.datamanager.rest;


import javax.ws.rs.core.MediaType;

import uk.co.manifesto.wcs.datamanager.rest.exception.RestConnectionException;

import com.fatwire.wem.sso.SSO;
import com.fatwire.wem.sso.SSOException;
import com.fatwire.wem.sso.SSOSession;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class RestClient {
	
	private Client client;
	private String baseUri;
	private String multiticket;
	private WebResource baseResource;
	private final String username;
	private final String password;

	public RestClient(String baseUri, String username, String password) throws RestConnectionException {
		this.client = new Client();
		this.baseUri = baseUri;
		this.baseResource = client.resource(baseUri + "REST");
		this.username = username;
		this.password = password;
		authenticate();
	}

	public ClientResponse get(String path)  {
		WebResource getResource = this.baseResource.path(getRelativePath(path));	
		Builder builder = getResource.accept(MediaType.APPLICATION_XML);
		return builder.get(ClientResponse.class);
	}
	
	public ClientResponse delete(String path) {
		WebResource getResource = this.baseResource.path(getRelativePath(path));
		Builder builder = getBuilder(getResource);
		return builder.delete(ClientResponse.class);
	}

	public <T> ClientResponse put(T bean, String path) {
		WebResource getResource = this.baseResource.path(getRelativePath(path));
		Builder builder = getBuilder(getResource);
		return builder.put(ClientResponse.class, bean);
	}
	
	public <T> ClientResponse post(T bean, String path) {
		WebResource getResource = this.baseResource.path(getRelativePath(path));
		Builder builder = getBuilder(getResource);
		return builder.post(ClientResponse.class, bean);
	}

	private Builder getBuilder(WebResource getResource) {
		Builder builder = getResource.accept(MediaType.APPLICATION_XML);
		builder = builder.header("Pragma", "auth-redirect=false");
		builder = builder.header("X-CSRF-Token", multiticket);
		return builder;
	}
	
	private String getRelativePath(String path) {
		if (path.startsWith(baseUri)) {
			path = path.replace(baseUri + "REST", "");
		}
		return path;
	}	
	
	private void authenticate() throws RestConnectionException {
		try {
			SSOSession ssoSession = SSO.getSSOSession(baseUri);
			multiticket = ssoSession.getMultiTicket(username, password);
			baseResource = baseResource.queryParam("multiticket", multiticket);
		} catch (SSOException e) {
			throw new RestConnectionException(e);
		}
	}
}
