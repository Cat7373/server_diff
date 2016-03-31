package net.minecraft.server;

import java.io.IOException;

public class PacketPlayOutRemoveEntityEffect implements Packet<PacketListenerPlayOut> {

    private int a;
    private MobEffectList b;

    public PacketPlayOutRemoveEntityEffect() {}

    public PacketPlayOutRemoveEntityEffect(int i, MobEffectList mobeffectlist) {
        this.a = i;
        this.b = mobeffectlist;
    }

    public void a(PacketDataSerializer packetdataserializer) throws IOException {
        this.a = packetdataserializer.g();
        this.b = MobEffectList.fromId(packetdataserializer.readUnsignedByte());
    }

    public void b(PacketDataSerializer packetdataserializer) throws IOException {
        packetdataserializer.b(this.a);
        packetdataserializer.writeByte(MobEffectList.getId(this.b));
    }

    public void a(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.a(this);
    }
}
