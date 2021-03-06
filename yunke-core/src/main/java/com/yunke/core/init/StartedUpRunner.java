package com.yunke.core.init;

import com.yunke.common.core.util.SystemUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author chachae
 * @since 2020/04/25
 */
@Component
@RequiredArgsConstructor
public class StartedUpRunner implements ApplicationRunner {

  private final ConfigurableApplicationContext context;
  private final Environment environment;

  @Override
  public void run(ApplicationArguments args) {
    if (context.isActive()) {
      SystemUtil.printServerUpBanner(environment);
    }
  }
}
