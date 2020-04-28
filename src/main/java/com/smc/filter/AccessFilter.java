package com.smc.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;

public class AccessFilter extends ZuulFilter {

    @Value("${smc.enableAccessTokenCheck}")
    private boolean enableAccessTokenCheck;

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

    @Override
    public Object run() {
        if (enableAccessTokenCheck) {
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            System.out.printf("send %s request to %s\n", request.getMethod(), request.getRequestURL().toString());

            Object accessToken = request.getParameter("accessToken");
            if (accessToken == null) {
                System.err.println("access token is null.");
                ctx.setSendZuulResponse(false);
                ctx.setResponseStatusCode(401);
                return null;
            }
            System.out.println("access token verified.");
        } else {
            System.out.println("access token skipped.");
        }
        return null;
    }
}
