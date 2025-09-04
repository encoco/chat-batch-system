import React from 'react';
import { useNavigate } from 'react-router-dom';
import './select.css';

const Select = () => {
    const navigate = useNavigate();

    const selectRole = (role) => {
        navigate(`/cs?role=${role}`);
    };

    return (
        <div className="main-container">
            <div className="main-content">
                <h1 className="main-title">🎧 고객 상담 시스템</h1>
                <p className="main-subtitle">역할을 선택해주세요</p>

                <div className="role-buttons">
                    <button
                        className="role-btn customer-btn"
                        onClick={() => selectRole('customer')}
                    >
                        <span className="role-icon">👤</span>
                        <span className="role-text">고객</span>
                    </button>

                    <button
                        className="role-btn agent-btn"
                        onClick={() => selectRole('agent')}
                    >
                        <span className="role-icon">🎧</span>
                        <span className="role-text">상담원</span>
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Select;