package com.lxc.dubbo.core.protocol.netty;

import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.core.domain.FrankMiniDubboMessage;
import com.lxc.dubbo.core.domain.Invocation;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.CharsetUtil;

import java.util.List;

public class FrankMiniDubboInvocationCodec extends ByteToMessageCodec<FrankMiniDubboMessage> {

    private final int magicNum = 19980120;


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, FrankMiniDubboMessage message, ByteBuf byteBuf) throws Exception {
        // 4字节 写入4字节的魔数，判断数据包有效性
        byteBuf.writeInt(magicNum);
        // 1字节 版本号
        byteBuf.writeByte(1);
        // 1字节 暂定 0为json
        byteBuf.writeByte(0);
        // 4字节 请求序号类型
        byteBuf.writeInt(message.getSequenceId());
        // 2字节 占用两字节
        byteBuf.writeByte(0xff);
        byteBuf.writeByte(0xff);
        // 4字节 内容长度
        byte[] invocationBytes = JSON.toJSONBytes(message.getInvocation());
        byteBuf.writeInt(invocationBytes.length);
        byteBuf.writeBytes(invocationBytes);

    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int magic = byteBuf.readInt();
        if (magic != magicNum) {
            return;
        }
        int versionNum = byteBuf.readByte();
        int protocol = byteBuf.readByte();
        int sequenceId = byteBuf.readInt();
        byte b = byteBuf.readByte();
        byte c = byteBuf.readByte();
        int length = byteBuf.readInt();
        ByteBuf contentByteBuf = byteBuf.readBytes(length);
        String jsonString = contentByteBuf.toString(CharsetUtil.UTF_8);
        contentByteBuf.release();
        Invocation invocation = JSON.parseObject(jsonString, Invocation.class);
        list.add(invocation);
    }
}
