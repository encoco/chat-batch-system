import React, {useState, useEffect, useRef, useMemo} from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import '../css/ChatPage.css';
import webSocketService from "../../service/WebSocket.js";

const ChatPage = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const userRole = searchParams.get('role') || 'customer';

    const userId = useMemo(() => {
        const storedUserId = sessionStorage.getItem('userId');
        if (storedUserId) {
            return parseInt(storedUserId);
        }
        const newUserId = Math.floor(Math.random() * 10000) + 1;
        sessionStorage.setItem('userId', newUserId.toString());
        console.log('새 userId 생성:', newUserId);
        return newUserId;
    }, []);

    const [matchStatus, setMatchStatus] = useState('connecting');
    const [sessionId, setSessionId] = useState(null);
    const [messages, setMessages] = useState([]);
    const [inputMessage, setInputMessage] = useState('');
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        console.log('채팅 페이지 초기화, userId:', userId, 'role:', userRole);

        webSocketService.connect(userId, () => {
            console.log('연결 완료, 구독 설정 시작');

            // 먼저 구독 설정
            webSocketService.subscribeToMatchResult(userId, (notification) => {
                console.log('매칭 성공 알림 수신:', notification);
                setSessionId(notification.sessionId);
                setMatchStatus('matched');

                webSocketService.subscribeToChatRoom(notification.sessionId, (message) => {
                    setMessages(prev => [...prev, {
                        id: message.chatId || Date.now(),
                        text: message.message,
                        sender: message.userId === userId ? 'user' : message.userId === 0 ? 'system' : 'other',
                        timestamp: new Date(message.date)
                    }]);
                });
            });

            webSocketService.subscribeToWaiting(userId, () => {
                console.log('대기 상태 알림 수신');
                setMatchStatus('waiting');
            });

            // 구독 설정 후 잠시 기다린 다음 매칭 요청
            setTimeout(() => {
                console.log('매칭 요청 전송');
                webSocketService.requestMatch(userId, userRole.toUpperCase());
            }, 100);
        });
    }, []);

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    };

    const handleLeave = () => {
        if (sessionId) {
            webSocketService.leaveChat(sessionId, userId);
        }
        navigate('/');
    };

    if (matchStatus === 'waiting') {
        return (
            <div className="waiting-container">
                <h2>{userRole === 'customer' ? '상담원 연결 중...' : '고객 대기 중...'}</h2>
            </div>
        );
    }

    if (matchStatus === 'connecting') {
        return <div>연결 중...</div>;
    }

    const sendMessage = () => {
        if (inputMessage.trim() === '' || !sessionId) return;

        const message = {
            userId: userId,
            message: inputMessage,
            date: new Date().toISOString()
        };

        console.log('메시지 전송:', message);
        webSocketService.sendChatMessage(sessionId, message);
        setInputMessage('');
    };

    return (
        <div className="chat-container">
            <div className="chat-wrapper">
                <button className="back-btn" onClick={handleLeave}>
                    상담 종료
                </button>

                <div className="chat-header">
                    <h2 className="chat-title">
                        {userRole === 'customer' ? '고객 채팅' : '상담원 채팅'}
                    </h2>
                    <div className="connection-status status-connected">
                        연결됨 (세션: {sessionId})
                    </div>
                </div>

                <div className="chat-messages">
                    {messages.map((message) => (
                        <div
                            key={message.id}
                            className={`message ${
                                message.sender === 'user'
                                    ? 'message-sent'
                                    : message.sender === 'system'
                                        ? 'message-system'
                                        : 'message-received'
                            }`}
                        >
                            <div className="message-text">
                                {message.text}
                            </div>
                            <div className="message-time">
                                {message.timestamp.toLocaleTimeString()}
                            </div>
                        </div>
                    ))}
                    <div ref={messagesEndRef} />
                </div>

                <div className="chat-input-wrapper">
                    <input
                        type="text"
                        value={inputMessage}
                        onChange={(e) => setInputMessage(e.target.value)}
                        onKeyPress={handleKeyPress}
                        placeholder="메시지를 입력하세요..."
                        className="chat-input"
                    />
                    <button onClick={sendMessage} className="send-btn">
                        전송
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ChatPage;