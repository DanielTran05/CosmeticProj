import axios from "axios";
import cookies from 'react-cookies'

export const endpoints = {
    'products': '/products' 
}

export const authApis = () => {
    let token = cookies.load('token');

    return axios.create({
        baseURL: "http://localhost:8080/cosmeMgt/",
        headers: {
            'Authorization': token ? `Bearer ${token}` : ""
        }
    })
}

export default axios.create({
    baseURL: "http://localhost:8080/cosmeMgt/"
})