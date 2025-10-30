package devtitans.irremote.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import devtitans.irremote.R;
import devtitans.irremote.data.model.IrCommand;

public class CommandAdapter extends ArrayAdapter<IrCommand> {

    private final CommandClickListener listener;

    public interface CommandClickListener {
        void onSendClick(IrCommand command);
        void onDeleteClick(IrCommand command);
    }

    public CommandAdapter(@NonNull Context context, @NonNull List<IrCommand> commands, CommandClickListener listener) {
        super(context, 0, commands);
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_command, parent, false);
        }

        IrCommand currentCommand = getItem(position);

        TextView textViewCommandName = convertView.findViewById(R.id.textViewCommandName);
        ImageButton buttonSend = convertView.findViewById(R.id.buttonSend);
        ImageButton buttonDelete = convertView.findViewById(R.id.buttonDelete);

        if (currentCommand != null) {
            textViewCommandName.setText(currentCommand.getName());

            buttonSend.setOnClickListener(v -> listener.onSendClick(currentCommand));
            buttonDelete.setOnClickListener(v -> listener.onDeleteClick(currentCommand));
        }

        return convertView;
    }
}
