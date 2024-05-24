package org.blab.vcas.consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blab.vcas.BrokerNotAvailableException;
import org.blab.vcas.MessageFormatException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class ByteChannel {
  private static final int RECONNECT_TIMEOUT = 3000;
  private static final int MAX_MESSAGE_SIZE = 2048;

  private static final Logger logger = LogManager.getLogger(ByteChannel.class);

  private final AsynchronousSocketChannel channel;
  private final BlockingQueue<byte[]> pendingWrites;
  private final ReadResultHandler readResultHandler;

  protected ByteChannel() {
    try {
      channel = AsynchronousSocketChannel.open();
      pendingWrites = new LinkedBlockingQueue<>();
      readResultHandler = new ReadResultHandler();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract void onMessage(byte[] msg);

  protected abstract void onChannelConnected();

  protected abstract void onChannelDisconnected();

  protected abstract void onChannelError(ByteChannelException e);

  protected void start(InetSocketAddress address) {
    channel.connect(address, address, new ConnectResultHandler());
  }

  protected void write(byte[] msg) {
    pendingWrites.add(msg);
  }

  protected void close() {
    try {
      channel.close();
    } catch (IOException e) {
      onChannelError(new ByteChannelException(e));
    }
  }

  private class ConnectResultHandler implements CompletionHandler<Void, InetSocketAddress> {
    @Override
    public void completed(Void unused, InetSocketAddress inetSocketAddress) {
      onChannelConnected();

      channel.write(ByteBuffer.wrap(new byte[0]), inetSocketAddress, new WriteResultHandler());
      channel.read(readResultHandler.buffer.clear(), inetSocketAddress, readResultHandler);
    }

    @Override
    public void failed(Throwable throwable, InetSocketAddress inetSocketAddress) {
      onChannelDisconnected();
      onChannelError(new ByteChannelException(new BrokerNotAvailableException(throwable)));

      try {
        Thread.sleep(RECONNECT_TIMEOUT);
        channel.connect(inetSocketAddress, inetSocketAddress, this);
      } catch (InterruptedException e) {
        onChannelError(new ByteChannelException(e));
      }
    }
  }

  private class WriteResultHandler implements CompletionHandler<Integer, InetSocketAddress> {
    @Override
    public void completed(Integer integer, InetSocketAddress inetSocketAddress) {
      logger.debug("Message sent.");

      try {
        byte[] next = pendingWrites.take();
        logger.debug("Sending message: {}", new String(next));
        channel.write(ByteBuffer.wrap(next), inetSocketAddress, this);
      } catch (InterruptedException e) {
        onChannelError(new ByteChannelException(e));
      }
    }

    @Override
    public void failed(Throwable throwable, InetSocketAddress inetSocketAddress) {
      onChannelError(new ByteChannelException(throwable));
    }
  }

  private class ReadResultHandler implements CompletionHandler<Integer, InetSocketAddress> {
    final ByteBuffer buffer;
    final ByteBuffer cache;

    ReadResultHandler() {
      buffer = ByteBuffer.allocate(MAX_MESSAGE_SIZE);
      cache = ByteBuffer.allocate(MAX_MESSAGE_SIZE);
    }

    @Override
    public void completed(Integer result, InetSocketAddress inetSocketAddress) {
      for (int i = 0; i < result; ++i)
        if (buffer.get(i) == '\n') submitMessage();
        else {
          if (!cache.hasRemaining()) {
            onChannelError(new ByteChannelException(new MessageFormatException("Overflow.")));
            cache.clear();
          }

          cache.put(buffer.get(i));
        }

      channel.read(buffer.clear(), inetSocketAddress, this);
    }

    private void submitMessage() {
      var msg = Arrays.copyOf(cache.array(), cache.position());
      cache.clear();
      onMessage(msg);
    }

    @Override
    public void failed(Throwable throwable, InetSocketAddress inetSocketAddress) {
      onChannelError(new ByteChannelException(throwable));
      channel.read(buffer, inetSocketAddress, this);
    }
  }
}
