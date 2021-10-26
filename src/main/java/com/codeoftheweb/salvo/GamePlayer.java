package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    Set<Ship> ships;

    @OneToMany(mappedBy = "gamePlayerSalvo", fetch=FetchType.EAGER)
    @OrderBy
    Set<Salvo> salvos;

    private LocalDateTime joinDate;

    public GamePlayer() { }

    public GamePlayer(Game game, Player player, LocalDateTime joinDate) {
        this.game = game;
        this.player = player;
        this.joinDate = joinDate;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public void setShips(Set<Ship> ships) {
        this.ships = ships;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public Set<Salvo> getSalvos() {
        return salvos;
    }

    public void setSalvos(Set<Salvo> salvos) {
        this.salvos = salvos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Object> makeGamePlayerDTO(){
        Map<String, Object>     dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("player", this.getPlayer().makePlayerDTO());
        return dto;
    }

    public Map<String, Object> makeGameViewDTO(){
        Map<String, Object>     dto = new LinkedHashMap<>();
        dto.put("id", this.getGame().getId());
        dto.put("created", this.getGame().getCreationDate());
        dto.put("gameState", getGameState());
        dto.put("gamePlayers", this.getGame().getGamePlayers()
                .stream()
                .map(x -> x.makeGamePlayerDTO())
                .collect(toList()));
        dto.put("ships", this.getShips()
                .stream()
                .map(ship -> ship.makeShipDTO())
                .collect(toList()));
        dto.put("salvoes", this.getGame().getGamePlayers()
                .stream()
                .flatMap(gamePlayer -> gamePlayer.getSalvos()
                        .stream()
                        .map(salvo -> salvo.makeSalvoDTO()))
                .collect(toList()));
        dto.put("hits", this.makeHitsDTO(this));
        return dto;
    }

    public Map<String, Object> makeHitsDTO(GamePlayer gamePlayer){
        Map<String, Object>     dto = new LinkedHashMap<>();
        if(getOpponent(gamePlayer).isPresent()) {
            if(getOpponent(gamePlayer).get().getShips().size() != 0 && gamePlayer.getShips().size() != 0) {
                dto.put("self", getOpponent(gamePlayer).get().getSalvos().stream().map(salvo -> makeHitsSelfOpponentDTO(getOpponent(gamePlayer).get(), salvo)));
                dto.put("opponent", this.getSalvos().stream().map(salvo -> makeHitsSelfOpponentDTO(gamePlayer, salvo)));
                return dto;
            }
        }
        dto.put("self", new ArrayList<>());
        dto.put("opponent", new ArrayList<>());
        return dto;
    }

    public Optional<GamePlayer> getOpponent(GamePlayer gamePlayer){
        return gamePlayer
                .getGame()
                .getGamePlayers()
                .stream()
                .filter(gp -> !gp.equals(gamePlayer))
                .findFirst();
    }

    public Map<String, Object> makeHitsSelfOpponentDTO(GamePlayer gamePlayer, Salvo salvo){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn", salvo.getTurn());
        dto.put("hitLocations", this.getHitLocations(gamePlayer, salvo));
        dto.put("damages", this.getDamages(this.getHitLocations(gamePlayer, salvo), gamePlayer, salvo));
        dto.put("missed", salvo.getSalvoLocations().size() - this.getHitLocations(gamePlayer, salvo).size());
        return dto;
    }

    public List<String> getHitLocations(GamePlayer gamePlayer, Salvo salvo){
        List<String> hits = new ArrayList<>();
        List<List<String>> shipLocations = getOpponent(gamePlayer)
                .get()
                .getShips()
                .stream()
                .map(x -> x.getShipLocations())
                .collect(Collectors.toList());
        for(String salvoLocation : salvo.getSalvoLocations()){
            for(List<String> listaDeShipLocations : shipLocations){
                for(String location: listaDeShipLocations){
                    if(salvoLocation.equals(location)){
                        hits.add(salvoLocation);
                    }
                }
            }
        }
        return hits;
    }

    public Map<String, Object> getDamages(List<String> hits, GamePlayer gamePlayer, Salvo salvo){
        int carrierHits = 0;
        int battleshipHits = 0;
        int submarineHits = 0;
        int destroyerHits = 0;
        int patrolboatHits = 0;
        int carrier = 0;
        int battleship = 0;
        int submarine = 0;
        int destroyer = 0;
        int patrolboat = 0;
        Set<Ship> setDeShip = getOpponent(gamePlayer).get().getShips();
        for(Ship ship : setDeShip){
            for(String hit : hits) {
                if (ship.getShipLocations().contains(hit)) {
                    if (ship.getType().equals("destroyer")) {
                        destroyerHits++;
                    }
                    if (ship.getType().equals("patrolboat")) {
                        patrolboatHits++;
                    }
                    if (ship.getType().equals("carrier")) {
                        carrierHits++;
                    }
                    if (ship.getType().equals("battleship")) {
                        battleshipHits++;
                    }
                    if (ship.getType().equals("submarine")) {
                        submarineHits++;
                    }
                }
            }
        }
        List<List<Salvo>> allHits = new ArrayList<>();
        allHits.add(gamePlayer.getSalvos().stream().filter(sv -> sv.getTurn() <= salvo.getTurn()).collect(toList()));
        for(Ship ship : setDeShip) {
            for (List<Salvo> allHitsList : allHits) {
                for (Salvo hit : allHitsList) {
                    for(String hitLocation : hit.getSalvoLocations()){
                        if(ship.getShipLocations().contains(hitLocation)){
                            if (ship.getType().equals("destroyer")) {
                                destroyer++;
                            }
                            if (ship.getType().equals("patrolboat")) {
                                patrolboat++;
                            }
                            if (ship.getType().equals("carrier")) {
                                carrier++;
                            }
                            if (ship.getType().equals("battleship")) {
                                battleship++;
                            }
                            if (ship.getType().equals("submarine")) {
                                submarine++;
                            }
                        }
                    }
                }
            }
        }
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("carrierHits", carrierHits);
        dto.put("battleshipHits", battleshipHits);
        dto.put("submarineHits", submarineHits);
        dto.put("destroyerHits", destroyerHits);
        dto.put("patrolboatHits", patrolboatHits);
        dto.put("carrier", carrier);
        dto.put("battleship", battleship);
        dto.put("submarine", submarine);
        dto.put("destroyer", destroyer);
        dto.put("patrolboat", patrolboat);
        return dto;
    }

    public String getGameState(){
        String state = "";
        if(this.getShips().size() == 0){
            state = "PLACESHIPS";
            return state;
        }
        if(getOpponent(this).isEmpty()){
            state = "WAITINGFOROPP";
            return state;
        }
        GamePlayer gamePlayer1 = this.getGame().getGamePlayers().stream().min(Comparator.comparing(gp -> gp.getId())).get();
        GamePlayer gamePlayer2 = this.getGame().getGamePlayers().stream().max(Comparator.comparing(gp -> gp.getId())).get();
        if(getHundidos(this)){
            if(this.getSalvos().size() == getOpponent(this).get().getSalvos().size()){
                if(getHundidos(getOpponent(this).get())){
                    state = "TIE";
                    return state;

                }else{
                    state = "WON";
                    return state;
                }
            }
        }
        if(getHundidos(getOpponent(this).get())){
            if(this.getSalvos().size() == getOpponent(this).get().getSalvos().size()){
                state = "LOSE";
                return state;
            }
        }
        if(getOpponent(this).get().getShips().size() != 5){
            state = "WAIT";
            return state;
        }
        if(this == gamePlayer1) {
            if((getOpponent(this).get().getShips().size() == 5 && this.getShips().size() == 5) &&
                (this.getSalvos().size() == getOpponent(this).get().getSalvos().size())) {
                state = "PLAY";
                return state;
            }else{
                state = "WAIT";
                return state;
            }
        }
        if(this == gamePlayer2){
            if((getOpponent(this).get().getShips().size() == 5 && this.getShips().size() == 5) &&
                    (this.getSalvos().size() < getOpponent(this).get().getSalvos().size())) {
                state = "PLAY";
                return state;
            }else{
                state = "WAIT";
                return state;
            }
        }
        state = "UNDEFINED";
        return state;
    }

    private boolean getHundidos(GamePlayer gamePlayer){
        List<String> allSalvos = new ArrayList<>();
        List<String> allShipLocations = new ArrayList<>();
        List<String> allHits = new ArrayList<>();
        GamePlayer opponent = getOpponent(gamePlayer).get();
        allSalvos = gamePlayer.getSalvos()
                .stream()
                .flatMap(salvo -> salvo.getSalvoLocations()
                        .stream()).collect(toList());
        allShipLocations = opponent.getShips()
                .stream()
                .flatMap(ship -> ship.getShipLocations()
                        .stream()).collect(toList());
        for(String salvo : allSalvos){
            if(allShipLocations.contains(salvo)){
                allHits.add(salvo);
            }
        }
        if(allHits.size() == 17) {
            return true;
        }else{
            return false;
        }
    }

    public Optional<Score> getScore() {
        return this.getPlayer().getScore(this.getGame());
    }
}