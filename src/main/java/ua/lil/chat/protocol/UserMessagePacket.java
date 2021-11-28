package ua.lil.chat.protocol;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserMessagePacket extends AbstractPacket {

    String userName, message;

    public UserMessagePacket() {
        super((short) 1);
    }

    @Override
    protected void read(ByteBuf buf) {
        this.userName = UserMessagePacket.readString(buf);
        this.message = UserMessagePacket.readString(buf);
    }

    @Override
    protected void write(ByteBuf buf) {
        UserMessagePacket.writeString(buf, this.userName);
        UserMessagePacket.writeString(buf, this.message);
    }
}

