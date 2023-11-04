package com.milkcow.tripai.member.controller;


import com.milkcow.tripai.global.dto.DataResponse;
import com.milkcow.tripai.global.dto.ResponseDto;
import com.milkcow.tripai.jwt.JwtService;
import com.milkcow.tripai.member.domain.Member;
import com.milkcow.tripai.member.dto.MemberSignupRequestDto;
import com.milkcow.tripai.member.dto.MemberUpdateRequestDto;
import com.milkcow.tripai.member.dto.MemberWithdrawRequestDto;
import com.milkcow.tripai.member.exception.MemberException;
import com.milkcow.tripai.member.result.MemberResult;
import com.milkcow.tripai.member.service.MemberService;
import com.milkcow.tripai.security.CustomUserDetails;
import com.milkcow.tripai.security.CustomUserDetailsService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    private final CustomUserDetailsService userDetailsService;

    private final JwtService jwtService;

    /**
     * Email을 통한 회원가입
     *
     * @param requestDto
     */
    @PostMapping("/signup/email")
    @Transactional
    public ResponseDto createByEmail(@RequestBody MemberSignupRequestDto requestDto) {

        memberService.emailSignUp(requestDto);
        return DataResponse.of(true, MemberResult.OK_SIGNUP);
    }


    /**
     * 회원 정보 수정
     *
     * @param userDetails
     * @param requestDto
     */
    @PostMapping("/users")
    @Transactional
    public ResponseDto updateMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MemberUpdateRequestDto requestDto) {

        /// TODO: 메서드 로직 중복 리펙터링
        if (requestDto.getPw() == null) {
            throw new MemberException(MemberResult.INVALID_INPUT);
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(
                userDetails.getUsername());
        Member foundMember = customUserDetails.getMember();

        memberService.updateNickname(foundMember.getId(), requestDto);
        return DataResponse.of(true, MemberResult.OK_NICKNAME_UPDATE);
    }

    @DeleteMapping("/users")
    @Transactional
    public ResponseDto withdrawMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MemberWithdrawRequestDto requestDto) throws Exception {

        if (requestDto.getPw() == null) {
            throw new MemberException(MemberResult.INVALID_INPUT);
        }
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(
                userDetails.getUsername());
        Member foundMember = customUserDetails.getMember();

        memberService.withdraw(foundMember.getId(), requestDto);
        return DataResponse.of(true, MemberResult.OK_WITHDRAW);
    }

    /**
     * 로그아웃 시 쿠키 만료 설정
     *
     * @param request
     * @param response
     */
    @GetMapping("/signout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        jwtService.expireRefreshToken(response, request);

    }

}
