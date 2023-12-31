package com.jose.diceroller.db;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.jose.diceroller.R;

import java.util.List;

public class ListAdapterdb extends RecyclerView.Adapter<ListAdapterdb.ViewHolder> {
    private List<PlayerHistorydb> mData;
    private LayoutInflater mInflater;

    private Context context;

    public ListAdapterdb(List<PlayerHistorydb> itemList, Context context){
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mData = itemList;

    }
    @Override
    public int getItemCount(){
        return mData.size();
    }
    @Override
    public ListAdapterdb.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = mInflater.inflate(R.layout.list_element, null);
        return new ListAdapterdb.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(final ListAdapterdb.ViewHolder holder, final int position){
        holder.bindData(mData.get(position));
    }
    public void setItems(List<PlayerHistorydb> items){
        mData = items;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView name, puntuacion, fecha, id, latitud, longitud;
        ViewHolder(View itemView){
            super(itemView);

            id = itemView.findViewById(R.id.idTextView);
            name = itemView.findViewById(R.id.nameTextView);
            puntuacion = itemView.findViewById(R.id.puntuacionTextView);
            fecha = itemView.findViewById(R.id.fechaTextView);
            latitud = itemView.findViewById(R.id.latitudTextView);
            longitud = itemView.findViewById(R.id.longitudTextView);

        }
        void bindData(final PlayerHistorydb item){
            id.setText("Id: "+(String.valueOf(item.getId())));
            name.setText(item.getNombre());
            puntuacion.setText("Monedas: " +(String.valueOf(item.getPuntuacion())));
            fecha.setText(item.getFecha());
            // Usar DecimalFormat para limitar a 5 dígitos
            java.text.DecimalFormat df = new java.text.DecimalFormat("#.######");
            latitud.setText("Lat: " + df.format(item.getLatitud()));
            longitud.setText("Long: " + df.format(item.getLongitud()));
        }
    }
}