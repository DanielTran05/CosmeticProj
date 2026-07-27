import React from 'react';

const Footer = () => {
    return (
        <footer className="text-white pt-5 pb-3 mt-5" style={{ backgroundColor: '#FF9800' }}>
            <div className="container">
                <div className="row">
                    <div className="col-md-3">
                        <h6 className="text-dark fw-bold">HỖ TRỢ KHÁCH HÀNG</h6>
                        <p className="mb-1 text-dark fw-bold">Hotline: 1800 6324</p>
                        <p className="small">(Miễn phí, 08-22h kể cả T7, CN)</p>
                        <ul className="list-unstyled small lh-lg">
                            <li>Các câu hỏi thường gặp</li>
                            <li>Gửi yêu cầu hỗ trợ</li>
                            <li>Hướng dẫn đặt hàng</li>
                            <li>Phương thức thanh toán</li>
                            <li>Chính sách đổi trả</li>
                        </ul>
                    </div>
                    <div className="col-md-3">
                        <h6 className="fw-bold">VỀ IchiLa Beauté.VN</h6>
                        <ul className="list-unstyled small lh-lg">
                            <li>Giới thiệu IchiLa Beauté.vn</li>
                            <li>Chính sách bảo mật</li>
                            <li>Điều khoản sử dụng</li>
                            <li>Tuyển dụng</li>
                        </ul>
                    </div>
                    <div className="col-md-3">
                        <h6 className="fw-bold">HỢP TÁC & LIÊN KẾT</h6>
                        <ul className="list-unstyled small lh-lg">
                            <li>IchiLa Clinic & Spa</li>
                            <li>Mỹ phẩm chính hãng</li>
                        </ul>
                        <h6 className="fw-bold mt-4">THANH TOÁN</h6>
                        <div className="d-flex gap-2">
                            <span className="badge bg-light text-dark border">VISA</span>
                            <span className="badge bg-light text-dark border">ATM</span>
                        </div>
                    </div>
                    <div className="col-md-3">
                        <h6 className="fw-bold">CẬP NHẬT THÔNG TIN KHUYẾN MÃI</h6>
                        <div className="input-group mb-3">
                            <input type="text" className="form-control" placeholder="Email của bạn" />
                            <button className="btn btn-dark text-white" type="button">Đăng ký</button>
                        </div>
                    </div>
                </div>
            </div>
        </footer>
    );
};

export default Footer;