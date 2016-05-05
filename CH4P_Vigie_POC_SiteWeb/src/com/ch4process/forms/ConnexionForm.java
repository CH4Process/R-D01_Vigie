package com.ch4process.forms;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import com.ch4process.beans.Utilisateur;

public final class ConnexionForm 
{
    private static final String CHAMP_EMAIL  = "email";
    private static final String CHAMP_PASS   = "motdepasse";

    private String              resultat;
    private Map<String, String> erreurs      = new HashMap<String, String>();

    public String getResultat() 
    {
        return resultat;
    }

    public Map<String, String> getErreurs() 
    {
        return erreurs;
    }

    public Utilisateur connecterUtilisateur( HttpServletRequest request ) 
    {
        /* R�cup�ration des champs du formulaire */
        String email = getValeurChamp( request, CHAMP_EMAIL );
        String motDePasse = getValeurChamp( request, CHAMP_PASS );

        Utilisateur utilisateur = new Utilisateur();

        /* Validation du champ email. */
        try 
        {
            validationEmail( email );
        } 
        catch ( Exception e ) 
        {
            setErreur( CHAMP_EMAIL, e.getMessage() );
        }
        utilisateur.setEmail( email );

        /* Validation du champ mot de passe. */
        try 
        {
            validationMotDePasse( motDePasse );
        } 
        catch ( Exception e ) 
        {
            setErreur( CHAMP_PASS, e.getMessage() );
        }
        utilisateur.setMotDePasse( motDePasse );
        
        
        if (!isValid(request, email, motDePasse))
        {
        	setErreur(CHAMP_EMAIL, "Utilisateur non r�f�renc�");
        }
        

        /* Initialisation du r�sultat global de la validation. */
        if ( erreurs.isEmpty() ) 
        {
            resultat = "Succ�s de la connexion.";
        } 
        else 
        {
            resultat = "�chec de la connexion.";
        }

        return utilisateur;
    }

    /**
     * Valide l'adresse email saisie.
     */
    private void validationEmail( String email ) throws Exception 
    {
        if ( email != null && !email.matches( "([^.@]+)(\\.[^.@]+)*@([^.@]+\\.)+([^.@]+)" ) ) 
        {
            throw new Exception( "Merci de saisir une adresse mail valide." );
        }
    }

    /**
     * Valide le mot de passe saisi.
     */
    private void validationMotDePasse( String motDePasse ) throws Exception 
    {
        if ( motDePasse != null ) 
        {
            if ( motDePasse.length() < 3 ) 
            {
                throw new Exception( "Le mot de passe doit contenir au moins 3 caract�res." );
            }
        } 
        else 
        {
            throw new Exception( "Merci de saisir votre mot de passe." );
        }
    }

    /*
     * Ajoute un message correspondant au champ sp�cifi� � la map des erreurs.
     */
    private void setErreur( String champ, String message ) 
    {
        erreurs.put( champ, message );
    }

    /*
     * M�thode utilitaire qui retourne null si un champ est vide, et son contenu
     * sinon.
     */
    private static String getValeurChamp( HttpServletRequest request, String nomChamp ) 
    {
        String valeur = request.getParameter( nomChamp );
        if ( valeur == null || valeur.trim().length() == 0 ) 
        {
            return null;
        } 
        else 
        {
            return valeur;
        }
    }
    
    private static boolean isValid(HttpServletRequest req, String login, String pass) 
    {
    	boolean connexion = false;
    	try 
    	{
    		Scanner sc = new Scanner(new File(req.getServletContext().getRealPath("/WEB-INF/config/acces.txt")));

    		while(sc.hasNext())
    		{
    			if(sc.nextLine().equals(login+"\t"+pass)){
    				connexion=true;
    				break;
    			}
    		}

    	} 
    	catch (FileNotFoundException e) 
    	{	
    		System.err.println("Le fichier n'existe pas !");
    	}
    	return connexion;

    }
}