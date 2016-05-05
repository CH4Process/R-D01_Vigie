package com.ch4process.beans;

public class Utilisateur 
{
	private String email;
	private String nom;
	private String motdepasse;
	
	public void setEmail(String email)
	{
		this.email = email;
	}
	
	public String getEmail()
	{
		return email;
	}
	
	public void setNom(String nom)
	{
		this.nom = nom;
	}
	
	public String getNom()
	{
		return nom;
	}
	
	public void setMotDePasse(String motdepasse)
	{
		this.motdepasse = motdepasse;
	}
	
	public String getMotDePasse()
	{
		return motdepasse;
	}


}
