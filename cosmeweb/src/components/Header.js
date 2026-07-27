import React from 'react';
import { Link } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';

const Header = () => {
    return (
        <header className="text-white" style={{ backgroundColor: '#FF9800' }}>
            <div className="container py-2 d-flex justify-content-between align-items-center">
                
                <Link to="/" className="text-white text-decoration-none h4 mb-0 fw-bold">
                    IchiLa Beauté.vn
                </Link>
                
                <div className="input-group w-50">
                    <input type="text" className="form-control" placeholder="Tìm kiếm sản phẩm..." />
                    <button className="btn btn-light" type="button"></button>
                </div>

                <div className="d-flex align-items-center gap-4">
                    <div className="d-flex flex-column align-items-center" style={{ cursor: 'pointer' }}>
                        <small>Đăng nhập / Đăng ký</small>
                        <strong>Tài khoản</strong>
                    </div>
                   
                    <div className="d-flex flex-column align-items-center">
                        <small>Hỗ trợ</small>
                        <strong>Khách hàng</strong>
                    </div>
                    
                    <div className="position-relative">
                        <Link to="/cart" className="text-white text-decoration-none">
                            Giỏ hàng
                            <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger text-white">
                                0
                            </span>
                        </Link>
                    </div>
                </div>
            </div>

            <div className="bg-light text-dark border-bottom">
                <div className="container d-flex gap-4 py-2 fw-semibold" style={{ fontSize: '14px' }}>
                    <span className="text-dark">☰ DANH MỤC</span>
                    <Link to="/brands" className="text-dark text-decoration-none">THƯƠNG HIỆU</Link>
                    <Link to="/best-sellers" className="text-dark text-decoration-none text-danger">BÁN CHẠY</Link>
                </div>
            </div>
        </header>
    );
};

export default Header;