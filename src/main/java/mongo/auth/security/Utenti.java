package mongo.auth.security;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "utenti")
public class Utenti 
{
	@Id
	private String id;
	private String userId;
	private String password;
	private String attivo;
	
	private List<String> ruoli;	
}
