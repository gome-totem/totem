package org.z.core.stocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

import org.z.global.interfaces.ModuleIntf;


public class ModuleNewStock implements ModuleIntf {
   private int PORT ;
	@Override
	public boolean init(boolean isReload) {
		        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		        EventLoopGroup workerGroup = new NioEventLoopGroup();
		        try {
		            ServerBootstrap b = new ServerBootstrap();
		            b.group(bossGroup, workerGroup)
		             .channel(NioServerSocketChannel.class)
		             .handler(new LoggingHandler(LogLevel.INFO))
		             .childHandler(new ChannelInitializer<SocketChannel>() {
		                 @Override
		                 public void initChannel(SocketChannel ch) throws Exception {
		                     ChannelPipeline p = ch.pipeline();
		                     p.addLast(
		                             new StringEncoder(CharsetUtil.UTF_8),
		                             new LineBasedFrameDecoder(8192),
		                             new StringDecoder(CharsetUtil.UTF_8),
		                             new ChunkedWriteHandler(),
		                             new FileServerHandler());
		                 }
		             });
		            ChannelFuture f = b.bind(PORT).sync();

		            // Wait until the server socket is closed.
		            f.channel().closeFuture().sync();
		        } catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
		            bossGroup.shutdownGracefully();
		            workerGroup.shutdownGracefully();
		        }
		return false;
	}

	@Override
	public void afterCreate(Object[] params) {
		 PORT =  (int)params[0];
	}

	@Override
	public void start(boolean isReload) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

}
