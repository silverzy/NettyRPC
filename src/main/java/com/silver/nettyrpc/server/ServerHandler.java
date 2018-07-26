package com.silver.nettyrpc.server;

import com.silver.nettyrpc.protocol.RpcRequest;
import com.silver.nettyrpc.protocol.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMember;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class ServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private final Map<String, Object> handlerMap;

    public ServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        ServerRPC.submit(new Runnable() {
            @Override
            public void run() {
                logger.debug("Receive request: " +request.getRequestId());
                RpcResponse response = new RpcResponse();
                response.setRequestId(request.getRequestId());
                try{
                    Object result = handle(request);
                    response.setResult(result);
                }catch (Throwable throwable){
                    response.setError(throwable.toString());
                    logger.debug("Server handle request error" + throwable);
                }
                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        logger.debug("Send response for request "+ request.getRequestId());
                    }
                });
            }
        });
    }

    /**
     * 获取RPC请求中的Class、Mthod、paramtertype、parameter等属性
     * 并调用Cglib进行反射调用方法，返回调用结果
     * @param request
     * @return
     * @throws InvocationTargetException
     */
    public Object handle(RpcRequest request) throws InvocationTargetException {
        String className = request.getClassName();
        Object serviceBean = handlerMap.get(className);

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterType = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        logger.debug(serviceClass.getName());
        logger.debug(methodName);
        for (Class<?> temp : parameterType) logger.debug(temp.getName());
        for (Object temp: parameters) logger.debug(temp.toString());

        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName,parameterType);

        return serviceFastMethod.invoke(serviceBean,parameters);

    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("server caught exception", cause);
        ctx.close();
    }
}
