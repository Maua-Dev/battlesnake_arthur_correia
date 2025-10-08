package com.mauadev.code;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.mauadev.code.entities.Board;
import com.mauadev.code.entities.Coordinate;
import com.mauadev.code.entities.GameState;
import com.mauadev.code.entities.Snake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    // Gson é uma biblioteca para converter objetos Java para JSON e vice-versa.
    private static final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        String path = request.getPath();
        Object responseBody = null;

        try {
            //Roteador para os diferentes endpoints da API BattleSnake
            switch (path) {
                case "/":
                    // Informações da sua cobra
                    responseBody = handleInfo();
                    break;
                case "/start":
                    // Lógica para o início do jogo
                    handleStart(request, context);
                    break;
                case "/move":
                    // Lógica para decidir o próximo movimento
                    responseBody = handleMove(request, context);
                    break;
                case "/end":
                    // Lógica para o fim do jogo
                    handleEnd(request, context);
                    break;
                default:
                    // Se a rota não for encontrada, retorna um erro 404
                    // Precisamos passar \ antes das aspas para não dar erro quando convertemos pra json
                    return response.withStatusCode(404).withBody("{\"error\": \"Path not found\"}");
            }

            // Configura a resposta de sucesso
            response.setStatusCode(200);
            response.setHeaders(Collections.singletonMap("Content-Type", "application/json"));
            if (responseBody != null) {
                // Converte o objeto de resposta para uma string JSON
                response.setBody(gson.toJson(responseBody));
            }

        } catch (Exception e) {
            // Em caso de erro em qualquer parte da lógica
            context.getLogger().log("ERROR: " + e.getMessage());
            response.setStatusCode(500);
            response.setBody(String.format("{\"error\": \"%s\"}", e.getMessage()));
        }

        return response;
    }

    /**
     * Responde ao endpoint / com as informações da sua cobra. 🐍
     */
    private Map<String, String> handleInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("apiversion", "1");
        info.put("author", "Leo");
        info.put("color", "#888888"); // Ex: Cinza
        info.put("head", "default");
        info.put("tail", "default");
        return info;
    }

    /**
     * Chamado no início de cada jogo. Não precisa retornar nada.
     */
    private void handleStart(APIGatewayProxyRequestEvent request, Context context) {
        // Você pode usar o corpo da requisição (request.getBody()) para obter o estado inicial do jogo.
        context.getLogger().log("Game Started!");
    }

    /**
     * Chamado a cada turno para decidir o movimento. 🕹️
     */
    private Map<String, String> handleMove(APIGatewayProxyRequestEvent request, Context context) {
        // AQUI VAI A LÓGICA DA SUA COBRA!
        // O corpo da requisição (request.getBody()) contém o estado atual do tabuleiro.
        // Você deve analisá-lo para tomar uma decisão inteligente.

        // Exemplo de um objeto tabuleiro:
        // {
        // "height": 11,
        // "width": 11,
        // "food": [
        //     {"x": 5, "y": 5},
        //     {"x": 9, "y": 0},
        //     {"x": 2, "y": 6}
        // ],
        // "hazards": [
        //     {"x": 0, "y": 0},
        //     {"x": 0, "y": 1},
        //     {"x": 0, "y": 2}
        // ],
        // "snakes": [
        //     {"id": "snake-one", ... },
        //     {"id": "snake-two", ... },
        //     {"id": "snake-three", ... }
        // ]
        // }

        // O tabuleiro deve ser acessado usando: 
        // Type mapType = new TypeToken<Map<String, String>>() {}.getType();
        // Map<String, String> body = gson.fromJson(response.getBody(), mapType);
        
        // Exemplo de lógica muito simples: sempre se mover para cima.
        // CUIDADO: Isso fará sua cobra bater na parede rapidamente!

        String requestBody = request.getBody();
        GameState gameState = gson.fromJson(requestBody, GameState.class);
        Board board = gameState.getBoard();
        Snake you = gameState.getYou();
        
        System.out.println("--- Turno: " + gameState.getTurn() + " ---");
        System.out.println("Minha vida: " + you.getHealth());
        System.out.println("Posição da minha cabeça: (" + you.getHead().getX() + ", " + you.getHead().getY() + ")");

        if (!board.getFood().isEmpty()) {
            Coordinate firstFood = board.getFood().get(0);
            System.out.println("Comida mais próxima em: (" + firstFood.getX() + ", " + firstFood.getY() + ")");
        }

        // Lógica do movimento (exemplo simples)
        // Agora que você tem o estado do jogo, pode implementar uma lógica mais inteligente aqui.
        Map<String, String> move = new HashMap<>();
        Coordinate cabeca = you.getHead();
        String direcao = null;
        List<Coordinate> corpo = you.getBody();

        BiPredicate<String, Coordinate> valido = (mov, head) -> {
        int nx = head.getX();
        int ny = head.getY();
    
        switch (mov) {
            case "up": ny++; break;
            case "down": ny--; break;
            case "left": nx--; break;
            case "right": nx++; break;
        }

        if (nx < 0 || nx >= board.getWidth() || ny < 0 || ny >= board.getHeight()){
            return false;
        }
        for (Coordinate c : corpo){
            if (nx == c.getX() && ny == c.getY()){
                return false;
            }
        }
        for (Snake s : board.getSnakes()) {
            if (s.getId().equals(you.getId())){
                continue;
            }
            for (Coordinate c : s.getBody()){
                if (nx == c.getX() && ny == c.getY()){
                    return false;
                }
            }
            Coordinate h = s.getHead();
            if (Math.abs(nx - h.getX()) <= 1 && Math.abs(ny - h.getY()) <= 1){
                return false;
            }
        }
        return true;
        };
        BiPredicate<String, Coordinate> valido2 = (mov, head) -> {
        int nx = head.getX();
        int ny = head.getY();
    
        switch (mov) {
            case "up": ny++; break;
            case "down": ny--; break;
            case "left": nx--; break;
            case "right": nx++; break;
        }

        if (nx < 0 || nx >= board.getWidth() || ny < 0 || ny >= board.getHeight()){
            return false;
        }
        for (Coordinate c : corpo){
            if (nx == c.getX() && ny == c.getY()){
                return false;
            }
        }
        for (Snake s : board.getSnakes()) {
            if (s.getId().equals(you.getId())){
                continue;
            }
            for (Coordinate c : s.getBody()){
                if (nx == c.getX() && ny == c.getY()){
                    return false;
                }
            }
            Coordinate h = s.getHead();
            if (Math.abs(nx - h.getX()) <= 1 && Math.abs(ny - h.getY()) <= 1){
                return false;
            }
        }
        return true;
        };
        Coordinate comida = board.getFood().stream()
        .min(Comparator.comparingInt(f -> Math.abs(f.getX() - cabeca.getX()) + Math.abs(f.getY() - cabeca.getY())))
        .orElse(null);
        List<String> prioridade = new ArrayList<>();
        List<String> semprioridade = new ArrayList<>();
        if (comida != null) {
            if (cabeca.getY() > comida.getY()) prioridade.add("down");
            if (cabeca.getX() < comida.getX()) prioridade.add("right");
            if (cabeca.getX() > comida.getX()) prioridade.add("left");
            if (cabeca.getY() < comida.getY()) prioridade.add("up");
        }
        if (comida != null) {
            if (cabeca.getY() > comida.getY()) semprioridade.add("up");
            if (cabeca.getX() < comida.getX()) semprioridade.add("left");
            if (cabeca.getX() > comida.getX()) semprioridade.add("right");
            if (cabeca.getY() < comida.getY()) semprioridade.add("down");
        }
        for (String m : prioridade) {
            if (valido.test(m, cabeca)) {
                direcao = m;
                break;
            }
        }
        if (direcao == null) {
            for (String m : semprioridade) {
                if (valido.test(m, cabeca)) {
                    direcao = m;
                    break;
                }
            }
        }
        if (direcao == null) {
            for (String m : semprioridade) {
                if (valido2.test(m, cabeca)) {
                    direcao = m;
                    break;
                }
            }
        }
        if (direcao == null){
            for (String m : semprioridade) {
                direcao = m;
            }
        }
        move.put("move", direcao);
        return move;
    }

    /**
     * Chamado no final de cada jogo. Não precisa retornar nada.
     */
    private void handleEnd(APIGatewayProxyRequestEvent request, Context context) {
        // Você pode analisar a requisição para saber se venceu ou perdeu.
        context.getLogger().log("Game Ended!");
    }
}