package com.kalabhedia.gimme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.MyViewHolder> {

    private Context mContext;
    private List<CardArray> CardList;
    private String phoneNumber;
    private CardArray cardArray;

    public CardAdapter(Context mContext, List<CardArray> cardArrays) {
        this.mContext = mContext;
        this.CardList = cardArrays;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        cardArray = CardList.get(position);
        holder.title.setText(cardArray.name);
        int money = Integer.parseInt(cardArray.verifiedSum);
        if (money > 0) {
            holder.count.setText("₹ " + money + "");
            holder.count.setBackgroundResource(R.drawable.circle_plus);
        } else {
            holder.count.setText("₹ " + (-1 * money) + "");
            holder.count.setBackgroundResource(R.drawable.circle_minus);
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("phoneNumber", cardArray.name);
                bundle.putString("amount", cardArray.verifiedSum);
                Intent intent = new Intent(mContext, ShowSpecificUser.class);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                alertDialogBuilder.setTitle("Settle up?");
                alertDialogBuilder.setMessage("Once settlement is successful your activity concerned to the person will get delete.");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        });

                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return false;
            }
        });//TODO
        holder.count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("phoneNumber", cardArray.name);
                bundle.putString("amount", cardArray.verifiedSum);
                Intent intent = new Intent(mContext, ShowSpecificUser.class);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });//TODO

    }


    @Override
    public int getItemCount() {
        return CardList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView title, count;
        private ImageView thumbnail, overflow;
        private CardView cardView;
        private AdapterView.OnItemClickListener clickListener;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.card_title);
            count = (TextView) view.findViewById(R.id.count);
            cardView = view.findViewById(R.id.card_view);
        }

        @Override
        public void onClick(View view) {
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    }
}

