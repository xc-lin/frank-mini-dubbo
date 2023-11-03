package com.lxc.dubbo.core.protocol.netty;

import com.alibaba.fastjson.JSON;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.lxc.dubbo.core.domain.FrankMiniDubboBaseMessage;
import com.lxc.dubbo.core.domain.enums.SerializeTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.CharsetUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class FrankMiniDubboBaseCodec<T extends FrankMiniDubboBaseMessage, P> extends ByteToMessageCodec<T> {

    private final int magicNum = 19980120;

    private Class<P> clazz;

    public FrankMiniDubboBaseCodec(Class<P> clazz) {
        this.clazz = clazz;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, T message, ByteBuf byteBuf) throws Exception {
        // 4字节 写入4字节的魔数，判断数据包有效性
        byteBuf.writeInt(magicNum);
        // 1字节 版本号
        byteBuf.writeByte(1);
        // 1字节 暂定 0为json
        byteBuf.writeByte(message.getSerializeType());
        // 4字节 请求序号类型
        byteBuf.writeInt(message.getSequenceId());
        // 2字节 占用两字节
        byteBuf.writeByte(0xff);
        byteBuf.writeByte(0xff);
        byte[] invocationBytes = new byte[]{};
        long startTime = System.currentTimeMillis();
        // 根据序列化协议，动态选择
        if (message.getSerializeType() == SerializeTypeEnum.HESSIAN.getCode()) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Hessian2Output output = new Hessian2Output(os);
            output.writeObject(message.getData());
            output.close();
            invocationBytes = os.toByteArray();
        } else {
            invocationBytes = JSON.toJSONBytes(message.getData());
        }
        long endTime = System.currentTimeMillis();
//        System.out.println("encode: " + (endTime - startTime));
        // 4字节 内容长度
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
        int serializeType = byteBuf.readByte();
        int sequenceId = byteBuf.readInt();
        byte b = byteBuf.readByte();
        byte c = byteBuf.readByte();
        int length = byteBuf.readInt();

        byte[] contentBytes = new byte[length];
        byteBuf.readBytes(contentBytes);
        P data = null;
        long startTime = System.currentTimeMillis();
        if (serializeType == SerializeTypeEnum.HESSIAN.getCode()) {
            ByteArrayInputStream in = new ByteArrayInputStream(contentBytes);
            Hessian2Input input = new Hessian2Input(in);
            data = (P) input.readObject();
        } else {
            String jsonString = new String(contentBytes, CharsetUtil.UTF_8);
            data = JSON.parseObject(jsonString, clazz);
        }
        long endTime = System.currentTimeMillis();
//        System.out.println("decode: " + (endTime - startTime));
        list.add(data);
    }
}
