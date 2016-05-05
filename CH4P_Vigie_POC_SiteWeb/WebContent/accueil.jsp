<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="jstl_core" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta charset="utf-8" />
        <link rel="stylesheet" href="<jstl_core:url value="/inc/css/style.css"/>" />
        <title>CH4Process - Exploitation des données</title>
    </head>

    <body>

<div id="page">

<header>
    <div id="banniere">
    <img src="inc/images/logo.png" alt="CH4Process" id="image_top" />
    </div>
</header>
<article>
<form method="post" action="connexion">
            <fieldset>
                <legend>Connexion</legend>
                <p>Vous pouvez vous connecter via ce formulaire.</p>

                <label for="nom">Adresse email <span class="requis">*</span></label>
                <input type="email" id="email" name="email" value="<jstl_core:out value="${utilisateur.email}"/>" size="50" maxlength="60" />
                <span class="erreur">${form.erreurs['email'] }</span>
                <br />

                <label for="motdepasse">Mot de passe <span class="requis">*</span></label>
                <input type="password" id="motdepasse" name="motdepasse" value="" size="50" maxlength="20" />
                <span class="erreur">${form.erreurs['motdepasse'] }</span>
                <br />
                <br />

                <input type="submit" value="Connexion" class="sansLabel" />
                <br />
                <p class="${empty form.erreurs ? 'succes' : 'erreur'}">${form.resultat}</p>    
                <jstl_core:if test="${sessionScore.utilisateur != null }"> 
                	<p class="succes">Vous êtes connecté(e) avec l'adresse : ${sessionScope.utilisateur.email}</p>
                </jstl_core:if> 
            </fieldset>
        </form>
        </article>
    
 <section>
 
 	<article>
 		<h1>Bienvenue dans votre espace CH4Process ! </h1>
 		<p>Cet espace personnel est dédié à l'analyse des données collectées par la Vigie. Dans le bandeau de navigation, vous pouvez accéder à vos données avec le bouton "Relevés" ou nous contacter avec le bouton "Contact".</p>
		<p>Pour toute demande complémentaire sur les données ou pour toute analyse et conseil, n'hésitez pas à nous contacter ! </p>
 		
 		<h2>News</h2>
 		<p>14/03/2016 : Mise en place de l'espace personnel !</p>
 	</article>
 	
 	<aside>
				<h1>A propos de nous</h1>
				<p align="center"><img id="process_img" src="inc/images/industrie.png" alt="Process" /></p>
				<p>CH4process est une startup d'ingénierie spécialisée dans les
					services et produits liés au Biogas.</p>
				<p>Nos membres possèdent plusieurs années d'expérience dans le
					financement, la construction, la mise en service, l'audit et
					l'exploitation de centrales de cogénération et dans les
					méthaniseurs.</p>
				<p>Blabla défis, blablabla expérience à votre service.</p>
			</aside>


		


</section>
    
<footer>
	<p> Les images de ce site ne sont pas libres de droit. Sauf Max. Il est libre Max...</p>
</footer>

</div>
    </body>
</html>