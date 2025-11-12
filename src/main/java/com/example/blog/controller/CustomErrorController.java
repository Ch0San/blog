package com.example.blog.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 커스텀 에러 컨트롤러.
 *
 * HTTP 상태 코드에 따라 403/404/500 조각 뷰로 라우팅합니다.
 */
@Controller
public class CustomErrorController implements ErrorController {

    /**
     * 공용 에러 엔드포인트.
     *
     * @param request 서블릿 요청(상태 코드 조회)
     * @return 상태코드별 에러 뷰 이름
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                return "fragments/index_401";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "fragments/index_403";
            } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "fragments/index_404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "fragments/index_500";
            }
        }

        // 기본 에러 페이지 (500)
        return "fragments/index_500";
    }
}
