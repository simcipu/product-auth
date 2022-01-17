package mongo.auth.service;

import mongo.auth.exception.DuplicateException;
import mongo.auth.security.Utenti;

import java.util.List;

public interface UtentiService
{
	public List<Utenti> SelTutti();

	public void Save(Utenti utente) throws DuplicateException;
	
	public void Delete(Utenti utente);

	public Utenti getUtente(String userId);
	
}
