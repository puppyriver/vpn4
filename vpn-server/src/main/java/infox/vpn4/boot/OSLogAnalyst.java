package infox.vpn4.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Ronnie
 */

//@SpringBootApplication(scanBasePackages = {"infox.itmanager6.boot","infox.itmanager6.web.controller"})

//@SpringBootApplication(exclude=DispatcherServletAutoConfiguration.class,scanBasePackages = {"infox.itmanager6.boot"})
@SpringBootApplication(scanBasePackages = {"infox.vpn4.boot","infox.vpn4.modules.oslog"})
// 开启缓存请把下行取消注释
//@EnableCaching
public class OSLogAnalyst {

    public static void main(String[] args) {
        SpringApplication.run(OSLogAnalyst.class, args);
    }


}
