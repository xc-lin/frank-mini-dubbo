package com.lxc.dubbo.core.protocol.netty;

import com.lxc.dubbo.core.domain.FrankMiniDubboProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class FrankMiniDubboCodec extends ByteToMessageCodec<FrankMiniDubboProtocol> {

    private final int magicNum = 19980120;

    /**
     * 读消息
     * @param channelHandlerContext
     * @param byteBuf
     * @param list
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int magic = byteBuf.readInt();
        if (magic != magicNum) {
            return;
        }
        int versionNum = byteBuf.readByte();
        int serializeType = byteBuf.readByte();
        int sequenceId = byteBuf.readInt();
        byteBuf.readByte();
        byteBuf.readByte();
        int length = byteBuf.readInt();
        byte[] contentBytes = new byte[length];
        byteBuf.readBytes(contentBytes);
        FrankMiniDubboProtocol frankMiniDubboProtocol = new FrankMiniDubboProtocol(sequenceId, length, serializeType, contentBytes);
        list.add(frankMiniDubboProtocol);
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, FrankMiniDubboProtocol protocol, ByteBuf byteBuf) throws Exception {
        // 4字节 写入4字节的魔数，判断数据包有效性
        byteBuf.writeInt(magicNum);
        // 1字节 版本号
        byteBuf.writeByte(1);
        // 1字节 暂定 0为json
        byteBuf.writeByte(protocol.getSerializeType());
        // 4字节 请求序号类型
        byteBuf.writeInt(protocol.getSequenceId());
        // 2字节 占用两字节
        byteBuf.writeByte(0xff);
        byteBuf.writeByte(0xff);
        // 4字节 内容长度
        byteBuf.writeInt(protocol.getLength());
        byteBuf.writeBytes(protocol.getContentBytes());

    }
}
