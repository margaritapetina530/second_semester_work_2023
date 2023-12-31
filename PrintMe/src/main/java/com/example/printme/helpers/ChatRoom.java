package com.example.printme.helpers;

import com.example.printme.server.ServerHandlerClient;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChatRoom {
    private String roomName;
    private List<ServerHandlerClient> clients;
    private Boolean zherebievka = false;

    private ServerHandlerClient arter;

    private String word = "";

    private ArrayList<Message> messagesInThisGame = new ArrayList<>();
    private ArrayList<CanvasState> canvasInThisGame = new ArrayList<>();
    public Integer getClientsCount(){
        return clients.size();
    }

    public ChatRoom(String roomName) throws IOException {
        this.roomName = roomName;
        this.clients = new ArrayList<>();
    }

    public void broadcastMessage(Message message) throws IOException {
        messagesInThisGame.add(message);
        if(arter!= null) {
            arter.sendMessage(message);
        }
        for (ServerHandlerClient client : clients) {
            // Не отправлять сообщение отправителю
            if (!client.getUsername().equals(message.userName)) {
                client.sendMessage(message);
            }
        }
        gameValidation(message);
    }
    public void broadcastCanvasState(CanvasState canvasState) throws IOException {
        canvasInThisGame.add(canvasState);
        for (ServerHandlerClient client : clients) {
            client.sendCanvasState(canvasState);
        }
    }
    public void broadcastRoles() throws IOException{
        if (arter != null) {
            arter.sentRole(word);
        }else{
            onGameStart();
        }
        for (ServerHandlerClient client:clients){
            client.sentRole("tell");
        }
    }


    public void addClient(ServerHandlerClient client) throws IOException {
        clients.add(client);
        if(!zherebievka && clients.size()==3){
            onGameStart();
        }else{
            client.sentRole("tell");
            for(Message message: messagesInThisGame){
                client.sendMessage(message);
            }
            for(CanvasState canvasState:canvasInThisGame){
                client.sendCanvasState(canvasState);
            }
        }
    }

    private void onGameStart() throws IOException {
        arter = zhrebi();
        zherebievka = true;
        clients.remove(arter);
        word = WordList.getRandomWord();
        canvasInThisGame = new ArrayList<>();
        broadcastRoles();
        Message message = new Message("starting game", "SERVER");
        broadcastMessage(message);
    }

    private void gameValidation(Message message) throws IOException {
        if(message.text.toLowerCase().equals(word)){
            System.out.println(message.text + " " + word);
            Message messagewinner = new Message( "winner", "SERVER");
            broadcastMessage(messagewinner);
            onGameEnd();
        }
    }

    public void onGameEnd() throws IOException {
        if (arter != null) {
            clients.add(arter);
        }
        arter = null;
        zherebievka = false;
        word = "";
        messagesInThisGame.clear();
        canvasInThisGame.clear();
        onGameStart();
    }
    private ServerHandlerClient zhrebi(){
        Random random = new Random();
        return clients.get(random.nextInt(clients.size() - 1));
    }

    public void removeClient(ServerHandlerClient client) {
        clients.remove(client);
    }

    public String getRoomName() {
        return roomName;
    }
}
