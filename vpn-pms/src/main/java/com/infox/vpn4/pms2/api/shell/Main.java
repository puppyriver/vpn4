package com.infox.vpn4.pms2.api.shell;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/25
 * Time: 20:54
 * rongrong.chen@alcatel-sbell.com.cn
 */
import java.io.IOException;

import org.springframework.shell.Bootstrap;

/**
 * Driver class to run the helloworld example.
 *
 * @author Mark Pollack
 *
 */
public class Main {

    /**
     * Main class that delegates to Spring Shell's Bootstrap class in order to simplify debugging inside an IDE
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.out.println("你好");
        Bootstrap.main(args);

    }

}
