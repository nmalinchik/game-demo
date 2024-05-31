package com.example.game.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter class for converting the game board between its entity representation and JSON.
 */
@Converter
public class BoardConverter implements AttributeConverter<String[][], String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts the board to a JSON string for storage in the database.
     *
     * @param board the board to convert
     * @return the JSON string representation of the board
     */
    @Override
    public String convertToDatabaseColumn(String[][] board) {
        try {
            return objectMapper.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting board to JSON", e);
        }
    }

    /**
     * Converts the JSON string from the database back to the board.
     *
     * @param boardJson the JSON string representation of the board
     * @return the board as a 2D array
     */
    @Override
    public String[][] convertToEntityAttribute(String boardJson) {
        try {
            return objectMapper.readValue(boardJson, String[][].class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to board", e);
        }
    }
}
