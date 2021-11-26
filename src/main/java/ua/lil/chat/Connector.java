package ua.lil.chat;

import com.google.common.collect.Sets;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import ua.lil.chat.listener.ListenerManager;
import ua.lil.chat.netty.NettyChannelInitializer;
import ua.lil.chat.protocol.AbstractPacket;

import java.util.HashSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Connector {

    private static boolean debug;
    private static Connector instance;
    private boolean enabled;
    private Bootstrap bootstrap;
    private EventLoopGroup group;
    private Channel channel;
    private ScheduledFuture reconnectScheduledFuture;
    private String name;
    private String host;
    private int port;
    private int playersCount;
    private final HashSet registedCommands = Sets.newHashSet();
    private final ListenerManager listenerManager = new ListenerManager();

    public static void setInstance(Connector instance) {
        if (instance == null) {
            throw new RuntimeException("Instance cannot be null!");
        }

        if (Connector.instance != null) {
            throw new RuntimeException("Instance already set!");
        }
        Connector.instance = instance;
    }

    public static void info(Object msg) {
        System.out.println("[CONNECTOR] " + msg);
    }

    public Connector(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
    }

    protected void start() {
        this.enabled = true;
        Connector.info("Registering listeners...");
        Connector.info("Creating NioEventLoopGroup...");
        this.group = Epoll.isAvailable() ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
        Connector.info("Creating Bootstrap...");
        this.bootstrap = (Bootstrap) ((Bootstrap) ((Bootstrap) new Bootstrap().group(this.group)).channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)).handler(new NettyChannelInitializer());
        Connector.info("Connecting to the core...");
        this.group.execute(() -> {
            if (!this.connect()) {
                this.reconnect();
            }
        });
    }

    protected boolean connect() {
        try {
            ChannelFutureListener listener = channelFuture -> {
                if (channelFuture.isSuccess()) {
                    this.channel = channelFuture.channel();
                    Connector.info("Connected to the Core [/" + this.host + ":" + this.port + "]");
                } else {
                    Connector.info("Could not connect to the Core [/" + this.host + ":" + this.port + "]: " + channelFuture.cause().getMessage());
                }
            };
            this.bootstrap.connect(this.host, this.port).addListener(listener).syncUninterruptibly();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public void reconnect() {
        if (this.enabled && !this.isConnected() && (this.reconnectScheduledFuture == null || this.reconnectScheduledFuture.isCancelled())) {
            this.reconnectScheduledFuture = this.group.scheduleWithFixedDelay(() -> {
                if (this.isConnected()) {
                    this.reconnectScheduledFuture.cancel(false);
                    return;
                }
                Connector.info("Reconnecting to the Core...");
                if (this.connect()) {
                    this.reconnectScheduledFuture.cancel(false);
                }
            }, 5L, 5L, TimeUnit.SECONDS);
        }
    }

    public void onDisconnect() {
        Connector.info("Lost connection to the Core... Waiting to reconnect...");
        this.playersCount = 0;
        this.registedCommands.clear();
        this.channel = null;
        this.reconnect();
    }

    public void stop() {
        this.enabled = false;
        if (this.reconnectScheduledFuture != null) {
            this.reconnectScheduledFuture.cancel(false);
        }
        Connector.info("Closing netty channel...");
        this.channel.close().syncUninterruptibly();
        Connector.info("Shutdowning NioEventLoopGroup...");
        this.group.shutdownGracefully();
        Connector.info("Closed!");
    }

    public boolean isConnected() {
        return this.channel != null;
    }

    public void sendPacket(AbstractPacket packet) {
        if (this.channel != null && this.channel.isActive()) {
            if (Connector.isDebug()) {
                Connector.info("PacketHandler has sending: " + packet.toString());
            }
            this.channel.writeAndFlush(packet);
        }
    }

    public static String getCoreName() {
        return Connector.instance.name;
    }

    public static int getCoreOnline() {
        return Connector.instance.playersCount;
    }

    public static void sendCorePacket(AbstractPacket packet) {
        instance.sendPacket(packet);
    }

    public static boolean isCoreConnected() {
        return instance.isConnected();
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        Connector.debug = debug;
    }

    public static Connector getInstance() {
        return instance;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlayersCount() {
        return this.playersCount;
    }

    public HashSet getRegistedCommands() {
        return this.registedCommands;
    }

    public ListenerManager getListenerManager() {
        return this.listenerManager;
    }
}

