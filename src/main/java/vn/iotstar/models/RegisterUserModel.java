package vn.iotstar.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterUserModel {
    private String email;
    private String password;
    private String fullName;
    private String images; // tên file ảnh trong static/images, để trống thì dùng default.svg
}
