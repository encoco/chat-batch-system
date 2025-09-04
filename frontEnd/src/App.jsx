import './App.css'
import {Route, Routes} from 'react-router-dom';
import Select from './components/component/select.jsx';
import ChatPage from './components/component/chat.jsx';

function App() {
    return (
        <div className="App">
            <Routes>
                <Route path="/" element={<Select/>}/>
                <Route path="/cs" element={<ChatPage/>}/>
            </Routes>
        </div>
    );
}

export default App
