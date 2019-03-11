package com.logic.geekchat.model.ChatClient;

import android.util.Log;

import com.logic.geekchat.LogUtil;
import com.logic.geekchat.Util;
import com.logic.geekchat.account.ServerConfig;
import com.logic.geekchat.protocol.GeekChatJsonMethodsRpc;
import com.logic.geekchat.protocol.GeekChatProtocal;
import com.logic.geekchat.protocol.ProtocalListener;
import com.logic.geekchat.protocol.packer.IBodyPacker;
import com.logic.geekchat.protocol.packer.JsonPacker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class ChatClient implements IChatClient {
    private static String TAG = "ChatClient";
    private static IChatClient mInstance;
    private static String mToken;
    private List<Friend> mFriends;

    private ChatClient() {}

    static public IChatClient getInstance() {
        if (mInstance == null) {
            mInstance = new ChatClient();
        }
        return mInstance;
    }

    @Override
    public String getToken() {
        return mToken;
    }

    @Override
    public void setToken(String token) {
        mToken = token;
    }

    @Override
    public void login(final String id, final String password, final boolean force, final ResultListener listener) {
        LogUtil.i(TAG, "login() force:"+force);
        JSONObject json = new JSONObject();
        ProtocalListener seedListener = new ProtocalListener() {
            @Override
            public void onPackerReceived(GeekChatJsonMethodsRpc rpc, IBodyPacker packer) {
                LogUtil.i(TAG, "onPackerReceived");
                JsonPacker jsonPacker = (JsonPacker)packer;
                JSONObject json = jsonPacker.getJson();
                GeekChatProtocal protocal = GeekChatProtocal.getInstance();

                if(rpc.getMethods().equals("com.login.seed.respond")
                        || rpc.getMethods().equals("com.login.force.respond")) {
                    LogUtil.i(TAG, rpc.getMethods());
                    String seedString;
                    try {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(ServerConfig.GEEKCHAT_SERVER_PASSWORD_LENS);
                        seedString = json.getString("seed");
                        if (seedString != null) {
                            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                            messageDigest.update(seedString.getBytes("UTF-8"));
                            byteBuffer.put(password.getBytes());
                            messageDigest.update(byteBuffer.array());

                            json = new JSONObject();
                            if (!force) {
                                json.put("method", "com.login.request");
                            } else {
                                json.put("method", "com.login.force");
                            }
                            json.put("username", id);
                            json.put("crypto", Util.bytesToHexString(messageDigest.digest()));
                            protocal.sendPacker(new JsonPacker(json));
                        }
                    } catch (Exception e) {
                        LogUtil.e(TAG, e.toString());
                        listener.onResult(ResultListener.RESULT_FAILED);
                    }
                    protocal.unregisterMethodsRpc(rpc);

                    rpc.setMethods("com.login.respond");
                    protocal.registerMethodsRpc(rpc);
                } else if(rpc.getMethods().equals("com.login.respond")){
                    LogUtil.i(TAG, "com.login.respond");
                    try {
                        int errno = json.getInt("errno");
                        if(errno == 0) {
                            listener.onResult(ResultListener.RESULT_SUCCEED);
                            mToken = json.getString("token");
                            LogUtil.i(TAG, "succeed to login.token:"+mToken);
                        } else {
                            listener.onResult(ResultListener.RESULT_FAILED);
                        }
                    } catch(Exception ex) {
                        LogUtil.i(TAG, ex.toString());
                        listener.onResult(ResultListener.RESULT_FAILED);
                    }
                    protocal.unregisterMethodsRpc(rpc);
                }
            }

            @Override
            public void onError(GeekChatJsonMethodsRpc rpc, Exception ex) {
                LogUtil.i(TAG, "onError");
                GeekChatProtocal protocal = GeekChatProtocal.getInstance();
                protocal.unregisterMethodsRpc(rpc);
                listener.onResult(ResultListener.RESULT_FAILED);
            }
        };
        GeekChatJsonMethodsRpc rpc;
        if (!force) {
            rpc = new GeekChatJsonMethodsRpc("com.login.seed.respond", seedListener);
        } else {
            rpc = new GeekChatJsonMethodsRpc("com.login.force.respond", seedListener);
        }
        GeekChatProtocal protocal = GeekChatProtocal.getInstance();
        protocal.registerMethodsRpc(rpc);

        try {
            if (!force) {
                json.put("method", "com.login.seed.request");
            } else {
                json.put("method", "com.login.force.request");
            }
            json.put("username", id);
            protocal.sendPacker(new JsonPacker(json));
        } catch(Exception ex) {
            LogUtil.e(TAG, ex.toString());
        }
    }

    @Override
    public void logout(final ResultListener logoutListener) {
        ProtocalListener protocalListener = new ProtocalListener() {
            @Override
            public void onPackerReceived(GeekChatJsonMethodsRpc rpc, IBodyPacker packer) {
                try {
                    if (rpc.getMethods().equals("com.logout.bytoken.respond")) {
                        GeekChatProtocal.getInstance().unregisterMethodsRpc(rpc);

                        JSONObject json = ((JsonPacker) packer).getJson();
                        int errno = json.getInt("errno");
                        LogUtil.i(TAG, "errno:"+errno);
                        if (errno == 0) {
                            logoutListener.onResult(ResultListener.RESULT_SUCCEED);
                        } else {
                            logoutListener.onResult(ResultListener.RESULT_FAILED);
                        }
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, e.toString());
                }
            }

            @Override
            public void onError(GeekChatJsonMethodsRpc rpc, Exception ex) {
                LogUtil.e(TAG, ex.toString());
                logoutListener.onResult(ResultListener.RESULT_FAILED);
            }
        };
        GeekChatJsonMethodsRpc rpc = new GeekChatJsonMethodsRpc("com.logout.bytoken.respond", protocalListener);
        GeekChatProtocal protocal = GeekChatProtocal.getInstance();
        protocal.registerMethodsRpc(rpc);

        try {
            JSONObject json = new JSONObject();
            json.put("method", "com.logout.bytoken.request");
            json.put("token", mToken);
            protocal.sendPacker(new JsonPacker(json));
        } catch (Exception e) {
            LogUtil.e(TAG, e.toString());
        }
    }

    @Override
    public void send(String message, String to, final ResultListener sendListener) {
        final GeekChatProtocal protocal = GeekChatProtocal.getInstance();

        ProtocalListener protocalListener = new ProtocalListener() {
            @Override
            public void onPackerReceived(GeekChatJsonMethodsRpc rpc, IBodyPacker packer) {
                if (rpc.getMethods().equals("com.message.sendto.respond")) {
                    try {
                        protocal.unregisterMethodsRpc(rpc);
                        JsonPacker jsonPacker = (JsonPacker) packer;
                        JSONObject json = jsonPacker.getJson();
                        int errno = json.getInt("errno");
                        if (errno == 0) {
                            sendListener.onResult(ResultListener.RESULT_SUCCEED);
                        } else {
                            sendListener.onResult(ResultListener.RESULT_FAILED);
                        }
                    } catch (Exception e) {
                        LogUtil.i(TAG, e.toString());
                    }
                }
            }

            @Override
            public void onError(GeekChatJsonMethodsRpc rpc, Exception ex) {
                sendListener.onResult(ResultListener.RESULT_FAILED);
            }
        };

        GeekChatJsonMethodsRpc rpc = new GeekChatJsonMethodsRpc("com.message.sendto.respond", protocalListener);
        protocal.registerMethodsRpc(rpc);

        try {
            JSONObject json = new JSONObject();
            json.put("method", "com.message.sendto.request");
            json.put("sendto", to);
            json.put("message", message);
            protocal.sendPacker(new JsonPacker(json));
        } catch(Exception e) {
            LogUtil.i(TAG, e.toString());
        }
    }

    @Override
    public void registerMessageReceiver(final MessageReceiver messageReceiver) {
        final GeekChatProtocal protocal = GeekChatProtocal.getInstance();

        ProtocalListener protocalListener = new ProtocalListener() {
            @Override
            public void onPackerReceived(GeekChatJsonMethodsRpc rpc, IBodyPacker packer) {
                try {
                    JsonPacker jsonPacker = (JsonPacker) packer;
                    JSONObject json = jsonPacker.getJson();
                    String from = json.getString("from");
                    String to = json.getString("to");
                    String uuid = json.getString("uuid");
                    String message = json.getString("message");
                    LogUtil.d(TAG, "new msg from:"+from+" to:"+to+" uuid:"+uuid+" msg:"+message);
                    messageReceiver.onNewMessage(message, from, uuid);

                    JSONObject jsonRespond = new JSONObject();
                    jsonRespond.put("method", "com.message.recv.respond");
                    jsonRespond.put("token", mToken);
                    jsonRespond.put("uuid", uuid);
                    jsonRespond.put("errno", "0x0000");
                    protocal.sendPacker(new JsonPacker(jsonRespond));
                } catch (Exception e) {
                    Log.i(TAG, e.toString());
                }
            }

            @Override
            public void onError(GeekChatJsonMethodsRpc rpc, Exception ex) {
                LogUtil.i(TAG, ex.toString());
            }
        };

        GeekChatJsonMethodsRpc rpc = new GeekChatJsonMethodsRpc("com.message.recv.request", protocalListener);
        protocal.registerMethodsRpc(rpc);
    }

    @Override
    public List<Friend> getFriends() {
        return mFriends;
    }

    @Override
    public void syncFriends(final ResultListener resultListener) {
        LogUtil.i(TAG, "syncFriends()");
        final GeekChatProtocal protocal = GeekChatProtocal.getInstance();

        ProtocalListener protocalListener = new ProtocalListener() {
            @Override
            public void onPackerReceived(GeekChatJsonMethodsRpc rpc, IBodyPacker packer) {
                LogUtil.i(TAG, "onPackerReceived rpc:"+rpc.getMethods());
                try {
                    if (rpc.getMethods().equals("com.friends.list.respond")) {
                        protocal.unregisterMethodsRpc(rpc);

                        JSONObject json = ((JsonPacker) packer).getJson();
                        LogUtil.i(TAG, "json:"+json.toString());
                        int errno = json.getInt("errno");
                        LogUtil.i(TAG, "errno:"+errno);

                        if (errno == 0) {
                            JSONArray jsonArray = json.getJSONArray("friends");
                            List<Friend> friends = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i += 1) {
                                friends.add(new Friend(jsonArray.getString(i)));
                            }
                            mFriends = friends;
                            resultListener.onResult(ResultListener.RESULT_SUCCEED);
                        } else {
                            resultListener.onResult(ResultListener.RESULT_FAILED);
                        }
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, e.toString());
                    resultListener.onResult(ResultListener.RESULT_FAILED);
                }
            }

            @Override
            public void onError(GeekChatJsonMethodsRpc rpc, Exception ex) {
                LogUtil.e(TAG, ex.toString());
                resultListener.onResult(ResultListener.RESULT_FAILED);
            }
        };

        GeekChatJsonMethodsRpc rpc = new GeekChatJsonMethodsRpc("com.friends.list.respond", protocalListener);
        protocal.registerMethodsRpc(rpc);
         try {
             JSONObject json = new JSONObject();

             json.put("method", "com.friends.list.request");
             json.put("token", mToken);
             json.put("pages", "0");
             LogUtil.i(TAG, "send json:"+json.toString());
             protocal.sendPacker(new JsonPacker(json));
         } catch (Exception e) {
             LogUtil.e(TAG, e.toString());
         }
    }

    @Override
    public void register(String id, String password, final ResultListener registerListener) {
        LogUtil.i(TAG, "register()");
        final GeekChatProtocal protocal = GeekChatProtocal.getInstance();

        ProtocalListener protocalListener = new ProtocalListener() {
            @Override
            public void onPackerReceived(GeekChatJsonMethodsRpc rpc, IBodyPacker packer) {
                LogUtil.i(TAG, "onPackerReceived");
                try {
                    if (rpc.getMethods().equals("com.register.respond")) {
                        protocal.unregisterMethodsRpc(rpc);

                        JSONObject json = ((JsonPacker) packer).getJson();
                        int errno = json.getInt("errno");

                        LogUtil.i(TAG, "errno="+errno);
                        if (errno == 0) {
                            registerListener.onResult(ResultListener.RESULT_SUCCEED);
                        } else {
                            registerListener.onResult(ResultListener.RESULT_FAILED);
                        }
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, e.toString());
                    registerListener.onResult(ResultListener.RESULT_FAILED);
                }
            }

            @Override
            public void onError(GeekChatJsonMethodsRpc rpc, Exception ex) {
                LogUtil.e(TAG, "onError");
                LogUtil.e(TAG, ex.toString());
                registerListener.onResult(ResultListener.RESULT_FAILED);
            }
        };

        GeekChatJsonMethodsRpc rpc = new GeekChatJsonMethodsRpc("com.register.respond", protocalListener);
        protocal.registerMethodsRpc(rpc);
        try {
            JSONObject json = new JSONObject();

            json.put("method", "com.register.request");
            json.put("username", id);
            json.put("password", password);
            LogUtil.i(TAG, "sendPacker");
            protocal.sendPacker(new JsonPacker(json));
        } catch (Exception e) {
            LogUtil.e(TAG, e.toString());
        }
    }

    @Override
    public void addFriend(String id, final ResultListener listener) {
        LogUtil.i(TAG, "addFriend()");
        final GeekChatProtocal protocal = GeekChatProtocal.getInstance();

        ProtocalListener protocalListener = new ProtocalListener() {
            @Override
            public void onPackerReceived(GeekChatJsonMethodsRpc rpc, IBodyPacker packer) {
                LogUtil.i(TAG, "onPackerReceived");
                LogUtil.i(TAG, "rpc:"+rpc.getMethods());
                try {
                    if (rpc.getMethods().equals("com.friends.add.respond")) {
                        protocal.unregisterMethodsRpc(rpc);
                        JSONObject json = ((JsonPacker)packer).getJson();
                        int errno = json.getInt("errno");

                        LogUtil.i(TAG, "errno:"+errno);
                        if (errno == 0) {
                            listener.onResult(ResultListener.RESULT_SUCCEED);
                        } else {
                            listener.onResult(ResultListener.RESULT_FAILED);
                        }
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, e.toString());
                }
            }

            @Override
            public void onError(GeekChatJsonMethodsRpc rpc, Exception ex) {

            }
        };

        GeekChatJsonMethodsRpc rpc = new GeekChatJsonMethodsRpc("com.friends.add.respond", protocalListener);
        protocal.registerMethodsRpc(rpc);

        try {
            JSONObject json = new JSONObject();

            json.put("method", "com.friends.add.request");
            json.put("token", mToken);
            json.put("friend", id);
            LogUtil.i(TAG, "send packer");
            protocal.sendPacker(new JsonPacker(json));
        } catch (Exception e) {
            LogUtil.e(TAG, e.toString());
        }
    }

    @Override
    public void heartbeat(final ResultListener listener) {
        LogUtil.i(TAG, "heartbeat()");
        final GeekChatProtocal protocal = GeekChatProtocal.getInstance();

        ProtocalListener protocalListener = new ProtocalListener() {
            @Override
            public void onPackerReceived(GeekChatJsonMethodsRpc rpc, IBodyPacker packer) {
                LogUtil.i(TAG, "onPackerReceived");
                try {
                    JSONObject json = ((JsonPacker) packer).getJson();
                    int errno = json.getInt("errno");
                    LogUtil.i(TAG, "errno:"+errno);

                    if (errno == 0) {
                        listener.onResult(ResultListener.RESULT_SUCCEED);
                    } else {
                        listener.onResult(ResultListener.RESULT_FAILED);
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, e.toString());
                    listener.onResult(ResultListener.RESULT_FAILED);
                }
            }

            @Override
            public void onError(GeekChatJsonMethodsRpc rpc, Exception ex) {
                LogUtil.e(TAG, "onError:"+ex.toString());
            }
        };

        GeekChatJsonMethodsRpc rpc = new GeekChatJsonMethodsRpc("com.heartbeat.respond", protocalListener);
        protocal.registerMethodsRpc(rpc);
        try {
            JSONObject json = new JSONObject();
            json.put("method", "com.heartbeat.request");
            json.put("token", mToken);
            LogUtil.i(TAG, "send json:");
            protocal.sendPacker(new JsonPacker(json));
        } catch (Exception e) {
            LogUtil.i(TAG, e.toString());
        }
    }
}
