package com.codeoftheweb.salvo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController{

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    @RequestMapping("/games")
    public Map<String, Object> makeControllerDTO(Authentication authentication){
        Map<String, Object>     dto = new LinkedHashMap<>();
        if(isGuest(authentication)) {
            dto.put("player", "Guest");
        }else{
            dto.put("player", playerRepository.findByUserName(authentication.getName()).makePlayerDTO());
        }
        dto.put("games", gameRepository.findAll().stream().map(game -> game.makeGameDTO()).collect(Collectors.toList()));
        return dto;
    }

    @RequestMapping("/game_view/{gamePlayerId}")
    public ResponseEntity<Map<String, Object>> findGamePlayer(@PathVariable Long gamePlayerId, Authentication authentication) {
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);
        if(!gamePlayer.isPresent()) {
            return new ResponseEntity<>(makeMap("error", "No existe Player"), HttpStatus.UNAUTHORIZED);
        }else {
            if (gamePlayer.get().getPlayer().getId() != playerRepository.findByUserName(authentication.getName()).getId()) {
                return new ResponseEntity<>(makeMap("error", "Prohibido"), HttpStatus.UNAUTHORIZED);
            } else {
                if(gamePlayer.get().getGameState().equals("WON")){
                    scoreRepository.save(new Score(1.0F, LocalDateTime.now(), gamePlayer.get().getGame(), gamePlayer.get().getPlayer()));
                }
                if(gamePlayer.get().getGameState().equals("LOSE")){
                    scoreRepository.save(new Score(0.0F, LocalDateTime.now(), gamePlayer.get().getGame(), gamePlayer.get().getPlayer()));
                }
                if(gamePlayer.get().getGameState().equals("TIE")){
                    scoreRepository.save(new Score(0.5F, LocalDateTime.now(), gamePlayer.get().getGame(), gamePlayer.get().getPlayer()));
                }
                return new ResponseEntity<>(gamePlayer.get().makeGameViewDTO(), HttpStatus.ACCEPTED);
            }
        }
    }

    @PostMapping("/games")
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication){
        if(isGuest(authentication)){
            return new ResponseEntity<>(makeMap("error", "Inicie sesión"), HttpStatus.UNAUTHORIZED);
        }else{
            Game newGame = new Game(LocalDateTime.now());
            gameRepository.save(newGame);
            Player currentPlayer = playerRepository.findByUserName(authentication.getName());
            GamePlayer newGamePlayer = new GamePlayer(newGame, currentPlayer, LocalDateTime.now());
            gamePlayerRepository.save(newGamePlayer);
            return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
        }
    }

    @PostMapping("/game/{nn}/players")
    public ResponseEntity<Map<String, Object>> joinGame(Authentication authentication, @PathVariable Long nn){
        if(isGuest(authentication)){
            return new ResponseEntity<>(makeMap("error", "Inicie sesión"), HttpStatus.UNAUTHORIZED);
        }else{
            if(gameRepository.findById(nn).isEmpty()){
                return new ResponseEntity<>(makeMap("error", "No hay juegos"), HttpStatus.FORBIDDEN);
            }else{
                if(gameRepository.findById(nn).get().getGamePlayers().size() > 1){
                    return new ResponseEntity<>(makeMap("error", "Lleno"), HttpStatus.FORBIDDEN);
                }else{
                    if(gameRepository.findById(nn).get().getGamePlayers()
                            .stream().findFirst().get().getPlayer() == playerRepository.findByUserName(authentication.getName())){
                        return new ResponseEntity<>(makeMap("error", "Jugando"), HttpStatus.FORBIDDEN);
                    }else{
                        Player currentPlayer = playerRepository.findByUserName(authentication.getName());
                        GamePlayer newGamePlayer = new GamePlayer(gameRepository.findById(nn).get(), currentPlayer, LocalDateTime.now());
                        gamePlayerRepository.save(newGamePlayer);
                        return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
                    }
                }
            }
        }
    }

    @PostMapping("/players")
    public ResponseEntity<Map<String, Object>> createUser(@RequestParam String email, @RequestParam String password) {
        if (email.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "No existis"), HttpStatus.FORBIDDEN);
        }
        Player player = playerRepository.findByUserName(email);
        if (player != null) {
            return new ResponseEntity<>(makeMap("error", "Mail ocupado"), HttpStatus.FORBIDDEN);
        }
        Player newPlayer = playerRepository.save(new Player(email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(makeMap("Mail", newPlayer.getUserName()), HttpStatus.CREATED);
    }

    @GetMapping("/games/players/{gamePlayerId}/ships")
    public ResponseEntity<Map<String, Object>> getships(Authentication authentication, @PathVariable Long gamePlayerId){
        if(isGuest(authentication)){
            return new ResponseEntity<>(makeMap("error", "Inicie sesion"), HttpStatus.UNAUTHORIZED);
        }
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);
        if(gamePlayer.isEmpty()){
            return new ResponseEntity<>(makeMap("error", "No existe el gameplayer"), HttpStatus.FORBIDDEN);
        }
        if(playerRepository.findByUserName(authentication.getName()).getGamePlayers().stream()
                .noneMatch(gp -> gp.equals(gamePlayer.get()))){
            return new ResponseEntity<>(makeMap("error", "GP que no le corresponde"), HttpStatus.UNAUTHORIZED);
        }
        Optional<List<Ship>> ships = shipRepository.findByGamePlayer(gamePlayer.get());
        if(ships.isEmpty()){
            return new ResponseEntity<>(makeMap("error", "No ubicaste los barcos"), HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(makeMap("ships",gamePlayer.get()
                .getShips()
                .stream()
                .map(Ship::makeShipDTO).collect(Collectors.toList())), HttpStatus.ACCEPTED);
    }

    @PostMapping("/games/players/{gamePlayerId}/ships")
    public ResponseEntity<Map<String, Object>> saveShips(Authentication authentication, @PathVariable Long gamePlayerId, @RequestBody List<Ship> ships){
        if(isGuest(authentication)){
            return new ResponseEntity<>(makeMap("error", "Inicie sesión para colocar los barcos"), HttpStatus.UNAUTHORIZED);
        }
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);
        if(gamePlayer.isEmpty()){
            return new ResponseEntity<>(makeMap("error", "No existe el gameplayer"), HttpStatus.UNAUTHORIZED);
        }
        if(playerRepository.findByUserName(authentication.getName()).getGamePlayers().stream()
                .noneMatch(gp -> gp.equals(gamePlayer.get()))){
            return new ResponseEntity<>(makeMap("error", "GP que no le corresponde"), HttpStatus.UNAUTHORIZED);
        }
        if(ships.size() != 5){
            return new ResponseEntity<>(makeMap("error", "Solo 5 barcos"), HttpStatus.FORBIDDEN);
        }
        if(gamePlayer.get().getShips().size() != 0){
            return new ResponseEntity<>(makeMap("error", "Ya ubicaste los barcos"), HttpStatus.FORBIDDEN);
        }
        for(Ship newShip: ships){
            if(newShip.getType().equals("carrier") && newShip.getShipLocations().size() != 5){
                return new ResponseEntity<>(makeMap("error", "Carrier solo 5 casilleros"), HttpStatus.FORBIDDEN);
            }
            if(newShip.getType().equals("battleship") && newShip.getShipLocations().size() != 4){
                return new ResponseEntity<>(makeMap("error", "Battleship solo 4 casilleros"), HttpStatus.FORBIDDEN);
            }
            if(newShip.getType().equals("submarine") && newShip.getShipLocations().size() != 3){
                return new ResponseEntity<>(makeMap("error", "Submarine solo 3 casilleros"), HttpStatus.FORBIDDEN);
            }
            if(newShip.getType().equals("destroyer") && newShip.getShipLocations().size() != 3){
                return new ResponseEntity<>(makeMap("error", "Destroyer solo 3 casilleros"), HttpStatus.FORBIDDEN);
            }
            if(newShip.getType().equals("patrolboat") && newShip.getShipLocations().size() != 2){
                return new ResponseEntity<>(makeMap("error", "Patrolboat solo 2 casilleros"), HttpStatus.FORBIDDEN);
            }
        }
        for(Ship newShip: ships) {
            newShip.setGamePlayer(gamePlayer.get());
            shipRepository.save(newShip);
        }
        return new ResponseEntity<>(makeMap("OK", "Posiciones guardadas"), HttpStatus.CREATED);
    }

    @GetMapping("/games/players/{gamePlayerId}/salvoes")
    public ResponseEntity<Map<String, Object>> getSalvoes(Authentication authentication, @PathVariable Long gamePlayerId){
        if(isGuest(authentication)){
            return new ResponseEntity<>(makeMap("error", "Inicie sesion"), HttpStatus.UNAUTHORIZED);
        }
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);
        if(gamePlayer.isEmpty()){
            return new ResponseEntity<>(makeMap("error", "No existe un gameplayer con ese id."), HttpStatus.UNAUTHORIZED);
        }
        Player currentPlayer = playerRepository.findByUserName(authentication.getName());
        if(currentPlayer.getId() != gamePlayer.get().getPlayer().getId()){
            return new ResponseEntity<>(makeMap("error", "GP que no le corresponde"), HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(makeMap("salvoes",gamePlayer.get()
                .getSalvos()
                .stream()
                .map(Salvo::makeSalvoDTO).collect(Collectors.toList())), HttpStatus.ACCEPTED);
    }

    @PostMapping("/games/players/{gamePlayerId}/salvoes")
    public ResponseEntity<Map<String, Object>> saveSalvos(Authentication authentication, @PathVariable Long gamePlayerId, @RequestBody Salvo salvo){
        if(isGuest(authentication)){
            return new ResponseEntity<>(makeMap("error", "Inicie sesion"), HttpStatus.UNAUTHORIZED);
        }
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);
        if(gamePlayer.isEmpty()){
            return new ResponseEntity<>(makeMap("error", "No existe un gameplayer con ese id."), HttpStatus.UNAUTHORIZED);
        }
        Player currentPlayer = playerRepository.findByUserName(authentication.getName());
        if(currentPlayer.getId() != gamePlayer.get().getPlayer().getId()) {
            return new ResponseEntity<>(makeMap("error", "GP que no le corresponde"), HttpStatus.UNAUTHORIZED);
        }
        if(gamePlayer.get().getShips().size() != 5){
            return new ResponseEntity<>(makeMap("error", "Ubique 5 barcos"), HttpStatus.FORBIDDEN);
        }
        if(salvo.getSalvoLocations().size() < 1 || salvo.getSalvoLocations().size() > 5) {
            return new ResponseEntity<>(makeMap("error", "Debe enviar entre 1 y 5 bombas"), HttpStatus.FORBIDDEN);
        }
        Optional<GamePlayer> contrario = gamePlayer.get()
                .getGame()
                .getGamePlayers()
                .stream()
                .filter(gp -> gp != gamePlayer.get())
                .findFirst();
        if(contrario.isEmpty()){
            return new ResponseEntity<>(makeMap("error", "Waiiiit"), HttpStatus.FORBIDDEN);
        }
        if(contrario.get().getShips().size() != 5){
            return new ResponseEntity<>(makeMap("error", "aguarde la ubicacion del enemigo"), HttpStatus.FORBIDDEN);
        }
        if(gamePlayer.get().getId() < contrario.get().getId()){
            if(gamePlayer.get().getSalvos().size() == contrario.get().getSalvos().size()){
                int turn = (int)gamePlayer.get().getSalvos().stream().count();
                turn += 1;
                salvo.setTurn(turn);
                salvo.setGamePlayerSalvo(gamePlayer.get());
                salvoRepository.save(salvo);
                return new ResponseEntity<>(makeMap("OK", "Fuego cruzado"), HttpStatus.CREATED);
            }else{
                return new ResponseEntity<>(makeMap("error", "Espere fuego rival"), HttpStatus.FORBIDDEN);
            }
        }else{
            if(gamePlayer.get().getSalvos().size() < contrario.get().getSalvos().size()){
                int turn = (int)gamePlayer.get().getSalvos().stream().count();
                turn += 1;
                salvo.setTurn(turn);
                salvo.setGamePlayerSalvo(gamePlayer.get());
                salvoRepository.save(salvo);
                return new ResponseEntity<>(makeMap("OK", "Fuego cruzado"), HttpStatus.CREATED);
            }else{
                return new ResponseEntity<>(makeMap("error", "Espere fuego rival"), HttpStatus.FORBIDDEN);
            }
        }
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}