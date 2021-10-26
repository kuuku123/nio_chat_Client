package ui;

import domain.Client;
import domain.Room;
import service.BroadCastService;
import service.NetworkService;
import service.ResponseService;
import util.MyLog;
import util.SendPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static service.NetworkService.selector;
import static util.ElseProcess.availableReqId;
import static util.ElseProcess.read_text_restore;

public class UI
{

    public static Object for_startConnection = new Object();
    public static Object for_uploadfile = new Object();
    public static Object for_broadcastInvite = new Object();
    private final static Logger logr = MyLog.getLogr();
    private Client client;
    private final NetworkService ns;
    private SocketChannel socketChannel;
    public static int fileNum = -1;
    public static String fileName = "";
    int cutSize = 500;

    public UI(Client client, NetworkService networkService)
    {
        this.client = client;
        this.ns = networkService;
        this.socketChannel = client.getSocketChannel();
    }

    public void processInput(String command)
    {
        if (command.length() > 0 && command.charAt(0) == '\\') // if it is command
        {
            if (command.startsWith("login", 1))
            {
                if(command.length()<=6)
                {
                    logr.info("type your user id");
                    return;
                }
                if (client.isLoggedIn() == true)
                {
                    logr.severe("already logged in");
                    return;
                }
                if(!client.getSocketChannel().isOpen())
                {
                    try
                    {
                        socketChannel = SocketChannel.open();
                        socketChannel.configureBlocking(false);
                        SelectionKey selectionKey = socketChannel.register(NetworkService.selector, SelectionKey.OP_CONNECT);
                        SendPackage sendPackage = new SendPackage(client, 0, 0, 0, 0, ByteBuffer.allocate(0));
                        selectionKey.attach(sendPackage);
                        client.setSocketChannel(socketChannel);
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                synchronized (for_startConnection)
                {
                    try
                    {
                        ns.startConnection(client);
                        for_startConnection.wait();
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                if (client.isConnection_start_fail())
                {
                    logr.info("[서버가 준비 안됨 기다려야함]");
                    client.setConnection_start_fail(false);
                    try
                    {
                        client.setSocketChannel(SocketChannel.open());
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    return;
                }

                String name = command.substring(7);
                client.setUserId(name);
                read_text_restore(client);
                int reqId = availableReqId(0);
                SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                SendPackage sendPackage = new SendPackage(client, reqId, 0, 0, 0, ByteBuffer.allocate(0));
                selectionKey.attach(sendPackage);
                selectionKey.interestOps(SelectionKey.OP_WRITE);
                ns.send(selectionKey);
            }
            else if (client.isLoggedIn() == false)
            {
                logr.info("login first");
                return;
            }
            else if (command.startsWith("logout", 1))
            {
                if (client.isLoggedIn() == true)
                {
                    int reqId = availableReqId(1);
                    SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                    SendPackage sendPackage = new SendPackage(client, reqId, 1, 0, 0, ByteBuffer.allocate(0));
                    selectionKey.attach(sendPackage);
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    ns.send(selectionKey);
                }
            }
            else if (command.startsWith("uploadfile", 1))
            {
                if(client.isLoggedIn() == true && client.getCurRoom() == null)
                {
                    logr.info("you are not in the room");
                    return;
                }
                else if(client.isLoggedIn() == true && client.getCurRoom() != null)
                {
                    try
                    {
                        byte[] bytes = Files.readAllBytes(Paths.get("./"+fileName));
                        int totalSize = bytes.length;
                        int blockCount = totalSize / cutSize;
                        int blockLeftover = totalSize % cutSize;
                        int startPos = 0;
                        int tempPos = 0;
                        for(int a = 0; a<=blockCount; a++)
                        {
                            byte[] small;
                            int c = 0;
                            int i = availableReqId(3);
                            if(a == blockCount)
                            {
                                small = new byte[blockLeftover];
                                for(int b = a*cutSize; b<a*cutSize+blockLeftover; b++)
                                {
                                    small[c] = bytes[b];
                                    c++;
                                }
                                tempPos+= blockLeftover;
                            }
                            else
                            {
                                small = new byte[cutSize];
                                for(int b = a*cutSize; b<a*cutSize+cutSize; b++)
                                {
                                    small[c] = bytes[b];
                                    c++;
                                }
                                tempPos += cutSize;
                            }
                            ByteBuffer fileBuf = ByteBuffer.allocate(1000);
                            fileBuf.putInt(fileNum);
                            fileBuf.putInt(startPos);
                            fileBuf.put(small);
                            fileBuf.flip();
                            synchronized (fileBuf)
                            {
                                try
                                {
                                    SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                                    SendPackage sendPackage = new SendPackage(client, i, 3, 0, 0, fileBuf);
                                    selectionKey.attach(sendPackage);
                                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                                    ns.send(selectionKey);

                                    fileBuf.wait(100);
                                } catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            startPos = tempPos;
                        }
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                }

            }
            else if (command.startsWith("showfile", 1))
            {
                if(client.isLoggedIn() == true && client.getCurRoom() == null)
                {
                    logr.info("you are not in the room");
                    return;
                }
                else if(client.isLoggedIn() == true && client.getCurRoom() != null)
                {
                    int i = availableReqId(4);
                    SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                    SendPackage sendPackage = new SendPackage(client, i, 4, 0, 0, ByteBuffer.allocate(0));
                    selectionKey.attach(sendPackage);
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    ns.send(selectionKey);

                }
                return;
            }
            else if (command.startsWith("downloadfile", 1))
            {
                if(client.isLoggedIn() == true && client.getCurRoom() == null)
                {
                    logr.info("you are not in the room");
                    return;
                }
                else if(client.isLoggedIn() == true && client.getCurRoom() != null)
                {
                    int i = availableReqId(5);
                    ByteBuffer buffer = ByteBuffer.allocate(100);
                    int fileNum = Integer.parseInt(command.substring(14));
                    buffer.putInt(fileNum);
                    buffer.putInt(cutSize);
                    buffer.putInt(100);
                    buffer.flip();
                    SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                    SendPackage sendPackage = new SendPackage(client, i, 5, 0, 0, buffer);
                    selectionKey.attach(sendPackage);
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    ns.send(selectionKey);

                }

                return;
            }
            else if (command.startsWith("deletefile", 1))
            {
                if(client.isLoggedIn() == true && client.getCurRoom() == null)
                {
                    logr.info("you are not in the room");
                    return;
                }
                else if(client.isLoggedIn() == true && client.getCurRoom() != null)
                {
                    int i = availableReqId(6);
                    ByteBuffer buffer = ByteBuffer.allocate(100);
                    int fileNum = Integer.parseInt(command.substring(12));
                    buffer.putInt(fileNum);
                    buffer.flip();
                    SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                    SendPackage sendPackage = new SendPackage(client, i, 6, 0, 0, buffer);
                    selectionKey.attach(sendPackage);
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    ns.send(selectionKey);
                }

                return;


            }
            else if (command.startsWith("createroom", 1))
            {
                int reqId = availableReqId(7);
                if (client.isLoggedIn() == true)
                {
                    String roomName = command.substring(12);
                    ByteBuffer roomNameBuf = ByteBuffer.allocate(100);
                    roomNameBuf.put(roomName.getBytes(StandardCharsets.UTF_8));
                    roomNameBuf.position(16);
                    roomNameBuf.flip();

                    SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                    SendPackage sendPackage = new SendPackage(client, reqId, 7, 0, 0, roomNameBuf);
                    selectionKey.attach(sendPackage);
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    ns.send(selectionKey);
                }

            }
            else if (command.startsWith("quitroom", 1))
            {
                if(client.isLoggedIn() == true && client.getCurRoom() == null)
                {
                    logr.info("you are not in the room");
                    return;
                }
                else if(client.isLoggedIn() == true && client.getCurRoom() != null)
                {
                    int i = availableReqId(8);
                    SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                    SendPackage sendPackage = new SendPackage(client, i, 8, 0, 0, ByteBuffer.allocate(0));
                    selectionKey.attach(sendPackage);
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    ns.send(selectionKey);

                    return;
                }
            } else if (command.startsWith("inviteuser", 1))
            {
                if(client.isLoggedIn() == true && client.getCurRoom() == null)
                {
                    logr.info("you are not in the room");
                    return;
                }
                 else if (client.isLoggedIn() == true && client.getCurRoom() != null)
                {
                    int length = command.length();
                    String query = command.substring(12);
                    String[] s1 = query.split(" ");
                    String[] s = new String[s1.length - 1];
                    for (int i = 0; i < s1.length - 1; i++)
                    {
                        s[i] = s1[i];
                    }
                    int roomNum = Integer.parseInt(s1[s1.length - 1]);
                    int userCount = s.length;
                    ByteBuffer inviteBuffer = ByteBuffer.allocate(userCount * 16 + 4);
                    inviteBuffer.putInt(userCount);
                    int i = 0;
                    for (i = 0; i < userCount; i++)
                    {
                        inviteBuffer.position(i * 16 + 4);
                        inviteBuffer.put(s[i].getBytes(StandardCharsets.UTF_8));
                    }
                    inviteBuffer.position(i * 16 + 4);
                    int a = availableReqId(9);
                    inviteBuffer.flip();

                    SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                    SendPackage sendPackage = new SendPackage(client, a, 9, 0, 0, inviteBuffer);
                    selectionKey.attach(sendPackage);
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    ns.send(selectionKey);
                }
            }
            else if (command.startsWith("showuser", 1))
            {
                if(client.isLoggedIn() == true && client.getCurRoom() == null)
                {
                    logr.info("you are not in the room");
                    return;
                }
                else if(client.isLoggedIn() == true && client.getCurRoom() != null)
                {
                    int i = availableReqId(10);

                    SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                    SendPackage sendPackage = new SendPackage(client, i, 10, 0, 0, ByteBuffer.allocate(0));
                    selectionKey.attach(sendPackage);
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    ns.send(selectionKey);
                }
                return;

            } else if (command.startsWith("showroom", 1))
            {
                if (client.isLoggedIn() == true)
                {
                    int i = availableReqId(11);
                    SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                    SendPackage sendPackage = new SendPackage(client, i, 11, 0, 0, ByteBuffer.allocate(0));
                    selectionKey.attach(sendPackage);
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    ns.send(selectionKey);
                }
            } else if (command.startsWith("enterroom", 1))
            {
                if (client.isLoggedIn() == true)
                {
                    int i = availableReqId(12);
                    int roomNum = Integer.parseInt(command.substring(11));
                    for(Room r : client.getRoomList())
                    {
                        if (r.getRoomNum() == roomNum)
                        {
                            client.setCurRoom(r);
                            break;
                        }
                    }

                    SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                    SendPackage sendPackage = new SendPackage(client, i, 12, 0, 0, ByteBuffer.allocate(0));
                    selectionKey.attach(sendPackage);
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    ns.send(selectionKey);
                }

            } else if (command.startsWith("enrollfile", 1))
            {
                if(client.isLoggedIn() == true && client.getCurRoom() == null)
                {
                    logr.info("you are not in the room");
                    return;
                }
                else if(client.isLoggedIn() == true && client.getCurRoom() != null)
                {
                    int i = availableReqId(13);
                    fileName = command.substring(12);
                    ByteBuffer fileNameBuf = ByteBuffer.allocate(100);
                    fileNameBuf.putInt(-1);
                    fileNameBuf.put(fileName.getBytes(StandardCharsets.UTF_8));
                    fileNameBuf.position(20);
                    try
                    {
                        byte[] bytes = Files.readAllBytes(Paths.get("./"+fileName));
                        fileNameBuf.putInt(bytes.length);
                        fileNameBuf.flip();

                        SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                        SendPackage sendPackage = new SendPackage(client, i, 13, 0, 0, fileNameBuf);
                        selectionKey.attach(sendPackage);
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                        ns.send(selectionKey);

                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else if(command.startsWith("exitroom",1))
            {
                if(client.isLoggedIn() == true && client.getCurRoom() == null)
                {
                    logr.info("you are not in the room");
                    return;
                }
                else if(client.isLoggedIn() == true && client.getCurRoom() != null)
                {
                    int i = availableReqId(15);
                    SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                    SendPackage sendPackage = new SendPackage(client, i, 15, 0, 0, ByteBuffer.allocate(0));
                    selectionKey.attach(sendPackage);
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    ns.send(selectionKey);
                    return;
                }
            }
            else
            {
                logr.info("no such command");
                return;
            }
        } else
        {
            if (client.isLoggedIn() == false)
            {
                logr.info("login first");
                return;
            } else if (client.isLoggedIn() == true && client.getCurRoom() == null)
            {
                logr.info("make YOUR chatroom first ");
                return;
            } else
            {
                int i = availableReqId(2);
                ByteBuffer textBuffer = ByteBuffer.allocate(1000);
                textBuffer.put(command.getBytes(StandardCharsets.UTF_8));
                textBuffer.flip();

                SelectionKey selectionKey = client.getSocketChannel().keyFor(selector);
                SendPackage sendPackage = new SendPackage(client, i, 2, 0, 0, textBuffer);
                selectionKey.attach(sendPackage);
                selectionKey.interestOps(SelectionKey.OP_WRITE);
                ns.send(selectionKey);
            }
        }
    }
}
