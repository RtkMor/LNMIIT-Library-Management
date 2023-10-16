package com.example.abcd.ui.explore;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;

import java.util.List;
public class MyBookAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private final Context context;
    private final List<BookClass> bookList;

    public MyBookAdapter(Context context, List<BookClass> bookList){
        this.context = context;
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position){
        BookClass book = bookList.get(position);

        String bookId = book.getBookId();
        holder.recTitle.setText(bookList.get(position).getTitle());
        holder.recAuthor.setText(bookList.get(position).getAuthor());
        holder.recISBN.setText(bookList.get(position).getISBN());

        holder.recCard.setOnClickListener(view -> {
            Intent intent = new Intent(context, BookDetailsActivity.class);
            intent.putExtra("bookId", bookId);
            intent.putExtra("title", book.getTitle());
            intent.putExtra("author", book.getAuthor());
            intent.putExtra("isbn", book.getISBN());
            intent.putExtra("accountNumber", book.getAccountNumber());
            intent.putExtra("callNumber", book.getCallNumber());
            intent.putExtra("rating", book.getRating());
            intent.putExtra("ratingNumber", book.getRatingNumber());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount(){
        return bookList.size();
    }

}

class MyViewHolder extends RecyclerView.ViewHolder{
    TextView recTitle, recAuthor, recISBN;
    View recCard;
    public MyViewHolder(@NonNull View itemView){
        super(itemView);

        recCard = itemView.findViewById(R.id.recCard);
        recTitle = itemView.findViewById(R.id.recTitle);
        recAuthor = itemView.findViewById(R.id.recAuthor);
        recISBN = itemView.findViewById(R.id.recISBN);

    }
}
