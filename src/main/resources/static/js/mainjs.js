$(document).ready(function () {

    // ===== Trang profile: hiển thị thông tin người dùng đăng nhập =====
    if ($('#profile').length) {
        var token = localStorage.getItem('token');
        if (!token) {
            window.location.href = '/login';
            return;
        }
        $.ajax({
            type: 'GET',
            url: '/users/me',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            beforeSend: function (xhr) {
                xhr.setRequestHeader('Authorization', 'Bearer ' + token);
            },
            success: function (data) {
                $('#profile').html(data.fullName + ' (' + data.email + ')');
                $('#images').attr('src', '/images/' + (data.images || 'default.svg'));
            },
            error: function () {
                alert('Sorry, you are not logged in (or token expired).');
                localStorage.removeItem('token');
                window.location.href = '/login';
            }
        });
    }

    // ===== Hàm Login =====
    $('#Login').click(function () {
        var email = document.getElementById('email').value;
        var password = document.getElementById('password').value;
        var basicInfo = JSON.stringify({
            email: email,
            password: password
        });
        $.ajax({
            type: 'POST',
            url: '/auth/login',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: basicInfo,
            success: function (data) {
                localStorage.setItem('token', data.token);
                window.location.href = '/user/profile';
            },
            error: function () {
                alert('Login Failed');
            }
        });
    });

    // ===== Hàm đăng xuất =====
    $('#logout').click(function () {
        localStorage.clear();
        window.location.href = '/login';
    });
});
