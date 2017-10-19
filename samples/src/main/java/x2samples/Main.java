// Copyright (c) 2016-2017 Jae-jun Kang
// See the file LICENSE for details.

package x2samples;

import java.io.Console;

import x2java.*;
import x2java.flows.*;

public class Main {
    public static void main(String[] args) {
        Hub.instance()
            .attach(new SingleThreadFlow()
                .add(new HelloCase())
                .add(new OutputCase()));

        Hub.startup();
        try {
            //while (true) {
            //    try {
            //        Thread.sleep(1000);
            //    }
            //    catch (InterruptedException ie) { }
                new HelloReq().setName("world").post();
            //}
            /*
            Console console = System.console();
            if (console != null) {
                while (true) {
                    String message = console.readLine();
                    if (message.equals("quit")) {
                        break;
                    }
                    else {
                        new HelloReq().setName(message).post();
                    }
                }
            }
            */
        }
        finally {
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException ie) { }

            Hub.shutdown();
        }
    }
}

class HelloCase extends Case {
    @Override
    protected void setup() {
        bind(new HelloReq(), new Handler() {
            public void invoke(Event e) {
                HelloReq req = (HelloReq)e;
                new HelloResp()
                    .setGreeting(String.format("Hello, %s!", req.getName()))
                    .post();
            }
        });
    }
}

class OutputCase extends Case {
    @Override
    protected void setup() {
        bind(new HelloResp(), new Handler() {
            public void invoke(Event e) {
                System.out.println(e);
            }
        });
    }
}
