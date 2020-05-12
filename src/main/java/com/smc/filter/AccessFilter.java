package com.smc.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.smc.pojo.User;
import com.smc.service.FeignUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class AccessFilter extends ZuulFilter {

    @Value("${smc.enableAccessTokenCheck}")
    private boolean enableAccessTokenCheck;

    @Autowired
    private FeignUserService userService;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    private boolean needToCheckTokenForUrl(String requestUrl) {
        return !requestUrl.endsWith("/user/register")
                && !requestUrl.endsWith("/user/register-confirm")
                && !requestUrl.endsWith("/user/find-by-id")
                && !requestUrl.endsWith("/user/login");
    }

    @Override
    public Object run() {
        if (enableAccessTokenCheck) {
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            String requestUrl = request.getRequestURL().toString();
            System.out.printf("send %s request to %s\n", request.getMethod(), requestUrl);

            if (needToCheckTokenForUrl(requestUrl)) {
                String accessToken = request.getHeader("Access-Token");
                if (accessToken == null) {
                    System.err.println("access token is null.");
                    ctx.setSendZuulResponse(false);
                    ctx.setResponseStatusCode(401);
                    return null;
                }
                try {
                    DecodedJWT decodedJwt = JWT.decode(accessToken);
                    int userId = Integer.valueOf(decodedJwt.getAudience().get(0));
                    Date expireDate = decodedJwt.getExpiresAt();
                    if (System.currentTimeMillis() > expireDate.getTime()) {
                        throw new Exception("You access token has expired, please login.");
                    }
                    User user = userService.findUserById(userId);
                    String password = user.getPassword();
                    JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(password)).build();
                    jwtVerifier.verify(accessToken);
                } catch (JWTDecodeException j) {
                    throw new RuntimeException("Access token decode failed.");
                } catch (JWTVerificationException e) {
                    throw new RuntimeException("Access token verification failed.");
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                System.out.println("access token verified.");
            } else {
                System.out.println("access token check is not needed for this url.");
            }
        } else {
            System.out.println("access token skipped.");
        }
        return null;
    }
}
