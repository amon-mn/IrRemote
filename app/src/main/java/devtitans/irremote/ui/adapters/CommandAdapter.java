// Em: app/src/main/java/devtitans/irremote/ui/adapters/CommandAdapter.java

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

    // Interfaces para os cliques
    public interface OnCommandSendListener {
        void onSend(IrCommand command);
    }
    public interface OnCommandDeleteListener {
        void onDelete(IrCommand command);
    }

    public interface OnCommandEditListener {
        void onEdit(IrCommand command);
    }

    private List<IrCommand> commands;
    private OnCommandSendListener sendListener;
    private OnCommandDeleteListener deleteListener;
    private OnCommandEditListener editListener;

    public CommandAdapter(@NonNull Context context, @NonNull List<IrCommand> commands,
                          OnCommandSendListener sendListener,
                          OnCommandDeleteListener deleteListener,
                          OnCommandEditListener editListener) {
        super(context, 0, commands);
        this.commands = commands;
        this.sendListener = sendListener;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Infla o layout list_item_command.xml
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_command, parent, false);
        }

        IrCommand command = commands.get(position);

        TextView tvName = convertView.findViewById(R.id.textViewCommandName);
        ImageButton btnSend = convertView.findViewById(R.id.buttonSend);
        ImageButton btnDelete = convertView.findViewById(R.id.buttonDelete);

        tvName.setText(command.getCommandName());

        // Define os cliques para os ícones
        btnSend.setOnClickListener(v -> {
            if (sendListener != null) {
                sendListener.onSend(command);
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(command);
            }
        });

        convertView.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEdit(command);
            }
        });

        return convertView;
    }
}