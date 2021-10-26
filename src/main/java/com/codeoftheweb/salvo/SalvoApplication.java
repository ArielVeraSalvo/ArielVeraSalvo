package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Arrays;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository,
									  GameRepository gameRepository,
									  GamePlayerRepository gamePlayerRepository,
									  ShipRepository shipRepository,
									  SalvoRepository salvoRepository,
									  ScoreRepository scoreRepository) {
		return (args) -> {
			Player player1 = new Player("j.bauer@ctu.gov", passwordEncoder().encode("24"));
			Player player2 = new Player("c.obrian@ctu.gov", passwordEncoder().encode("42"));
			Player player3 = new Player("kim_bauer@gmail.com", passwordEncoder().encode("kb"));
			Player player4 = new Player("t.almeida@ctu.gov", passwordEncoder().encode("mole"));
			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);
			playerRepository.save(player4);

			Game game1 = new Game(LocalDateTime.now());
			Game game2 = new Game(LocalDateTime.now().plusHours(1));
			Game game3 = new Game(LocalDateTime.now().plusHours(2));
			Game game4 = new Game(LocalDateTime.now().plusHours(3));
			Game game5 = new Game(LocalDateTime.now().plusHours(4));
			Game game6 = new Game(LocalDateTime.now().plusHours(5));
			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);
			gameRepository.save(game4);
			gameRepository.save(game5);
			gameRepository.save(game6);

			GamePlayer gamePlayers1 = new GamePlayer(game1, player1, LocalDateTime.now());
			GamePlayer gamePlayers2 = new GamePlayer(game1, player2, LocalDateTime.now());
			GamePlayer gamePlayers3 = new GamePlayer(game2, player1, LocalDateTime.now().plusHours(1));
			GamePlayer gamePlayers4 = new GamePlayer(game2, player2, LocalDateTime.now().plusHours(1));
			GamePlayer gamePlayers5 = new GamePlayer(game3, player2, LocalDateTime.now().plusHours(2));
			GamePlayer gamePlayers6 = new GamePlayer(game3, player3, LocalDateTime.now().plusHours(2));
			GamePlayer gamePlayers7 = new GamePlayer(game4, player1, LocalDateTime.now().plusHours(3));
			GamePlayer gamePlayers8 = new GamePlayer(game4, player3, LocalDateTime.now().plusHours(3));
			GamePlayer gamePlayers9 = new GamePlayer(game5, player3, LocalDateTime.now().plusHours(4));
			GamePlayer gamePlayers10 = new GamePlayer(game5, player1, LocalDateTime.now().plusHours(4));
			GamePlayer gamePlayers11 = new GamePlayer(game6, player4, LocalDateTime.now().plusHours(5));
			gamePlayerRepository.save(gamePlayers1);
			gamePlayerRepository.save(gamePlayers2);
			gamePlayerRepository.save(gamePlayers3);
			gamePlayerRepository.save(gamePlayers4);
			gamePlayerRepository.save(gamePlayers5);
			gamePlayerRepository.save(gamePlayers6);
			gamePlayerRepository.save(gamePlayers7);
			gamePlayerRepository.save(gamePlayers8);
			gamePlayerRepository.save(gamePlayers9);
			gamePlayerRepository.save(gamePlayers10);
			gamePlayerRepository.save(gamePlayers11);

			Ship ship1 = new Ship("destroyer", gamePlayers1, Arrays.asList("H1", "H2"));
			Ship ship2 = new Ship("patrolboat", gamePlayers1, Arrays.asList("H5", "H6", "H7"));
			Ship ship3 = new Ship("carrier", gamePlayers1, Arrays.asList("A1", "A2", "A3", "A4", "A5"));
			Ship ship4 = new Ship("battleship", gamePlayers1, Arrays.asList("B1", "B2", "B3", "B4"));
			Ship ship5 = new Ship("submarine", gamePlayers1, Arrays.asList("D1", "E1", "F1"));
			Ship ship6 = new Ship("destroyer", gamePlayers2, Arrays.asList("A1", "A2"));
			Ship ship7 = new Ship("patrolboat", gamePlayers2, Arrays.asList("C1", "C2", "C3"));
			Ship ship8 = new Ship("carrier", gamePlayers2, Arrays.asList("A7", "B7", "C7", "D7", "E7"));
			Ship ship9 = new Ship("battleship", gamePlayers2, Arrays.asList("F1", "F2", "F3", "F4"));
			Ship ship10 = new Ship("submarine", gamePlayers2, Arrays.asList("D1", "D2", "D3"));
			shipRepository.save(ship1);
			shipRepository.save(ship2);
			shipRepository.save(ship3);
			shipRepository.save(ship4);
			shipRepository.save(ship5);
			shipRepository.save(ship6);
			shipRepository.save(ship7);
			shipRepository.save(ship8);
			shipRepository.save(ship9);
			shipRepository.save(ship10);

			Salvo salvo1 = new Salvo(1, gamePlayers1, Arrays.asList("B1", "B2", "B3", "B4", "B5"));
			Salvo salvo2 = new Salvo(2, gamePlayers1, Arrays.asList("H1", "H2", "H5", "H6", "H7"));
			Salvo salvo3 = new Salvo(1, gamePlayers2, Arrays.asList("G1", "G2", "G3", "G4", "G5"));
			Salvo salvo4 = new Salvo(2, gamePlayers2, Arrays.asList("A1", "A2", "A3", "A4", "A5"));
			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);

			Score score1 = new Score(1.0F, LocalDateTime.now(), game1, player1);
			Score score2 = new Score(0.0F, LocalDateTime.now(), game1, player2);
		//	Score score3 = new Score(0.5F, LocalDateTime.now(), game2, player1);
		//	Score score4 = new Score(0.5F, LocalDateTime.now(), game2, player2);
			scoreRepository.save(score1);
			scoreRepository.save(score2);
		//	scoreRepository.save(score3);
		//	scoreRepository.save(score4);
		};
	}
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository playerRepository;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userName-> {
			Player player = playerRepository.findByUserName(userName);
			if (player != null) {
				return new User(player.getUserName(), player.getPassword(),
						AuthorityUtils.createAuthorityList("PLAYER"));
			} else {
				throw new UsernameNotFoundException("Unknown user: " + userName);
			}
		});
	}
}

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/api/login").permitAll()
				.antMatchers("/api/players").permitAll()
				.antMatchers("/web/**").permitAll()
				.antMatchers("/api/games").permitAll()
				.antMatchers("/h2-console/**").permitAll()
				.and().headers().frameOptions().disable()
				.and().csrf().ignoringAntMatchers("/h2-console/**")
				.and().cors().disable();

		http.authorizeRequests().
				antMatchers("/api/game_view/**").hasAuthority("PLAYER");
		http.formLogin()
				.usernameParameter("name")
				.passwordParameter("pwd")
				.loginPage("/api/login");

		http.logout().logoutUrl("/api/logout");

		// turn off checking for CSRF tokens
		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}
}