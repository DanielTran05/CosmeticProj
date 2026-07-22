import { useEffect, useState } from "react";
import Apis, { endpoints } from "../configs/Apis";

const Header = () => {
    const [products, setProducts] = useState([]);

    const loadProducts = async () => {
        try {
            let res = await Apis.get(endpoints['products']);
            
            if (res.data && res.data.result && res.data.result.content) {
                setProducts(res.data.result.content);
            }
        } catch (error) {
            console.error("Lỗi khi tải danh sách sản phẩm:", error);
        }
    }

    useEffect(() => {
        loadProducts();
    }, []);

    return (
        <>
            <h1>MY HEADER</h1>
            <ul>
                {Array.isArray(products) && products.map((p, index) => (
                    <li key={p.id || index}>
                        {p.name} - Mức giá: {p.basePrice}
                    </li>
                ))}
            </ul>
        </>
    );
}

export default Header;