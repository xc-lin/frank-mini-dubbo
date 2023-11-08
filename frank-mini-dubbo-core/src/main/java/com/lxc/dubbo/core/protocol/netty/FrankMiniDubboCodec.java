package com.lxc.dubbo.core.protocol.netty;

import com.lxc.dubbo.core.domain.FrankMiniDubboProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * 内部协议的解析
 */
public class FrankMiniDubboCodec extends ByteToMessageCodec<FrankMiniDubboProtocol> {

    private final int magicNum = 19980120;

    /**
     * 读消息
     * 解析协议
     * @param channelHandlerContext
     * @param byteBuf
     * @param list
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 4字节，魔数，用来判断数据包是否有效
        int magic = byteBuf.readInt();
        if (magic != magicNum) {
            return;
        }
        // 1字节 版本号，暂时无用
        int versionNum = byteBuf.readByte();
        // 1字节 序列化方式
        int serializeType = byteBuf.readByte();
        // 4字节 请求序号，暂时无用，在具体数据中有个uuid使用它当作请求唯一标识，后面将会优化
        int sequenceId = byteBuf.readInt();
        // 2字节，字节补齐到2的n次方 读取多余的无用数据
        byteBuf.readByte();
        byteBuf.readByte();
        // 4字节，内容长度
        int length = byteBuf.readInt();
        byte[] contentBytes = new byte[length];
        // 实际内容
        byteBuf.readBytes(contentBytes);
        FrankMiniDubboProtocol frankMiniDubboProtocol = new FrankMiniDubboProtocol(sequenceId, length, serializeType, contentBytes);
        list.add(frankMiniDubboProtocol);
    }

    /**
     * 编码协议
     * @param channelHandlerContext
     * @param protocol
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, FrankMiniDubboProtocol protocol, ByteBuf byteBuf) throws Exception {
        // 4字节 写入4字节的魔数，判断数据包有效性
        byteBuf.writeInt(magicNum);
        // 1字节 版本号
        byteBuf.writeByte(1);
        // 1字节 序列化方式
        byteBuf.writeByte(protocol.getSerializeType());
        // 4字节 请求序号，暂时无用
        byteBuf.writeInt(protocol.getSequenceId());
        // 2字节 占用两字节
        byteBuf.writeByte(0xff);
        byteBuf.writeByte(0xff);
        // 4字节 内容长度
        byteBuf.writeInt(protocol.getLength());
        byteBuf.writeBytes(protocol.getContentBytes());
    }
}
