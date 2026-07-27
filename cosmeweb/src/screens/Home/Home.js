import { useEffect, useState } from "react";
import Apis, { endpoints } from '../../configs/Apis';

const Home = () => {
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadCategories = async () => {
            try {
                let res = await Apis.get(endpoints['categories']);
                
                console.log("Data danh mục từ API:", res.data);

                const categoryList = Array.isArray(res.data) ? res.data : (res.data.result || []);
                setCategories(categoryList);
            } catch (ex) {
                console.error("Lỗi tải danh mục:", ex);
            } finally {
                setLoading(false);
            }
        };
        
        loadCategories();
    }, []);

    if (loading) {
        return <div className="text-center mt-4">Đang tải danh mục...</div>;
    }

    return (
        <div className="container mt-4" style={{ backgroundColor: '#FFFDF9', paddingBottom: '20px' }}>
            
            {/* Section Danh Mục */}
            <div className="bg-white p-3 rounded mb-4 shadow-sm">
                <h4 className="text-dark mb-3 fw-bold">Danh mục</h4>
                
                <div className="d-flex gap-3 overflow-auto">
                    {categories && categories.length > 0 ? (
                        categories.map(cate => (
                            <div key={cate.id} className="text-center" style={{ minWidth: '100px', cursor: 'pointer' }}>
                                <img 
                                    src={cate.img || cate.avatar || 'https://via.placeholder.com/80'} 
                                    alt={cate.name} 
                                    className="img-thumbnail rounded"
                                    style={{ width: '80px', height: '80px', objectFit: 'cover' }}
                                />
                                <p className="small mt-2 text-secondary fw-semibold">{cate.name}</p>
                            </div>
                        ))
                    ) : (
                        <p className="text-muted small">Chưa có danh mục nào.</p>
                    )}
                </div>
            </div>

        </div>
    );
};

export default Home;