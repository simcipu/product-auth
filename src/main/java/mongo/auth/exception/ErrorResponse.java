package mongo.auth.exception;

import lombok.Data;

import java.util.Date;

@Data
public class ErrorResponse 
{
	private Date data = new Date();
	private int codice;
	private String messaggio;
}
