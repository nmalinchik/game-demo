package com.example.game.mapper;

import com.example.game.model.GameDto;
import com.example.game.model.GameView;
import com.example.game.model.entity.Game;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GameMapper {
    GameMapper INSTANCE = Mappers.getMapper(GameMapper.class);

    @Mapping(source = "lastPlayedPlayer.id",target = "lastPlayedPlayerId")
    GameDto gameToGameDto(Game game);

    @Mapping(source = "winner.name",target = "winner")
    GameView gameToGameView(Game game);

}