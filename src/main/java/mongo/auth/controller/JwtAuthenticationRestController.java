package mongo.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mongo.auth.exception.BindingException;
import mongo.auth.exception.DuplicateException;
import mongo.auth.security.JwtTokenUtil;
import mongo.auth.security.Utenti;
import mongo.auth.service.UtentiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;


@RestController
public class JwtAuthenticationRestController 
{

	@Value("${sicurezza.header}")
	private String tokenHeader;

	@Value("${product.username}")
	private String user;

	@Value("${product.password}")
	private String password;

	@Autowired
	private UtentiService utentiService;


	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private ResourceBundleMessageSource errMessage;

	@Autowired
	@Qualifier("customUserDetailsService")
	private UserDetailsService userDetailsService;
	
	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationRestController.class);

	@PostMapping(value = "${sicurezza.uri}")
	public ResponseEntity<JwtTokenResponse> createAuthenticationToken(@RequestBody JwtTokenRequest authenticationRequest)
			throws AuthenticationException 
	{
		logger.info("Autenticazione e Generazione Token");

		authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

		final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

		final String token = jwtTokenUtil.generateToken(userDetails);
		Utenti ut =utentiService.getUtente(authenticationRequest.getUsername());

		logger.warn(String.format("Token %s", token));
		JwtTokenResponse jwt =new JwtTokenResponse(token);
		jwt.setUtente(ut);

		return ResponseEntity.ok(jwt);
	}


	@GetMapping(value = "${sicurezza.uri}/userid/{userId}")
	public Utenti getUtente(@PathVariable("userId") String userId)
	{

		Utenti retVal = utentiService.getUtente(userId);

		return retVal;
	}

	@GetMapping(value = "${sicurezza.uri}/utenti")
	public List<Utenti> getUtenti()
	{

		List<Utenti> utentiList = utentiService.SelTutti();

		return utentiList;
	}


	@GetMapping(value = "${sicurezza.uri}/validate/{username}/{token}")
	public Boolean validateToken(@PathVariable("username") String username,@PathVariable("token") String token) throws Exception {
		if (token == null || token.length() < 7 || username==null)
		{
			throw new Exception("Token/username assente o non valido!");
		}
		 Boolean re=false;
	     re = jwtTokenUtil.validateToken(token,username);

		return re;
	}

	@RequestMapping(value = "${sicurezza.uri}", method = RequestMethod.GET)
	public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request) 
			throws Exception 
	{
		String authToken = request.getHeader(tokenHeader);
		
		if (authToken == null || authToken.length() < 7)
		{
			throw new Exception("Token assente o non valido!");
		}
		
		final String token = authToken.substring(7);
		
		if (jwtTokenUtil.canTokenBeRefreshed(token)) 
		{
			String refreshedToken = jwtTokenUtil.refreshToken(token);
			
			return ResponseEntity.ok(new JwtTokenResponse(refreshedToken));
		} 
		else 
		{
			return ResponseEntity.badRequest().body(null);
		}
	}

	@DeleteMapping(value = "${sicurezza.uri}/delete/{userId}")
	public ResponseEntity<?> deleteUser(@PathVariable("userId") String userId) {

		Utenti ut=utentiService.getUtente(userId);
		utentiService.Delete(ut);

		HttpHeaders headers = new HttpHeaders();
		ObjectMapper mapper = new ObjectMapper();

		headers.setContentType(MediaType.APPLICATION_JSON);

		ObjectNode responseNode = mapper.createObjectNode();

		responseNode.put("code", HttpStatus.OK.toString());
		responseNode.put("message", "delete Utente " + ut.getUserId()+ " Eseguita Con Successo");

		return new ResponseEntity<>(responseNode, headers, HttpStatus.OK);
	}

	@PostMapping(value = "${sicurezza.uri}/update/{change}")
	public ResponseEntity<?> updateUser(@Valid @RequestBody Utenti utenti,@PathVariable("change") Boolean change) {


	    utentiService.update(utenti);

		HttpHeaders headers = new HttpHeaders();
		ObjectMapper mapper = new ObjectMapper();

		headers.setContentType(MediaType.APPLICATION_JSON);

		ObjectNode responseNode = mapper.createObjectNode();

		responseNode.put("code", HttpStatus.OK.toString());
		responseNode.put("message", "update Utente " + utenti.getPassword()+ " Eseguita Con Successo");

		return new ResponseEntity<>(responseNode, headers, HttpStatus.OK);
	}

	@PostMapping(value = "${sicurezza.uri}/inserisci")
	public ResponseEntity<?> addNewUser(@Valid @RequestBody UserRequest userRequest,
										BindingResult bindingResult) throws BindingException, DuplicateException {

		if (bindingResult.hasErrors())
		{
			String MsgErr = errMessage.getMessage(bindingResult.getFieldError(), LocaleContextHolder.getLocale());

			throw new BindingException(MsgErr);
		}

		if(!(userRequest.getAdminPassword().equals(password) && userRequest.getAdminUser().equals(user))){
			throw new AuthenticationException("UTENTE NON ABILITATO");
		}

			String encodedPassword = passwordEncoder.encode(userRequest.getUtente().getPassword());
			userRequest.getUtente().setPassword(encodedPassword);
			utentiService.save(userRequest.getUtente());

			HttpHeaders headers = new HttpHeaders();
			ObjectMapper mapper = new ObjectMapper();

			headers.setContentType(MediaType.APPLICATION_JSON);

			ObjectNode responseNode = mapper.createObjectNode();

			responseNode.put("code", HttpStatus.OK.toString());
			responseNode.put("message", "Inserimento Utente " + userRequest.getUtente().getUserId() + " Eseguita Con Successo");

		return new ResponseEntity<>(responseNode, headers, HttpStatus.CREATED);
	}

	@ExceptionHandler({ AuthenticationException.class })
	public ResponseEntity<String> handleAuthenticationException(AuthenticationException e) 
	{
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
	}

	private void authenticate(String username, String password) 
	{
		Objects.requireNonNull(username);
		Objects.requireNonNull(password);

		try 
		{
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} 
		catch (DisabledException e) 
		{
			logger.warn("UTENTE DISABILITATO");
			throw new AuthenticationException("UTENTE DISABILITATO", e);
		} 
		catch (BadCredentialsException e) 
		{
			logger.warn("CREDENZIALI NON VALIDE");
			throw new AuthenticationException("CREDENZIALI NON VALIDE", e);
		}
	}
}
