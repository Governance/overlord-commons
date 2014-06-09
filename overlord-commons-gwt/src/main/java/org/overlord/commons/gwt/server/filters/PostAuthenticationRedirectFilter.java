package org.overlord.commons.gwt.server.filters;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * In certain containers, the IDP causes the browser to POST the SAML assertion to the SP (e.g. s-ramp-ui).
 * This POST is consumed, the user is authenticated, and the UI is loaded.  However, if the users attempts to
 * refresh that page, the browser will confirm that they wish to resend the POST data.
 * 
 * Protect against this by forcing a GET redirect following a POST to the SP.
 * 
 * @author Brett Meyer
 */
public class PostAuthenticationRedirectFilter implements Filter {

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		Enumeration<String> params = httpRequest.getParameterNames();
		if (httpRequest.getMethod().equalsIgnoreCase("POST")) {
			while (params.hasMoreElements()) {
				String param = params.nextElement();
				if (param.equalsIgnoreCase("SAMLResponse")) {
					String url = httpRequest.getRequestURL().toString();
					if (httpRequest.getQueryString() != null) {
						url += "?" + httpRequest.getQueryString();
					}
					((HttpServletResponse) response).sendRedirect(url);
				}
			}
		}
		chain.doFilter(request, response);
	}

	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
