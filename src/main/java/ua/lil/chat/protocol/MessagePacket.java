package ua.lil.chat.protocol;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessagePacket extends AbstractPacket {

    String message;

    public MessagePacket() {
        super((short) 1);
    }

    @Override
    protected void read(ByteBuf buf) {
        this.message = MessagePacket.readString(buf);
    }

    @Override
    protected void write(ByteBuf buf) {
        MessagePacket.writeString(buf, this.message);
    }
}

