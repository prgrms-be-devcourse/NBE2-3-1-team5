package com.example.coffee.service;

import com.example.coffee.common.ResultCode;
import com.example.coffee.common.jwt.JwtTokenProvider;
import com.example.coffee.model.user.CreateMemberDTO;
import com.example.coffee.model.user.LoginRequestDTO;
import com.example.coffee.repository.MemberMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.coffee.common.Result;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Service
public class AuthService {
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final String cookieName = "REFRESHTOKEN";

    public Result signup(CreateMemberDTO createMemberDTO) {
        try {
            if (memberMapper.isEmailExists(createMemberDTO.getEmail()) > 0) {
                return new Result(ResultCode.EMAIL_ALREADY_EXISTS);
            }
            // 현재 시간으로 설정
            LocalDateTime now = LocalDateTime.now();
            createMemberDTO.setCreatedAt(now);
            createMemberDTO.setUpdatedAt(now);
            String aPassword = passwordEncoder.encode(createMemberDTO.getPassword());
            createMemberDTO.setPassword(aPassword);
            memberMapper.signup(createMemberDTO);

            return new Result(ResultCode.SUCCESS, createMemberDTO);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new Result(ResultCode.FAIL_TO_SAVE_USER);
        }
    }

    public Result login(LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        try {
            CreateMemberDTO member = memberMapper.findEmail(loginRequestDTO.getEmail());
            if (member == null) {
                return new Result(ResultCode.NOT_EXISTS_USER);
            }

            if (!passwordEncoder.matches(loginRequestDTO.getPassword(), member.getPassword())) {
                return new Result(ResultCode.INVALID_USER_PASSWORD);
            }

            // 토큰 생성
            String token = jwtTokenProvider.createToken(member.getMemberId());
            String refreshToken = jwtTokenProvider.createRefreshToken(member.getMemberId());

            // Cookie에 refreshToken 값 추가
            Cookie cookie = new Cookie(cookieName, refreshToken);
            cookie.setMaxAge(604800);
            response.addCookie(cookie);

            // 암호화한 refreshToken 값을 DB에 넣기
            member.setRefreshToken(passwordEncoder.encode(refreshToken));
            memberMapper.modifyRefreshToken(member);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", Map.of(
                    "email", member.getEmail(),
                    "name", member.getName() != null ? member.getName() : "N/A",
                    "address", member.getAddress() != null ? member.getAddress() : "N/A",
                    "zipcode", member.getZipcode() != null ? member.getZipcode() : "N/A",
                    "createdAt", member.getCreatedAt() != null ? member.getCreatedAt().toString() : "N/A",
                    "updatedAt", member.getUpdatedAt() != null ? member.getUpdatedAt().toString() : "N/A"
            ));
            responseData.put("token", token);
            return new Result(ResultCode.SUCCESS, responseData);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(ResultCode.DB_ERROR);
        }
    }

    // refreshToken DB 값 Null AND 쿠키 값 삭제
    public Result logout(String token, HttpServletResponse response) {
        try{
            token = token.substring(7);
            long userId= jwtTokenProvider.getId(token);

            CreateMemberDTO member = memberMapper.userInfo(userId);
            if (member == null) {
                return new Result(ResultCode.NOT_EXISTS_USER);
            }
            // refreshToken 값 Null로 변경
            member.setRefreshToken(null);
            memberMapper.modifyRefreshToken(member);

            // Cookie에 refreshToken 값 추가
            Cookie cookie = new Cookie(cookieName, null);
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            return new Result(ResultCode.SUCCESS);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new Result(ResultCode.DB_ERROR);
        }
    }

    public Result getUserProfile(Long userId) {
        try {
            CreateMemberDTO member = memberMapper.userInfo(userId);
            if (member == null) {
                return new Result(ResultCode.NOT_EXISTS_USER);
            }
            return new Result(ResultCode.SUCCESS, member);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new Result(ResultCode.DB_ERROR);
        }
    }

    // refreshToken이 DB refreshToken과 일치하면 accessToken 재발급
    public Result reCreateToken(HttpServletRequest request) {
        try{
            Cookie[] cookies = request.getCookies();
            long memberId = 0;
            String refreshToken = null;
            // 쿠키(refreshToken)을 통해서 memberId / refreshToken 값 추출
            for (Cookie cookie : cookies){
                if(cookie.getName().equals(cookieName)){
                    memberId = jwtTokenProvider.getId(cookie.getValue());
                    refreshToken = cookie.getValue();
                }
            }

            if(memberId != 0){
                System.out.println("memberId : " + memberId);
                System.out.println("Request refreshToken : "+refreshToken);
                System.out.println("DB refreshToken : "+memberMapper.findRefreshToken(memberId));

                // 쿠키와 DB의 refreshToken 값이 같으면 accessToken 재발급
                if(passwordEncoder.matches(refreshToken,memberMapper.findRefreshToken(memberId))){
                    String token = jwtTokenProvider.createToken(memberId);
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("token", token);
                    return new Result(ResultCode.SUCCESS, responseData);
                } else {
                    return new Result(ResultCode.INVALID_TOKEN);
                }
            } else {
                return new Result(ResultCode.TOKEN_EXPIRED);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new Result(ResultCode.DB_ERROR);
        }
    }
}
