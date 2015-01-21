/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vanilla.java.tcp;

import net.openhft.affinity.AffinitySupport;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author peter.lawrey
 */
public class EchoServerMain {
    public static void main(String... args) throws IOException {
        int port = args.length < 1 ? EchoClientMain.PORT : Integer.parseInt(args[0]);
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(port));
        System.out.println("listening on " + ssc);
        while (true) {
            final SocketChannel socket = ssc.accept();
            socket.socket().setTcpNoDelay(true);
            socket.configureBlocking(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AffinitySupport.setAffinity(1 << 2L);
                    try {
                        System.out.println("Connected " + socket);
                        ByteBuffer bb = ByteBuffer.allocateDirect(64*1024);
                        while (socket.read(bb) >= 0) {
                            bb.flip();
                            if (socket.write(bb) < 0)
                                throw new EOFException();
                            if (bb.remaining() > 0)
                                bb.compact();
                            else
                                bb.clear();
                        }
                    } catch (IOException ignored) {
                    } finally {
                        System.out.println("... disconnected " + socket);
                        try {
                            socket.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }).start();
        }
    }
}