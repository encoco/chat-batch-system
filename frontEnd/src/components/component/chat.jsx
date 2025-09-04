import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import './ChatPage.css';

const ChatPage = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const userRole = searchParams.get('role') || 'customer';

    const [messages, setMessages] = useState([]);
    const [inputMessage, setInputMessage] = useState('');
    const [connectionStatus, setConnectionStatus] = useState('connecting');
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    useEffect(() => {
        // 초기 메시지 설정
        if (userRole === 'customer') {
            setMessages([
                {
                    id: 1,
                    text: "안녕하세요! 고객센터입니다. 무엇을 도와드릴까요?",
                    sender: 'system',
                    timestamp: new Date()
                }
            ]);

            // 연결 상태 시뮬레이션
            setTimeout(() => {
                setConnectionStatus('connected');
            }, 2000);
        } else {
            setMessages([
                {
                    id: 1,
                    text: "상담원 모드입니다. 고객의 문의를 기다리고 있습니다.",
                    sender: 'system',
                    timestamp: new Date()
                }
            ]);
            setConnectionStatus('waiting');
        }
    }, [userRole]);

    const sendMessage = () => {
        if (inputMessage.trim() === '') return;

        const newMessage = {
            id: messages.length + 1,
            text: inputMessage,
            sender: 'user',
            timestamp: new Date()
        };

        setMessages(prev => [...prev, newMessage]);
        setInputMessage('');

        // 간단한 자동 응답 시뮬레이션 (고객일 때만)
        if (userRole === 'customer') {
            setTimeout(() => {
                const autoReply = {
                    id: messages.length + 2,
                    text: "네, 확인해보겠습니다. 잠시만 기다려주세요.",
                    sender: 'agent',
                    timestamp: new Date()
                };
                setMessages(prev => [...prev, autoReply]);
            }, 1000);
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    };

    const goBack = () => {
        navigate('/');
    };

    const getStatusText = () => {
        switch (connectionStatus) {
            case 'connecting':
                return userRole === 'customer' ? '상담원 연결 중...' : '시스템 연결 중...';
            case 'connected':
                return '상담원 연결됨';
            case 'waiting':
                return '고객 대기 중';
            default:
                return '';
        }
    };

    const getStatusClass = () => {
        switch (connectionStatus) {
            case 'connecting':
                return 'status-connecting';
            case 'connected':
                return 'status-connected';
            case 'waiting':
                return 'status-waiting';
            default:
                return '';
        }
    };

    return (
        <div className="chat-container">
            <div className="chat-wrapper">
                <button className="back-btn" onClick={goBack}>
                    ← 뒤로가기
                </button>

                <div className="chat-header">
                    <h2 className="chat-title">
                        {userRole === 'customer' ? '👤 고객 채팅' : '🎧 상담원 채팅'}
                    </h2>
                    <div className={`connection-status ${getStatusClass()}`}>
                        {getStatusText()}
                    </div>
                </div>

                <div className="chat-messages" id="chatMessages">
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