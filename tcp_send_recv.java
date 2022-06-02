package com.example.myfirstapp;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.myfirstapp.SocketHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class tcp_send_recv extends AsyncTask<String,Handler,Void>
{
    PrintWriter pw;
    String IP="192.168.0.41";
    int PORT = 8820;
    final static int LEN_SIZE = 8;

    public tcp_send_recv(Handler mHandler)
    {
        Socket sk = SocketHandler.getSocket();
        Handler h = SocketHandler.getHreceiver();
        if (h != mHandler)
            SocketHandler.setHreceiver(mHandler);
    }


    @Override
    protected Void doInBackground(String... voids) //send data by size
    {
        String TAG = "doInBackground";
        Socket sk = SocketHandler.getSocket();

        if (sk == null) //check if there isn't a socket-try to connect to it.
        {
            try
            {
                Log.d(TAG, "Before Connect");
                sk = new Socket(this.IP, this.PORT);
                SocketHandler.setSocket(sk);

                Log.d(TAG, "connected");
                Thread listener = new Thread(new Listener(sk));
                listener.start();
            }
            catch (UnknownHostException e)
            {
                Log.e(TAG, "ERROR UnknownHostException socket");
            }
            catch (IOException e)
            {
                Log.e(TAG, "ERROR IOException socket");
            }
        }

        //send data by size
        String data = voids[0];
        data = String.format("%08d", data.length()) + "|" + data; // the data's size+the data
        Log.d(TAG, "Before Send");
        try
        {
            pw = new PrintWriter(sk.getOutputStream());
            pw.write(data);

            pw.flush();
            Log.d(TAG, "After Send:" + data);
            //pw.close();
        }
        catch (IOException e)
        {
            Log.e(TAG, "ERROR write " + e.getMessage());

            Handler mHandler = SocketHandler.getHreceiver(); // send to the current active activity
            Message msg = mHandler.obtainMessage();
            msg.obj = "Socket Error";
            mHandler.sendMessage(msg);
        }
        return null;
    }


    class Listener implements Runnable
    {
        Socket skl;
        BufferedReader in;
        char[] cbuf;
        String TAG = "Listener";

        public Listener(Socket sk)
        {
            this.skl = sk;

            try // create a new buffer reader
            {
                this.in = new BufferedReader(new InputStreamReader(this.skl.getInputStream()));
            }

            catch (IOException e)
            {
                Log.e(TAG, "ERROR buffer read " + e.getMessage());
                e.printStackTrace();
            }
            cbuf = new char[2000];
        }

        @Override
        public void run() //receiving data
        {
            boolean ok = true;

            while (ok)
            {
                try
                {
                    if (in.ready())
                    {
                        int len_read =0;
                        char[] cbuflen = new char[LEN_SIZE];

                        while (len_read < LEN_SIZE)
                        {
                            len_read += in.read(cbuflen, 0, LEN_SIZE-len_read);
                        }
                        String received_len = new String(cbuflen, 0, LEN_SIZE);
                        int total_to_read = Integer.parseInt(received_len);

                        int len = in.read(cbuf, 0, total_to_read +1);
                        String received = received_len + new String(cbuf, 0, len) ;
                        len += LEN_SIZE;
                        if (len > 0)
                        {
                            Log.d(TAG, " **** got data :" + received ); // the received data!

                            Handler mHandler = SocketHandler.getHreceiver();
                            if (mHandler == null)
                            {
                                try
                                {
                                    Thread.sleep(2000);
                                }
                                catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                                mHandler = SocketHandler.getHreceiver(); // the handler give the received data to current activity.
                            }
                            if (mHandler == null)
                            {
                                try
                                {
                                    Thread.sleep(2000);
                                }
                                catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                                mHandler = SocketHandler.getHreceiver();
                            }

                            if (mHandler != null)
                            {
                                Message msg = mHandler.obtainMessage();
                                msg.obj = received;
                                mHandler.sendMessage(msg);
                            }
                            else
                            {
                            Log.e(TAG, "Handle = Null,skipping msg=" + received );
                            }
                        }
                    }
                }
                catch (IOException e)
                {
                    Log.e(TAG, "ERROR read line- " + e.getMessage() + ".");
                    e.printStackTrace();
                    ok = false;
                }

                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    Log.e(TAG, "ERROR InterruptedException " + e.getMessage());
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "Login Listener finished ");
        }
    }
}
