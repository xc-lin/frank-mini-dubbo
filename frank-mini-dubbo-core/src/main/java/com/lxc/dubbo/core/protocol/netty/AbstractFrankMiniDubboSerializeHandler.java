package com.lxc.dubbo.core.protocol.netty;

import com.alibaba.fastjson.JSON;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.lxc.dubbo.core.domain.FrankMiniDubboBaseMessage;
import com.lxc.dubbo.core.domain.FrankMiniDubboProtocol;
import com.lxc.dubbo.core.domain.enums.SerializeTypeEnum;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

@Slf4j
public abstract class AbstractFrankMiniDubboSerializeHandler<T> extends ChannelDuplexHandler {

    Class<T> clazz;

    public AbstractFrankMiniDubboSerializeHandler(Class clazz) {
        this.clazz = clazz;
    }

    /**
     * 读消息
     * 反序列化消息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FrankMiniDubboProtocol)) {
            ctx.fireChannelRead(msg);
            return;
        }
        FrankMiniDubboProtocol frankMiniDubboProtocol = (FrankMiniDubboProtocol) msg;

        int serializeType = frankMiniDubboProtocol.getSerializeType();
        int sequenceId = frankMiniDubboProtocol.getSequenceId();
        int length = frankMiniDubboProtocol.getLength();
        byte[] contentBytes = frankMiniDubboProtocol.getContentBytes();
        T data;
        if (serializeType == SerializeTypeEnum.HESSIAN.getCode()) {
            ByteArrayInputStream in = new ByteArrayInputStream(contentBytes);
            Hessian2Input input = new Hessian2Input(in);
            data = (T) input.readObject();
        } else {
            String jsonString = new String(contentBytes, CharsetUtil.UTF_8);
            data = JSON.parseObject(jsonString, clazz);
        }
        ctx.fireChannelRead(data);

    }

    /**
     * 写消息
     * 序列化
     *
     * @param ctx     the {@link ChannelHandlerContext} for which the write operation is made
     * @param msg     the message to write
     * @param promise the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof FrankMiniDubboBaseMessage)) {
            ctx.write(msg, promise);
            return;
        }
        FrankMiniDubboBaseMessage frankMiniDubboBaseMessage = (FrankMiniDubboBaseMessage) msg;
        if (Objects.isNull(frankMiniDubboBaseMessage) || !Objects.equals(frankMiniDubboBaseMessage.getData().getClass(), clazz)) {
            ctx.write(msg, promise);
            return;
        }
        int serializeType = frankMiniDubboBaseMessage.getSerializeType();
        Object data = frankMiniDubboBaseMessage.getData();

        byte[] contentBytes;
        // 根据序列化协议，动态选择
        if (serializeType == SerializeTypeEnum.HESSIAN.getCode()) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Hessian2Output output = new Hessian2Output(os);
            output.writeObject(data);
            output.close();
            contentBytes = os.toByteArray();
        } else {
            contentBytes = JSON.toJSONBytes(data);
        }

        ctx.write(new FrankMiniDubboProtocol(frankMiniDubboBaseMessage.getSequenceId(), contentBytes.length, serializeType, contentBytes));
    }
}
