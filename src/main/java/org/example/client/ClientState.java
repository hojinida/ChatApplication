package org.example.client;

public class ClientState {
     private String nickname;
     private String clientId;
     private Long roomId;

    public ClientState(String clientId) {
        this.nickname = clientId; // 초기 닉네임을 IP:Port로 설정
        this.clientId = clientId;
        this.roomId = null;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getClientId() {
        return clientId;
    }
}
