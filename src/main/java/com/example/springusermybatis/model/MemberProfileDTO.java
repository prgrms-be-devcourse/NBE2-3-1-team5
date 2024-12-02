package com.example.springusermybatis.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.apache.ibatis.type.Alias;

@Alias(value = "userProfile")
@Getter
public class MemberProfileDTO {
    @Schema(description = "사용자 이름", example = "username")
    private String name;

    @Schema(description = "사용자 이메일", example = "abcd@google.com")
    private String email;

    @Schema(description = "주소", example = "서울시 강남구")
    private String address;

    @Schema(description = "우편번호", example = "12345")
    private String zipcode;
}
