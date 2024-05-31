package com.example.game.service;

import com.example.game.exception.EntityNotFoundException;
import com.example.game.exception.PlayerAlreadyJoinedException;
import com.example.game.exception.PlayerLimitExceededException;
import com.example.game.model.GameDto;
import com.example.game.model.entity.Game;
import com.example.game.model.entity.Player;
import com.example.game.model.enums.GameStatus;
import com.example.game.model.enums.PlayerSign;
import com.example.game.repos.PlayerRepository;
import com.example.game.service.kafka.KafkaSenderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private GameService gameService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private KafkaSenderService kafkaSenderService;

    @Mock
    private LockService lockService;

    @InjectMocks
    private PlayerService playerService;

    @Value(value = "${game.player.name}")
    private String playerName;

    @Value(value = "${game.player.sign}")
    private PlayerSign playerSign;

    @Test
    void init_whenPlayerExists_thenSetCurrentPlayer() {
        Player player = new Player();
        player.setName(playerName);
        player.setPlayerSign(playerSign);
        initPlayer(player);

        assertEquals(player, playerService.getCurrentPlayer());
    }

    @Test
    void init_whenPlayerDoesNotExist_thenCreateNewPlayer() {
        when(playerRepository.findByName(playerName)).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        playerService.init();

        Player currentPlayer = playerService.getCurrentPlayer();
        assertNotNull(currentPlayer);
        assertEquals(playerName, currentPlayer.getName());
        assertEquals(playerSign, currentPlayer.getPlayerSign());
    }

    @Test
    void findPlayerById_whenPlayerExists_thenReturnPlayer() {
        UUID playerId = UUID.randomUUID();
        Player player = new Player();
        player.setId(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        Player foundPlayer = playerService.findPlayerById(playerId);

        assertEquals(player, foundPlayer);
    }

    @Test
    void findPlayerById_whenPlayerDoesNotExist_thenThrowsException() {
        UUID playerId = UUID.randomUUID();

        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            playerService.findPlayerById(playerId);
        });

        assertEquals(String.format("Player with id: %s not found", playerId), exception.getMessage());
    }

    @Test
    void joinGame_whenGamePlayersIsEmpty_thenAddPlayerToGame() {
        UUID gameId = UUID.randomUUID();
        GameDto gameDto = new GameDto(gameId, GameStatus.NEW, UUID.randomUUID());
        Game game = new Game();
        game.setId(gameId);
        Player player = new Player();
        player.setId(UUID.randomUUID());
        player.setPlayerSign(PlayerSign.X);

        initPlayer(player);

        when(gameService.getGameById(gameId)).thenReturn(game);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(lockService).executeWithLock(eq("/joingame"), any(Runnable.class));

        playerService.joinGame(gameDto);

        assertThat(game.getPlayers()).hasSize(1);
        assertThat(player.getCurrentGame()).isEqualTo(game);

        verify(gameService).saveGame(game);
        verify(playerRepository).save(player);
        verify(kafkaSenderService, never()).sendGame(any(GameDto.class));
    }

    @Test
    void joinGame_whenGameHasOnePlayer_thenAddPlayerAndSendMessageToKafka() {
        Player oPlayer = new Player();
        oPlayer.setId(UUID.randomUUID());
        oPlayer.setPlayerSign(PlayerSign.O);
        UUID gameId = UUID.randomUUID();
        GameDto gameDto = new GameDto(gameId, GameStatus.NEW, UUID.randomUUID());
        Game game = new Game();
        game.setId(gameId);
        game.getPlayers().add(oPlayer);
        Player player = new Player();
        player.setId(UUID.randomUUID());
        player.setPlayerSign(PlayerSign.X);

        initPlayer(player);

        when(gameService.getGameById(gameId)).thenReturn(game);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(lockService).executeWithLock(eq("/joingame"), any(Runnable.class));

        playerService.joinGame(gameDto);

        verify(gameService).saveGame(game);
        verify(playerRepository).save(player);
        verify(kafkaSenderService).sendGame(any(GameDto.class));
    }

    @Test
    void joinGame_whenPlayerLimitExceeded_thenThrowsPlayerLimitExceededException() {
        Game game = new Game();
        game.getPlayers().add(new Player());
        game.getPlayers().add(new Player());
        Player player = new Player();

        initPlayer(player);

        UUID gameId = UUID.randomUUID();
        GameDto gameDto = new GameDto(gameId, GameStatus.NEW, UUID.randomUUID());

        when(gameService.getGameById(gameId)).thenReturn(game);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(lockService).executeWithLock(eq("/joingame"), any(Runnable.class));

        assertThrows(PlayerLimitExceededException.class, () -> {
            playerService.joinGame(gameDto);
        });
    }

    @Test
    void joinGame_whenPlayerAlreadyJoined_thenThrowsPlayerAlreadyJoinedException() {
        Game game = new Game();
        Player existingPlayer = new Player();
        existingPlayer.setPlayerSign(PlayerSign.X);
        game.getPlayers().add(existingPlayer);
        Player player = new Player();
        player.setPlayerSign(PlayerSign.X);

        initPlayer(player);

        UUID gameId = UUID.randomUUID();
        GameDto gameDto = new GameDto(gameId, GameStatus.NEW, UUID.randomUUID());

        when(gameService.getGameById(gameId)).thenReturn(game);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(lockService).executeWithLock(eq("/joingame"), any(Runnable.class));

        assertThrows(PlayerAlreadyJoinedException.class, () -> {
            playerService.joinGame(gameDto);
        });
    }

    @Test
    void joinGame_whenPlayerSignConflict_thenThrowsPlayerAlreadyJoinedException() {
        Game game = new Game();
        Player existingPlayer = new Player();
        existingPlayer.setPlayerSign(PlayerSign.X);
        game.getPlayers().add(existingPlayer);
        Player player = new Player();
        player.setPlayerSign(PlayerSign.X);

        initPlayer(player);

        UUID gameId = UUID.randomUUID();
        GameDto gameDto = new GameDto(gameId, GameStatus.NEW, UUID.randomUUID());

        when(gameService.getGameById(gameId)).thenReturn(game);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(lockService).executeWithLock(eq("/joingame"), any(Runnable.class));

        assertThrows(PlayerAlreadyJoinedException.class, () -> {
            playerService.joinGame(gameDto);
        });
    }

    private void initPlayer(Player player) {
        when(playerRepository.findByName(playerName)).thenReturn(Optional.of(player));
        playerService.init();
    }
}
