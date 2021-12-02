package io.github.mybatis.pal;


import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Properties;

/**
 * @author cause
 *
 * 限制拦截的type和方法
 */
@Intercepts(
        {
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
                @Signature(type = Executor.class, method = "queryCursor", args = {MappedStatement.class, Object.class, RowBounds.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        }
)
public class ConsumeTimeInterceptor implements Interceptor {

  public static Log log = LogFactory.getLog(ConsumeTimeInterceptor.class);

  long limitMilliSecond;

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    long start = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
    try {
      return invocation.proceed();
    } finally {
      try {
        long end = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        String methodName = invocation.getMethod().getName();
        long consumeTime = end - start;
        if (consumeTime > limitMilliSecond) {
          String logString;
          if ("update".equalsIgnoreCase(methodName)) {
            logString = "slow update consume milliSecond: ";
          } else {
            logString = "slow query consume milliSecond: ";
          }
          log.debug(logString + consumeTime);
        }
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }
  }

  /**
   * 设置插件自定义属性
   * @param properties
   */
  @Override
  public void setProperties(Properties properties) {
    String limitMilliSecond = properties.getProperty("limitMilliSecond");
    if (limitMilliSecond != null) {
      this.limitMilliSecond = Long.parseLong(limitMilliSecond);
    }
  }
}
