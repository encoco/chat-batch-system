import {Client} from '@stomp/stompjs';

class WebSocketService {
    client;
    userId;

    constructor() {
        this.client = new Client({
            brokerURL: 'ws://localhost:8080/api/ws',
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });
    }

    connect(userId, callback) {
        this.userId = userId;
        this.client.onConnect = frame => {
            callback();
        };
        this.client.activate();
    }

    requestMatch(userId, role) {
        this.client.publish({
            destination: '/api/pub/match-request',
            body: JSON.stringify({ userId, role })
        });
    }

    subscribeToMatchResult(userId, callback) {
        const destination = `/user/${userId}/queue/match-result`;
        console.log('구독 시작:', destination);

        const subscription = this.client.subscribe(destination, message => {
            console.log('Raw message received:', message);
            console.log('Message body:', message.body);
            console.log('Message headers:', message.headers);

            try {
                const notification = JSON.parse(message.body);
                console.log('Parsed notification:', notification);
                callback(notification);
            } catch (error) {
                console.error('JSON 파싱 오류:', error);
            }
        });

        console.log('구독 객체:', subscription);
        return subscription;
    }

    subscribeToWaiting(userId, callback) {
        this.client.subscribe(`/user/${userId}/queue/waiting`, message => {
            callback(JSON.parse(message.body));
        });
    }

    subscribeToChatRoom(sessionId, callback) {
        this.client.subscribe(`/api/sub/chat/${sessionId}`, message => {
            callback(JSON.parse(message.body));
        });
    }

    sendChatMessage(sessionId, message) {
        const destination = `/api/pub/chat/${sessionId}`;
        console.log('메시지 전송:', destination, message);

        this.client.publish({
            destination: destination,
            body: JSON.stringify(message)
        });
    }

    leaveChat(sessionId, userId) {
        this.client.publish({
            destination: `/api/pub/leave/${sessionId}`,
            body: JSON.stringify({ userId })
        });
    }
}

const webSocketService = new WebSocketService();
export default webSocketService;