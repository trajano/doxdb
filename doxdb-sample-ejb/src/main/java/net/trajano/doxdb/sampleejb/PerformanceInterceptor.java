package net.trajano.doxdb.sampleejb;

import javax.ejb.Stateless;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

@Stateless
public class PerformanceInterceptor {

    @AroundInvoke
    public Object intercept(final InvocationContext ctx) throws Exception {

        System.out.println("*** DefaultInterceptor intercepting " + ctx.getMethod().getName());
        try {
            return ctx.proceed();
        } finally {
            System.out.println("*** DefaultInterceptor exiting");
        }
    }
}
