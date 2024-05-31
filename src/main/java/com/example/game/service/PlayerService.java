package com.example.game.service;

import com.example.game.exception.EntityNotFoundException;
import com.example.game.exception.PlayerAlreadyJoinedException;
import com.example.game.exception.PlayerLimitExceededException;
import com.example.game.exception.PlayerSignConflictException;
import com.example.game.model.GameDto;
import com.example.game.model.entity.Game;
import com.example.game.model.entity.Player;
import com.example.game.model.enums.GameStatus;
import com.example.game.model.enums.PlayerSign;
import com.example.game.repos.PlayerRepository;
import com.example.game.service.kafka.KafkaSenderService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.UUID;

/**
 * Service for managing player-related operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    @Value(value = "${game.player.name}")
    private String playerName;

    @Value(value = "${game.player.sign}")
    private PlayerSign playerSign;
    private static final String LOCK_PATH_JOIN_GAME = "/joingame";
    private static final String PLAYER_NOT_FOUND = "Player with id: %s not found";
    private static final String PLAYER_ALREADY_JOINED = "Player with the same sign already joined";
    private static final String PLAYER_SIGN_CONFLICT = "Player with the same sign already joined";
    private static final String PLAYER_LIMIT_EXCEEDED = "Already joined necessary amount of players";

    private final GameService gameService;
    private final PlayerRepository playerRepository;
    private final KafkaSenderService kafkaSenderService;
    private final LockService lockService;

    @Getter
    private Player currentPlayer;

    /**
     * Initializes the current player. If the player with the given name exists, it is set as the current player.
     * Otherwise, a new player will be created and saved in the repository.
     */
    @Transactional
    @PostConstruct
    public void init() {
        currentPlayer = playerRepository.findByName(playerName)
                .orElseGet(this::createNewPlayer);
    }

    /**
     * Finds a player by ID.
     *
     * @param id the player ID
     * @return the player entity
     * @throws EntityNotFoundException if the player is not found
     */
    public Player findPlayerById(UUID id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(PLAYER_NOT_FOUND, id)));
    }

    /**
     * Joins a game based on the provided GameDto.
     *
     * @param gameDto the game data transfer object
     */
    @Transactional
    public void joinGame(GameDto gameDto) {
        log.info("Joining game {}", gameDto);
        lockService.executeWithLock(LOCK_PATH_JOIN_GAME, () -> {
            Game currentGame = gameService.getGameById(gameDto.getId());
            addPlayerToGame(currentGame, currentPlayer, gameDto);
        });
    }

    private void addPlayerToGame(Game game, Player player, GameDto gameDto) {
        var players = game.getPlayers();
        if (CollectionUtils.isEmpty(players)) {
            addFirstPlayerToGame(game, player);
        } else if (players.size() == 1) {
            addSecondPlayerToGame(game, player, gameDto, players);
        } else {
            throw new PlayerLimitExceededException(PLAYER_LIMIT_EXCEEDED);
        }
    }

    private void addFirstPlayerToGame(Game game, Player player) {
        log.debug("Adding first player to game ID: {}", game.getId());
        game.getPlayers().add(player);
        gameService.saveGame(game);
        player.setCurrentGame(game);
        playerRepository.save(player);
    }

    private void addSecondPlayerToGame(Game game, Player player, GameDto gameDto, java.util.List<Player> players) {
        if (players.get(0).equals(player)) {
            throw new PlayerAlreadyJoinedException(PLAYER_ALREADY_JOINED);
        } else if (players.get(0).getPlayerSign().equals(player.getPlayerSign())) {
            throw new PlayerSignConflictException(PLAYER_SIGN_CONFLICT);
        } else {
            updateGame(game, player);
            updatePlayer(game, player);
            kafkaSenderService.sendGame(new GameDto(gameDto.getId(), game.getStatus(), player.getId()));
        }
    }

    private void updateGame(Game game, Player player) {
        log.debug("Updating game ID: {} with player ID: {}", game.getId(), player.getId());
        game.getPlayers().add(player);
        game.setLastPlayedPlayer(player);
        game.setStatus(GameStatus.IN_PROGRESS);
        gameService.saveGame(game);
    }

    private void updatePlayer(Game game, Player player) {
        log.debug("Updating player ID: {} with game ID: {}", player.getId(), game.getId());
        player.setCurrentGame(game);
        playerRepository.save(player);
    }

    private Player createNewPlayer() {
        log.debug("Creating new player with name: {} and sign: {}", playerName, playerSign);
        var newPlayer = new Player();
        newPlayer.setName(playerName);
        newPlayer.setPlayerSign(playerSign);
        return playerRepository.save(newPlayer);
    }
}
