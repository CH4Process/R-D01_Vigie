package com.ch4process.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class TestFilter implements Filter
{
	public static final String ACCES_PUBLIC     = "/accueil.jsp";
    public static final String ATT_SESSION_USER = "utilisateur";

	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)throws IOException, ServletException 
	{
		HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        HttpSession session = request.getSession();
        
        /* Non-filtrage des ressources statiques */
        String chemin = request.getRequestURI().substring( request.getContextPath().length() );
        if ( chemin.startsWith( "/inc" ) ) 
        {
        	chain.doFilter( request, response );
        	return;
        }


        if ( session.getAttribute( ATT_SESSION_USER ) == null ) 
        {
            /* Redirection vers la page publique */
            //response.sendRedirect( request.getContextPath() + ACCES_PUBLIC );
            req.getServletContext().getRequestDispatcher( ACCES_PUBLIC ).forward( request, response );
        } 
        else 
        {
            /* Affichage de la page restreinte */
            chain.doFilter( request, response );
        }
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}

}
