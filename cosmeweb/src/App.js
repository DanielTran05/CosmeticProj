import { BrowserRouter, Route, Routes } from "react-router-dom";
import Header from "./components/Header";
import Home from "./screens/Home/Home";
import Footer from "./components/Footer";
import 'bootstrap/dist/css/bootstrap.min.css';

const App = () => {
   return(
     <BrowserRouter>
       <Header/>
         <Routes>
           <Route path="/" element= {<Home/>} />
         </Routes>
       <Footer/>
     </BrowserRouter>
   )

 }

export default App;