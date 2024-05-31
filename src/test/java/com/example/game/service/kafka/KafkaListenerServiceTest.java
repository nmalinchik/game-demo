package com.example.game.service.kafka;

import com.example.game.model.GameDto;
import com.example.game.model.GameMove;
import com.example.game.model.enums.GameStatus;
import com.example.game.service.MoveApplierService;
import com.example.game.service.MoveMakerService;
import com.example.game.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@EmbeddedKafka
@ActiveProfiles("test")
class KafkaListenerServiceTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private MoveApplierService moveApplierService;

    @Mock
    private MoveMakerService moveMakerService;

    @InjectMocks
    private KafkaListenerService kafkaListenerService;

    @Captor
    private ArgumentCaptor<GameDto> gameDtoCaptor;

    @Captor
    private ArgumentCaptor<GameMove> gameMoveCaptor;

    private GameDto gameDto;
    private GameMove gameMove;

    @BeforeEach
    void setUp() {
        gameDto = new GameDto();
        gameDto.setStatus(GameStatus.NEW);
        gameMove = new GameMove();
    }

    @Test
    void listenGameTopic_whenGameDtoIsNull_logsWarning() {
        kafkaListenerService.listenGameTopic(null);

        verifyNoInteractions(playerService, moveMakerService);
    }

    @Test
    void listenGameTopic_whenGameStatusIsNew_callsJoinGame() {
        kafkaListenerService.listenGameTopic(gameDto);

        verify(playerService).joinGame(gameDtoCaptor.capture());
        assertEquals(gameDto, gameDtoCaptor.getValue());
    }

    @Test
    void listenGameTopic_whenGameStatusIsInProgress_callsMakeMove() {
        gameDto.setStatus(GameStatus.IN_PROGRESS);

        kafkaListenerService.listenGameTopic(gameDto);

        verify(moveMakerService).makeMove(gameDtoCaptor.capture());
        assertEquals(gameDto, gameDtoCaptor.getValue());
    }

    @Test
    void listenGameTopic_whenGameStatusIsUnknown_logsWarning() {
        gameDto.setStatus(GameStatus.FINISHED);

        kafkaListenerService.listenGameTopic(gameDto);

        verifyNoInteractions(playerService, moveMakerService);
    }

    @Test
    void listenGameMoviesTopic_whenGameMoveIsNull_logsWarning() {
        kafkaListenerService.listenGameMoviesTopic(null);

        verifyNoInteractions(moveApplierService);
    }

    @Test
    void listenGameMoviesTopic_whenGameMoveIsNotNull_callsApplyMove() {
        kafkaListenerService.listenGameMoviesTopic(gameMove);

        verify(moveApplierService).applyMove(gameMoveCaptor.capture());
        assertEquals(gameMove, gameMoveCaptor.getValue());
    }
}
