package mongo.auth.service;


import mongo.auth.exception.DuplicateException;
import mongo.auth.repository.UtentiRepository;
import mongo.auth.security.Utenti;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UtentiServiceImpl implements UtentiService
{

	@Autowired
	UtentiRepository utentiRepository;
	
	@Override
	public List<Utenti> SelTutti()
	{

		return utentiRepository.findAll();
	}


	@Override
	public void save(Utenti utente) throws DuplicateException {
		if(utentiRepository.findByUserId(utente.getUserId()).isPresent()){
			throw  new DuplicateException("userId già esistente");
		}else {
			utentiRepository.save(utente);
		}
	}

	@Override
	public void update(Utenti utente) {
		utentiRepository.save(utente);
	}

	@Override
	public Utenti getUtente(String userId){

		return utentiRepository.findByUserId(userId).get();
	}

	@Override
	public void Delete(Utenti utente)
	{
		utentiRepository.delete(utente);
	}

}
