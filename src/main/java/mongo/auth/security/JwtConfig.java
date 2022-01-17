package mongo.auth.security;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
public class JwtConfig
{
	@Value("${sicurezza.uri}")
	private String uri;
	@Value("${sicurezza.refresh}")
	private String refresh;
	@Value("${sicurezza.header}")
	private String header;
	@Value("${sicurezza.prefix}")
	private String prefix;
	@Value("${sicurezza.expiration}")
	private int expiration;
	@Value("${sicurezza.secret}")
	private String secret;
}
