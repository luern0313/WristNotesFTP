package cn.luern0313.wristReaderFTP.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.luern0313.wristReaderFTP.R;
import cn.luern0313.wristReaderFTP.model.TransferModel;
import cn.luern0313.wristReaderFTP.util.DataProcessUtil;

/**
 * 被 luern0313 创建于 2020/7/6.
 */

public class TransferAdapter extends BaseAdapter
{
    private LayoutInflater inflater;
    private TransferListener transferListener;

    private ArrayList<TransferModel> transferModelArrayList;

    public TransferAdapter(LayoutInflater inflater, ArrayList<TransferModel> transferModelArrayList, TransferListener transferListener)
    {
        this.inflater = inflater;
        this.transferListener = transferListener;
        this.transferModelArrayList = transferModelArrayList;
    }

    @Override
    public int getCount()
    {
        return transferModelArrayList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return position;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        TransferModel transferModel = transferModelArrayList.get(position);
        ViewHolder viewHolder;
        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.item_transfer, null);
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
            viewHolder.name = convertView.findViewById(R.id.item_transfer_name);
            viewHolder.path = convertView.findViewById(R.id.item_transfer_path);
            viewHolder.size = convertView.findViewById(R.id.item_transfer_size);
            viewHolder.delete = convertView.findViewById(R.id.item_transfer_delete);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.name.setText(transferModel.getFileName());
        viewHolder.path.setText(transferModel.getFilePath());
        viewHolder.size.setText(DataProcessUtil.getSize(transferModel.getFileSize()));
        viewHolder.delete.setOnClickListener(onViewClick(position));
        return convertView;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                transferListener.onClick(v.getId(), position);
            }
        };
    }

    class ViewHolder
    {
        TextView name;
        TextView path;
        TextView size;
        ImageView delete;
    }

    public interface TransferListener
    {
        void onClick(int viewId, int position);
    }
}
