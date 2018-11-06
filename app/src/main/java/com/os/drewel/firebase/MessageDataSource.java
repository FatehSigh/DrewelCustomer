package com.os.drewel.firebase;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.os.drewel.application.DrewelApplication;
import com.os.drewel.model.ChannelInfoModel;
import com.os.drewel.model.ChatModel;
import com.os.drewel.model.ChatUserModel;
import com.os.drewel.model.MessageModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MessageDataSource {
    public static final DatabaseReference chatRoot = FirebaseDatabase.getInstance().getReference("chatmodel");
    private static final String TAG = "MessageDataSource";

    public static void saveMessage(ChatModel message, String convoId) {
        chatRoot.child(convoId).child("channel_info").setValue(message.getChannel_info());
//      chatRoot.child(convoId).child("messages").child(message.getMessages().getTime()).setValue(message.getMessages());
        chatRoot.child(convoId).child("messages").push().setValue(message.getMessages());
    }


    public static void stop(MessagesListener listener) {
        chatRoot.removeEventListener((com.google.firebase.database.ValueEventListener) listener);
    }

    public static void stop(ChannelListener listener) {
        chatRoot.removeEventListener((com.google.firebase.database.ValueEventListener) listener);
        chatRoot.removeEventListener((com.google.firebase.database.ChildEventListener) listener);

    }

    @Nullable
    public static ChannelListener addChannelListener(@NotNull String sender_id, @NotNull MessagesCallbacks callbacks) {
        ChannelListener listener = new ChannelListener(callbacks);
        chatRoot.child(sender_id).addChildEventListener(listener);
        chatRoot.child(sender_id).addValueEventListener(listener);
        return listener;
    }

    public static MessagesListener addMessagesListener(@NotNull String sender_id, @NotNull MessagesCallbacks callbacks) {
        MessagesListener listener = new MessagesListener(callbacks);
        chatRoot.child(sender_id).child("messages").addChildEventListener(listener);
        return listener;
    }

    public static class MessagesListener implements ChildEventListener, ValueEventListener {
        private MessagesCallbacks callbacks;

        MessagesListener(MessagesCallbacks callbacks) {
            this.callbacks = callbacks;
        }

        @Override
        public void onChildAdded(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @android.support.annotation.Nullable String s) {
            Log.e("MessagesLi onChild", dataSnapshot.toString());
            HashMap<String, String> msg = (HashMap) dataSnapshot.getValue();
            if (msg == null)
                return;
            Log.e("MessagesChild", msg.toString());
            ChatModel chatModel = new ChatModel();
            MessageModel messageModel = new MessageModel();


            messageModel.setMessage((String) msg.get("message"));
            messageModel.setMsg_channel((String) msg.get("msg_channel"));
            messageModel.setReceiver_id((String) msg.get("receiver_id"));
            messageModel.setReceiver_name((String) msg.get("receiver_name"));
            messageModel.setReceiver_profile_image((String) msg.get("receiver_profile_image"));
            messageModel.setSender_id((String) msg.get("sender_id"));
            messageModel.setSender_name(msg.get("sender_name"));
            messageModel.setTime(msg.get("time"));
            messageModel.setSender_profile_image(msg.get("sender_profile_image"));
            chatModel.setMessages(messageModel);
            if (callbacks != null) {
                callbacks.onMessageAdded(chatModel);
            }
        }

        @Override
        public void onChildChanged(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @android.support.annotation.Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @android.support.annotation.Nullable String s) {

        }


        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.e("MessagesLi onChild", dataSnapshot.toString());
            HashMap<String, String> msg = (HashMap) dataSnapshot.getValue();
            if (msg == null)
                return;
            ChatModel chatModel = new ChatModel();
            ChannelInfoModel channelInfoModel = new ChannelInfoModel();
            MessageModel messageModel = new MessageModel();
            if (dataSnapshot.getKey().equalsIgnoreCase("messages")) {
                messageModel.setMessage((String) msg.get("message"));
                messageModel.setMsg_channel((String) msg.get("msg_channel"));
                messageModel.setReceiver_id((String) msg.get("receiver_id"));
                messageModel.setReceiver_name((String) msg.get("receiver_name"));
                messageModel.setReceiver_profile_image((String) msg.get("receiver_profile_image"));
                messageModel.setSender_id((String) msg.get("sender_id"));
                messageModel.setSender_name((String) msg.get("sender_name"));
                messageModel.setTime((String) msg.get("time"));
                messageModel.setSender_profile_image((String) msg.get("sender_profile_image"));
                chatModel.setMessages(messageModel);
                if (callbacks != null) {
                    callbacks.onMessageAdded(chatModel);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }

    public static class ChannelListener implements ChildEventListener, ValueEventListener {
        private MessagesCallbacks callbacks;

        ChannelListener(MessagesCallbacks callbacks) {
            this.callbacks = callbacks;
        }

        @Override
        public void onChildAdded(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @android.support.annotation.Nullable String s) {
            Log.e("onChildAdded", dataSnapshot.toString());
            HashMap<String, String> msg = (HashMap) dataSnapshot.getValue();
            if (msg == null)
                return;
            Log.e("MessagesChild", msg.toString());
            ChatModel chatModel = new ChatModel();
            ChannelInfoModel channelInfoModel = new ChannelInfoModel();
            MessageModel messageModel = new MessageModel();
            if (dataSnapshot.getKey().equalsIgnoreCase("channel_info")) {
                if (msg.containsKey("admin_count"))
                    channelInfoModel.setAdmin_count(Integer.parseInt(String.valueOf(msg.get("admin_count"))));
                else
                    channelInfoModel.setAdmin_count(DrewelApplication.Companion.getAdmin_unread_count());
                channelInfoModel.setMessage(String.valueOf(msg.get("message")));
                channelInfoModel.setReceiver_id(msg.get("receiver_id"));
                channelInfoModel.setReceiver_name(msg.get("receiver_name"));
                channelInfoModel.setReceiver_profile_image(msg.get("receiver_profile_image"));
                channelInfoModel.setSender_id(msg.get("sender_id"));
                channelInfoModel.setTime(msg.get("time"));
                if (msg.containsKey("user_count"))
                    channelInfoModel.setUser_count(Integer.parseInt(String.valueOf(msg.get("user_count"))));
                else
                    channelInfoModel.setUser_count(DrewelApplication.Companion.getUser_unread_count());

                chatModel.setChannel_info(channelInfoModel);
                if (callbacks != null) {
                    callbacks.onChannelAdded(chatModel);
                }
            }


        }

        @Override
        public void onChildChanged(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @android.support.annotation.Nullable String s) {
            Log.e("onChildChanged", dataSnapshot.toString());
            Log.e("onDataChange", dataSnapshot.toString());
            HashMap<String, String> msg = (HashMap) dataSnapshot.getValue();
            if (msg == null)
                return;
            Log.e("MessagesChild", msg.toString());
            ChatModel chatModel = new ChatModel();
            ChannelInfoModel channelInfoModel = new ChannelInfoModel();
            MessageModel messageModel = new MessageModel();
            if (dataSnapshot.getKey().equalsIgnoreCase("channel_info")) {
                if (msg.containsKey("admin_count"))
                    channelInfoModel.setAdmin_count(Integer.parseInt(String.valueOf(msg.get("admin_count"))));
                else
                    channelInfoModel.setAdmin_count(DrewelApplication.Companion.getAdmin_unread_count());
                channelInfoModel.setMessage(String.valueOf(msg.get("message")));
                channelInfoModel.setReceiver_id(msg.get("receiver_id"));
                channelInfoModel.setReceiver_name(msg.get("receiver_name"));
                channelInfoModel.setReceiver_profile_image(msg.get("receiver_profile_image"));
                channelInfoModel.setSender_id(msg.get("sender_id"));
                channelInfoModel.setTime(msg.get("time"));
                if (msg.containsKey("user_count"))
                    channelInfoModel.setUser_count(Integer.parseInt(String.valueOf(msg.get("user_count"))));
                else
                    channelInfoModel.setUser_count(DrewelApplication.Companion.getUser_unread_count());

                chatModel.setChannel_info(channelInfoModel);
                if (callbacks != null) {
                    callbacks.onChannelAdded(chatModel);
                }
            }
        }

        @Override
        public void onChildRemoved(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
            Log.e("onChildRemoved", dataSnapshot.toString());
        }

        @Override
        public void onChildMoved(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @android.support.annotation.Nullable String s) {
            Log.e("onChildMoved", dataSnapshot.toString());
        }


        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e("onDataChange", databaseError.getMessage());
        }
    }

    public interface MessagesCallbacks {
        void onMessageAdded(ChatModel message);

        void onChannelAdded(ChatModel message);

    }
}
