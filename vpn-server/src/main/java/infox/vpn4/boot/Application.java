package infox.vpn4.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Ronnie
 */

//@SpringBootApplication(scanBasePackages = {"infox.itmanager6.boot","infox.itmanager6.web.controller"})

//@SpringBootApplication(exclude=DispatcherServletAutoConfiguration.class,scanBasePackages = {"infox.itmanager6.boot"})
@SpringBootApplication(scanBasePackages = {"infox.itmanager6.boot"})
// 开启缓存请把下行取消注释
//@EnableCaching
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}
